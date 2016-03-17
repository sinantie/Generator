package induction.problem.event3.generative.generation;

import edu.berkeley.nlp.ling.Tree;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import induction.problem.event3.params.Params;
import induction.Hypergraph;
import induction.Utils;
import induction.ngrams.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Event;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CFGNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.CFGParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author konstas
 */
public class GenInferStatePCFG extends GenInferState
{   
    Tree<String> recordTree;
    Indexer<String> indexer;
    LinkedList<Integer> sentenceBoundaries;
    Map<Integer, Integer> minWordsPerNonTerminal;
    Map<Integer, HashMap<CFGRule, Integer>> cfgRules;
    protected Set<CFGRule> excludedCfgRules;
    int docLengthBin;
    
    public GenInferStatePCFG(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
        recordTree = ex.getTrueWidget() != null ? ex.getTrueWidget().getRecordTree() : null;
        indexer = model.getRulesIndexer();
        minWordsPerNonTerminal = model.getMinWordsPerNonTerminal();        
        excludedCfgRules = new HashSet<>();
        cfgRules = cloneCfgRules(model.getCfgRules());
    }        
    
    @Override
    protected void initInferState(AModel model, int textLength)
    {
        super.initInferState(model, textLength);        
        if(opts.fixRecordSelection)
        {
            // keep track of sentence boundaries
            sentenceBoundaries = new LinkedList<>();
            for(int i = 0; i < ex.getIsSentenceBoundaryArray().length; i++)
            {
                if(ex.getIsSentenceBoundaryArray()[i])
                    sentenceBoundaries.add(i);
            }
        }
        docLengthBin = N > opts.maxDocLength ? params.cfgParams.getNumOfBins() - 1 : N / opts.docLengthBinSize;
        if(opts.nonRecursiveGrammar)
            excludeCfgRule(indexer.getIndex("S"));
    }
    
