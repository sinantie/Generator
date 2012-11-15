package induction.problem.event3.generative.alignment;

import edu.berkeley.nlp.ling.Tree;
import fig.basic.Indexer;
import induction.problem.event3.params.Params;
import induction.Hypergraph;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CFGNode;
import induction.problem.event3.params.CFGParams;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author konstas
 */
public class InferStatePCFG extends InferState
{    
    Tree<String> recordTree;
    Indexer<String> indexer;
    LinkedList<Integer> sentenceBoundaries;    
    int docLengthBin;
    
    public InferStatePCFG(Event3Model model, Example ex, Params params, Params counts,
            InferSpec ispec)
    {
        super(model, ex, params, counts, ispec);
        recordTree = ex.getTrueWidget() != null ? ex.getTrueWidget().getRecordTree() : null;
        indexer = model.getRulesIndexer();               
    }

    @Override
    protected void initInferState(AModel model)
    {
        super.initInferState(model);        
        // keep track of sentence boundaries
        sentenceBoundaries = new LinkedList<Integer>();
        for(int i = 0; i < ex.getIsSentenceBoundaryArray().length; i++)
        {
            if(ex.getIsSentenceBoundaryArray()[i])
                sentenceBoundaries.add(i);
        }                
        docLengthBin = N > opts.maxDocLength ? params.cfgParams.getNumOfBins() - 1 : N / opts.docLengthBinSize;
    }
       
    @Override
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {
        hypergraph.debug = opts.debug;
        // Need this because the pc sets might be inconsistent with the types
        hypergraph.allowEmptyNodes = true;

        if (genLabels() || prevGenLabels())
        {
            // Default is to generate the labels from a generic distribution
            // unless we say otherwise
            for(int i = 0; i < ex.N(); i++)
            {
                final int label = labels[i];
                hypergraph.addEdge(hypergraph.prodStartNode(),
                        new Hypergraph.HyperedgeInfo<Widget>()
                // Default is to generate the labels from a generic distribution
                // unless we say otherwise
                {
                    public double getWeight()
                    {
                        return get(params.genericLabelChoices, label);
                    }
                    public void setPosterior(double prob)
                    {
                        if (genLabels())
                            update(counts.genericLabelChoices, label, prob);
                    }
                    public Widget choose(Widget widget)
                    {
                        return widget;
                    }
                });
            } // for
        } // if

        if(opts.fixRecordSelection)
            hypergraph.addEdge(hypergraph.prodStartNode(), genEdge(0, N, recordTree));
        else
            hypergraph.addEdge(hypergraph.prodStartNode(), genEdge(0, N, indexer.getIndex("S"), sentenceBoundaries));
    }              
    