    @Override
    protected Widget newWidget()
    {       
        HashMap<Integer, Integer> eventTypeIndices =
                            new HashMap<Integer, Integer>(ex.events.size());
        for(Event e : ex.events.values())
        {
            eventTypeIndices.put(e.getId(), e.getEventTypeIndex());
        }
        return new GenWidget(newMatrix(), newMatrix(), newMatrix(), newMatrix(),
                             newMatrixOne(), newMatrixOne(),
                             ((Event3Model)model).eventTypeAllowedOnTrack, eventTypeIndices, 
                                opts.outputPcfgTrees ? "S" : null, new String[0]);
    }
    
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {        
        // setup hypergraph preliminaries
        hypergraph.setup(this, opts.debug, opts.modelType, true, opts.kBest, ngramModel, secondaryNgramModel, opts.ngramSize,
                opts.reorderType, opts.allowConsecutiveEvents,
                opts.oracleReranker,
                opts.useDependencies,
                opts.interpolationFactor,
                opts.posAtSurfaceLevel,
                opts.tagDelimiter,
                /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
                vocabulary.getIndex("<num>"),
                vocabulary.getIndex("ELIDED_SYMBOL"),
                opts.numAsSymbol,
                vocabulary, ex, graph);
        
        if(opts.fullPredRandomBaseline)
        {
            this.hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).boundary_t()));
        } // if
        else
        {
            WordNode startSymbol = new WordNode(-1, 0, -1, -1);
            hypergraph.addSumNode(startSymbol);
            WordNode endSymbol = new WordNode(ex.N() + 1, 0, -1, -1);
            if(!opts.useStopNode)
                hypergraph.addSumNode(endSymbol);
            this.hypergraph.addEdge(startSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
            {
                public double getWeight()
                { return 1;}
                public Pair getWeightAtRank(int rank)
                {
                    if(rank > 0)
                        return null;
                    return new Pair(1.0, vocabulary.getIndex("<s>"));
                }
                public void setPosterior(double prob)
                { }
                 public GenWidget choose(GenWidget widget)
                { return widget; }

                public GenWidget chooseWord(GenWidget widget, int word)
                { return widget; }
            });
            //////////////////////////////////////////////////////////////////////////////////
            if(!opts.useStopNode)
            {
                this.hypergraph.addEdge(endSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
                {
                    public double getWeight()
                    { return 1;}
                    public Pair getWeightAtRank(int rank)
                    {
                        if(rank > 0)
                            return null;
                        return new Pair(1.0, vocabulary.getIndex("</s>"));
                    }
                    public void setPosterior(double prob)
                    { }
                    public GenWidget choose(GenWidget widget)
                    { return widget; }

                    public GenWidget chooseWord(GenWidget widget, int word)
                    { return widget; }
                });
            }            
            //////////////////////////////////////////////////////////////////////////////////
            ArrayList<Object> list = new ArrayList(opts.ngramSize);
            for(int i = 0; i < opts.ngramSize - 1; i++) // Generate each word in this range using an LM
            {
                list.add(startSymbol);
            }
            // use the gold standard tree to guide record selection. If the gold standard tree
            // contains a rule not in the training set, then fall back to generating from the existing ruleset
            if(opts.fixRecordSelection && checkGoldTree())
            {
                try 
                {                    
                    list.add(genEdge(0, N, recordTree));
                } 
                catch(Exception e){LogInfo.error("Error: " + e + " " + ex.getName());}
            }
            else if(opts.fixNumSentences)
            {
                list.add(genEdge(0, ex.N(), indexer.getIndex("S"), opts.averageNumSentences));
            }
            else
                list.add(genEdge(0, ex.N(), indexer.getIndex("S")));
            if(!opts.useStopNode)
                list.add(endSymbol);
            this.hypergraph.addEdge(hypergraph.sumStartNode(), list);
        } // else
    }

    /**
     * Check if the input gold tree contains rules that are not in the original training set
     * @return 
     */
    private boolean checkGoldTree()
    {
        for(Iterator<Tree> it = recordTree.iterator(); it.hasNext(); )
        {
            Tree<String> subtree = it.next();
            if(Utils.countableRule(subtree)) // count only the binary rules
            {
                CFGRule rule = new CFGRule(subtree, indexer);
                if(!((Event3Model)model).containsCfgRule(rule))
                    return false;
            }
        }
        return true;
    }
    
    
    private boolean excludeCfgRule(int lhs)
    {
        boolean excluded = false;
        Event3Model m = ((Event3Model)model);
        String label = indexer.getObject(lhs);
        int eventTypeIndex = label.equals("none") ? m.none_t() : (m.getEventTypeNameIndexer().contains(label) ? m.getEventTypeNameIndexer().getIndex(label) : -1);
        if (eventTypeIndex != -1)
        {
            if((opts.allowNoneEvent && eventTypeIndex == m.none_t()) || ex.eventsByEventType.containsKey(eventTypeIndex))
                return false;
            else 
                return true;
        }
        if(cfgRules.get(lhs).isEmpty()) // already removed
            return true;
//        for(CFGRule candidateRule : m.getCfgCandidateRules(lhs).keySet())
        for(Iterator<CFGRule> it =  cfgRules.get(lhs).keySet().iterator(); it.hasNext();)
        {
            CFGRule candidateRule = it.next();
//            if(!excludedCfgRules.contains(candidateRule)) // we already removed this rule
            {
                final int rhs1 = candidateRule.getRhs1();
                final boolean isUnary = candidateRule.isUnary();                    
                if(excludeCfgRule(rhs1))
                {
                    excludedCfgRules.add(candidateRule);
                    it.remove();
                    excluded = true;
                    continue;
                }                        
                if(!isUnary)
                {
                    final int rhs2 = candidateRule.getRhs2();
                    if(excludeCfgRule(rhs2))
                    {
                        excludedCfgRules.add(candidateRule);
                        it.remove();
                        excluded = true;
                    }
                }
            } // if
        } // for
        return excluded;
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
        final int lhs = indexer.getIndex(tree.getLabelNoSpan());
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            // check if we are in a record leaf, or a pre-terminal.
            // In either case we treat them as equal, i.e., generate the record / field set
//            if (tree.getChildren().size() == 1 || tree.isLeaf())
            if (tree.isPreTerminal() || tree.isLeaf())
            {
                String label = tree.getLabelNoSpan();
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
                              int rhs = indexer.getIndex(children.get(0).getLabelNoSpan());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs));
                              public double getWeight()
                              {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { }
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
                              int rhs1 = indexer.getIndex(children.get(0).getLabelNoSpan());
                              int rhs2 = indexer.getIndex(children.get(1).getLabelNoSpan());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs1, rhs2));
                              public double getWeight()
                              {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { }
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
                              int rhs = indexer.getIndex(children.get(0).getLabelNoSpan());
                              int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs));
                              public double getWeight()
                              {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) { }
                              public Widget choose(Widget widget) {                          
                                  return widget;
                              }
                          }); 
                    }
                    else // binary trees only
                    {
                        int kStart, kEnd;
                        // respect minimum word span of each rhs non terminal if the grammar is non-recursive
                        if(opts.nonRecursiveGrammar)
                        {
                            int rhs1 = indexer.getIndex(children.get(0).getLabelNoSpan());
                            int rhs2 = indexer.getIndex(children.get(1).getLabelNoSpan());
                            int minWordsRhs1 = minWordsPerNonTerminal.get(rhs1);                                                 
                            int minWordsRhs2 = minWordsPerNonTerminal.get(rhs2);
                            kStart = start+minWordsRhs1;
                            kEnd = end - minWordsRhs2 + 1;
//                        for(int k = start+minWordsRhs1; k <= end - minWordsRhs2; k++)
                        }
                        else
                        {
                            kStart = start + 1;
                            kEnd = end;
                        }
                        for(int k = kStart; k < kEnd; k++)
                        {                                                
                            hypergraph.addEdge(node, genEdge(start, k, children.get(0)), 
                                                     genEdge(k, end, children.get(1)),
                              new Hypergraph.HyperedgeInfo<Widget>() {
                                  int rhs1 = indexer.getIndex(children.get(0).getLabelNoSpan());
                                  int rhs2 = indexer.getIndex(children.get(1).getLabelNoSpan());
                                  int indexOfRule = ((Event3Model)model).getCfgRuleIndex(new CFGRule(lhs, rhs1, rhs2));
                                  public double getWeight()
                                  {
                                      return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                                  }
                                  public void setPosterior(double prob) { }
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
    
    private Object genEdge(int start, int end, final int lhs)
    {
        Indexer eventTypeIndexer = ((Event3Model)model).getEventTypeNameIndexer();
        final CFGParams cfgParams = params.cfgParams;
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            String label = indexer.getObject(lhs);            
            int eventTypeIndex = label.equals("none") ? cfgParams.none_t : (eventTypeIndexer.contains(label) ? eventTypeIndexer.getIndex(label) : -1);
            // check if we are in a record leaf and the example contains events of this eventType (not always the case e.g., in ATIS), 
            // and generate the record / field set
            if (eventTypeIndex != -1 && (eventTypeIndex == cfgParams.none_t || ex.eventsByEventType.containsKey(eventTypeIndex)))
            {                
                hypergraph.addEdge(node, genRecord(start, end, eventTypeIndex));
            } // if            
            else
            {
//                final HashMap<CFGRule, Integer> candidateRules = ((Event3Model)model).getCfgCandidateRules(lhs);
                final HashMap<CFGRule, Integer> candidateRules = cfgRules.get(lhs);
                if(candidateRules != null)
                {
                    for(final Entry<CFGRule, Integer> candidateRule : candidateRules.entrySet()) // try to expand every rule with the same lhs
                    {
    //                    if(excludedCfgRules.contains(candidateRule.getKey()))
    //                        continue;
                        final int rhs1 = candidateRule.getKey().getRhs1();                    
                        final int indexOfRule =  candidateRule.getValue();
                        final boolean isUnary = candidateRule.getKey().isUnary();
                        final boolean isRootRule = opts.wordsPerRootRule ? ((Event3Model)model).isRootRule(candidateRule.getKey()): false;
                        if(isRootRule && get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) < opts.stage1.cfgThreshold)
                            continue;
    //                    else if(isRootRule)
    //                        System.out.println(candidateRule + " " + get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin));
                        if(isUnary) // unary trees
                        {
                            hypergraph.addEdge(node, genEdge(start, end, rhs1),
                              new Hypergraph.HyperedgeInfo<GenWidget>() {                              
                                  public double getWeight() {
                                      return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule) *
                                             (isRootRule ? get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) : 1.0);
                                  }
                                  public void setPosterior(double prob) {}
                                  public GenWidget choose(GenWidget widget) {
                                      if(opts.outputPcfgTrees)
                                          widget.addEdge(candidateRule.getKey());
                                      return widget;
                                  }
                              }); 
                        }
                        else // binary trees only
                        {                                                
                            final int rhs2 = candidateRule.getKey().getRhs2();
                            int kStart, kEnd;
                            // respect minimum word span of each rhs non terminal if the grammar is non-recursive
                            if(opts.nonRecursiveGrammar)
                            {
                                int minWordsRhs1 = minWordsPerNonTerminal.get(rhs1);
                                int minWordsRhs2 = minWordsPerNonTerminal.get(rhs2);
                                // break and cross on (hypothetical) punctuation
                                int nextBoundary = containsSentence(indexer, rhs1, rhs2) ? end(start+1, end) : end;
                                kStart = start+minWordsRhs1;
                                kEnd = nextBoundary - minWordsRhs2 + 1;
      //                        for(int k = start+minWordsRhs1; k <= nextBoundary - minWordsRhs2; k++)d
                            }
                            else
                            {
                                kStart = start + 1;
                                kEnd = end;
                            }
                            for(int k = kStart; k < kEnd ; k++)
                            {
                                hypergraph.addEdge(node, genEdge(start, k, rhs1), genEdge(k, end, rhs2),
                                  new Hypergraph.HyperedgeInfo<GenWidget>() {                                      
                                      public double getWeight() {
                                          return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule) *
                                             (isRootRule ? get(cfgParams.getWordsPerRootRule()[indexOfRule], docLengthBin) : 1.0);
                                      }
                                      public void setPosterior(double prob) {}
                                      public GenWidget choose(GenWidget widget) {
                                          if(opts.outputPcfgTrees)
                                            widget.addEdge(candidateRule.getKey());
                                          return widget;
                                      }
                                  });
                            } // for                        
                        } // else
                    } // for
                } // if
            } // else
        } // if
        return node;
    }
    
    private Object genEdge(int start, int end, final int lhs, int numSentences)
    {
        Indexer eventTypeIndexer = ((Event3Model)model).getEventTypeNameIndexer();
        final CFGParams cfgParams = params.cfgParams;
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            String label = indexer.getObject(lhs);            
            int eventTypeIndex = label.equals("none") ? cfgParams.none_t : (eventTypeIndexer.contains(label) ? eventTypeIndexer.getIndex(label) : -1);
            // check if we are in a record leaf and the example contains events of this eventType (not always the case e.g., in ATIS), 
            // and generate the record / field set
            if (eventTypeIndex != -1 && (eventTypeIndex == cfgParams.none_t || ex.eventsByEventType.containsKey(eventTypeIndex)))
            {                
                hypergraph.addEdge(node, genRecord(start, end, eventTypeIndex));
            } // if
            else
            {
                final HashMap<CFGRule, Integer> candidateRules = ((Event3Model)model).getCfgCandidateRules(lhs);
                for(final Entry<CFGRule, Integer> candidateRule : candidateRules.entrySet()) // try to expand every rule with the same lhs
                {
                    final int rhs1 = candidateRule.getKey().getRhs1();                    
                    final int indexOfRule =  candidateRule.getValue();
                    final boolean isUnary = candidateRule.getKey().isUnary();
                    if(isUnary) // unary trees
                    {
                        hypergraph.addEdge(node, genEdge(start, end, rhs1),
                          new Hypergraph.HyperedgeInfo<GenWidget>() {                              
                              public double getWeight() {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) {}
                              public GenWidget choose(GenWidget widget) {
                                  if(opts.outputPcfgTrees)
                                      widget.addEdge(candidateRule.getKey());
                                  return widget;
                              }
                          }); 
                    }
                    else // binary trees only
                    {                                                
                        final int rhs2 = candidateRule.getKey().getRhs2();
                        // respect minimum word span of each rhs non terminal
                        int minWordsRhs1 = minWordsPerNonTerminal.get(rhs1);                                                 
                        int minWordsRhs2 = minWordsPerNonTerminal.get(rhs2);                        
                        // break and cross on (hypothetical) punctuation                            
                        int nextBoundary = containsSentence(indexer, rhs1, rhs2) ? end(start+1, end) : end;
                        for(int k = start+minWordsRhs1; k <= nextBoundary - minWordsRhs2; k++)
                        {
                            hypergraph.addEdge(node, genEdge(start, k, rhs1), genEdge(k, end, rhs2),
                              new Hypergraph.HyperedgeInfo<GenWidget>() {                                      
                                  public double getWeight() {
                                      return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                                  }
                                  public void setPosterior(double prob) {}
                                  public GenWidget choose(GenWidget widget) {
                                      if(opts.outputPcfgTrees)
                                        widget.addEdge(candidateRule.getKey());
                                      return widget;
                                  }
                              });
                        } // for                        
                    } // else
                } // for
            } // else
        } // if
        return node;
    }

    private Map<Integer, HashMap<CFGRule, Integer>> cloneCfgRules(Map<Integer, HashMap<CFGRule, Integer>> cfgRules)
    {
        Map<Integer, HashMap<CFGRule, Integer>> clone = new HashMap<Integer, HashMap<CFGRule, Integer>>();
        for(Entry<Integer, HashMap<CFGRule, Integer>> e : cfgRules.entrySet())
        {
            clone.put(e.getKey(), new HashMap<CFGRule, Integer>(e.getValue()));            
        }
        return clone;
    }
}