    /**
     * Build binarized record content selection model, given the structure of the input
     * in a Penn Treebank format (binarized trees with induced constituents on the sentence level). 
     * <br/>
     * The structure of the record dependencies is fixed on the <code>tree</code> input,
     * and we don't learn the weights on each hyperedge, as we have already computed them offline.
     * However, we enumerate all the spans of the children of frontier non-terminals that are contained within a sentence.
     * 
     * @param start the beginning of the span of the <code>tree</code>
     * @param end the end of the span of the <code>tree</code>
     * @param tree the input (sub)-tree in Penn Treebank format
     * @return the head node of the hyperedge
     */
    protected CFGNode genEdge(int start, int end, Tree<String> tree)
    {
        final CFGParams cfgParams = params.cfgParams;
        final CFGParams cfgCounts = counts.cfgParams;
        final int lhs = indexer.getIndex(tree.getLabel());
        final boolean isRootRule = opts.wordsPerRootRule ? ((Event3Model)model).isRootRule(lhs) : false;
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            // check if we are in a record leaf, or a pre-terminal.
            // In either case we treat them as equal, i.e., generate the record / field set
//            if (tree.getChildren().size() == 1 || tree.isLeaf())
            if (tree.isPreTerminal() || tree.isLeaf())
            {
                String label = tree.getLabel();
                int eventTypeIndex = label.equals("none") ? cfgParams.none_t : ((Event3Model)model).getEventTypeNameIndexer().getIndex(label);
                hypergraph.addEdge(node, genRecord(start, end, eventTypeIndex));
            }  // if
            else // we are in a subtree with a non-terminal lhs and two rhs symbols
            {
                final List<Tree<String>> children = tree.getChildren();
                // check whether there is at least another sentence boundary between
                // start and end. If there is, define this as a splitting point between
                // children subtrees.
                Integer nextBoundary = sentenceBoundaries.peek() + 1; // cross punctuation
                if(nextBoundary < end)
                {   
                    sentenceBoundaries.poll();                         
                    if(children.size() == 1) // unary trees
                    {
                        hypergraph.addEdge(node, genEdge(start, nextBoundary, children.get(0)),                                              
                          new Hypergraph.HyperedgeInfo<Widget>() {
                              int rhs = indexer.getIndex(children.get(0).getLabel());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs));
                              public double getWeight() {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { 
                                  if(isRootRule)
                                      update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                              }
                              public Widget choose(Widget widget) {                          
                                  return widget;
                              }
                          }); 
                    }
                    else
                    {
                        hypergraph.addEdge(node, genEdge(start, nextBoundary, children.get(0)), 
                                                 genEdge(nextBoundary, end, children.get(1)),
                          new Hypergraph.HyperedgeInfo<Widget>() {
                              int rhs1 = indexer.getIndex(children.get(0).getLabel());
                              int rhs2 = indexer.getIndex(children.get(1).getLabel());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs1, rhs2));
                              public double getWeight() {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { 
                                  if(isRootRule)
                                      update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                              }
                              public Widget choose(Widget widget) {                          
                                  return widget;
                              }
                          });
                    } // binary trees only                    
                } // if
                // children are records/leaf nodes in the same sentence. 
                // Generate edges for every sub-span between start and end
                else 
                {
                    if(children.size() == 1) // unary trees
                    {
                        hypergraph.addEdge(node, genEdge(start, end, children.get(0)),                                              
                          new Hypergraph.HyperedgeInfo<Widget>() {
                              int rhs = indexer.getIndex(children.get(0).getLabel());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs));
                              public double getWeight() {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { 
                                  if(isRootRule)
                                      update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                              }
                              public Widget choose(Widget widget) {                          
                                  return widget;
                              }
                          }); 
                    }
                    else // binary trees only
                    {
                        for(int k = start + 1; k < end ; k++)
                        {                                                
                            hypergraph.addEdge(node, genEdge(start, k, children.get(0)), 
                                                     genEdge(k, end, children.get(1)),
                              new Hypergraph.HyperedgeInfo<Widget>() {
                                  int rhs1 = indexer.getIndex(children.get(0).getLabel());
                                  int rhs2 = indexer.getIndex(children.get(1).getLabel());
                                  int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs1, rhs2));
                                  public double getWeight() {
                                      return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                                  }
                                  public void setPosterior(double prob) { 
                                      if(isRootRule)
                                          update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                                  }
                                  public Widget choose(Widget widget) {                          
                                      return widget;
                                  }
                              }); 
                        } // for
                    } // else                    
                } // else
            } // else
        } // if
        return node;
    }
    
    protected Object genEdge(int start, int end, final int lhs, LinkedList<Integer> sentenceBoundaries)
    {
        Indexer eventTypeIndxer = ((Event3Model)model).getEventTypeNameIndexer();
        final CFGParams cfgParams = params.cfgParams;
        final CFGParams cfgCounts = counts.cfgParams;
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            String label = indexer.getObject(lhs);
            Integer nextBoundary = sentenceBoundaries.peek() + 1; // cross punctuation
            int eventTypeIndex = label.equals("none") ? cfgParams.none_t : (eventTypeIndxer.contains(label) ? eventTypeIndxer.getIndex(label) : -1);
            // check if we are in a record leaf and the example contains events of this eventType (not always the case e.g., in ATIS), 
            // and generate the record / field set
            if ( eventTypeIndex != -1 && (eventTypeIndex == cfgParams.none_t || ex.eventsByEventType.containsKey(eventTypeIndex)) )
            {                
                hypergraph.addEdge(node, genRecord(start, end, eventTypeIndex));
            } // if
            else // we are in a subtree with a non-terminal lhs and two rhs symbols
            {
                final HashMap<CFGRule, Integer> candidateRules = ((Event3Model)model).getCfgCandidateRules(lhs);                         
                for(Entry<CFGRule, Integer> candidateRule : candidateRules.entrySet()) // try to expand every rule with the same lhs
                {
                    final int rhs1 = candidateRule.getKey().getRhs1();                    
                    final int indexOfRule =  candidateRule.getValue();
                    final boolean isUnary = candidateRule.getKey().isUnary();
                    final boolean isRootRule = opts.wordsPerRootRule ? ((Event3Model)model).isRootRule(candidateRule.getKey()) : false;
                    if(isUnary) // unary trees
                    {
                        hypergraph.addEdge(node, genEdge(start, end, rhs1, sentenceBoundaries),
                          new Hypergraph.HyperedgeInfo<Widget>() {                              
                              public double getWeight() {                                  
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule) *
                                         (isRootRule ? get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) : 1.0);
                              }
                              public void setPosterior(double prob) {
                                  update(cfgCounts.getCfgRulesChoices().get(lhs), indexOfRule, prob);
                                  if(isRootRule)
                                      update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                              }
                              public Widget choose(Widget widget) {                          
                                  return widget;
                              }
                          }); 
                    }
                    else // binary trees only
                    {
                        final int rhs2 = candidateRule.getKey().getRhs2();
                        if(containsSentence(indexer, rhs1, rhs2))
                        {
                            if(sentenceBoundaries.size() > 1) // there are more sentences in the example
                            {
                                LinkedList<Integer> sentenceBoundariesCloned = new LinkedList<Integer>(sentenceBoundaries);
                                sentenceBoundariesCloned.poll();                            
                                hypergraph.addEdge(node, genEdge(start, nextBoundary, rhs1, sentenceBoundariesCloned), 
                                                         genEdge(nextBoundary, end, rhs2, sentenceBoundariesCloned),
                                  new Hypergraph.HyperedgeInfo<Widget>() {                              
                                      public double getWeight() {
                                          return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule) *
                                         (isRootRule ? get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) : 1.0);
                                      }
                                      public void setPosterior(double prob) {
                                          update(cfgCounts.getCfgRulesChoices().get(lhs), indexOfRule, prob);
                                          if(isRootRule)
                                              update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                                      }
                                      public Widget choose(Widget widget) {                          
                                          return widget;
                                      }
                                  }); 
                            }
    //                        else
    //                            return hypergraph.invalidNode;
                        }
                        else
                        {
    //                        if(sentenceBoundaries.size() > 1)
    //                            return hypergraph.invalidNode;
                            // last sentence, or many records spanning the same sentence; 
                            // don't expand in case there are more sentences in the example
                            if(sentenceBoundaries.size() == 1 || nextBoundary >= end)
                            {
                                for(int k = start + 1; k < end ; k++)
                                {                                                                                        
                                    hypergraph.addEdge(node, genEdge(start, k, rhs1, sentenceBoundaries), 
                                                             genEdge(k, end, rhs2, sentenceBoundaries),
                                      new Hypergraph.HyperedgeInfo<Widget>() {                                      
                                          public double getWeight() {
                                              return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule) *
                                             (isRootRule ? get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) : 1.0);
                                          }
                                          public void setPosterior(double prob) {
                                              update(cfgCounts.getCfgRulesChoices().get(lhs), indexOfRule, prob);
                                              if(isRootRule)
                                                  update(cfgCounts.getWordsPerRootRule()[indexOfRule], docLengthBin, prob);
                                          }
                                          public Widget choose(Widget widget) {
                                              return widget;
                                          }
                                      });
                                } // for
                            }
                        } // if (not contains 
                    } // else (binary trees)                    
                } // for
            } // else
        } // if
        return node;
    }  
}