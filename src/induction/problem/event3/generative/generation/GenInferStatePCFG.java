package induction.problem.event3.generative.generation;

import fig.basic.Indexer;
import induction.problem.event3.params.Params;
import induction.Hypergraph;
import induction.ngrams.NgramModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CFGNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.CFGParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author konstas
 */
public class GenInferStatePCFG extends GenInferState
{   
    Indexer<String> indexer;
    
    public GenInferStatePCFG(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
        indexer = model.getRulesIndexer();
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
            list.add(genEdge(0, ex.N(), indexer.getIndex("S")));
            if(!opts.useStopNode)
                list.add(endSymbol);
            this.hypergraph.addEdge(hypergraph.sumStartNode(), list,
                new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {return 1;}
                    public void setPosterior(double prob) {}
                    public Widget choose(Widget widget) {return widget;}
                });
        } // else
    }

    private Object genEdge(int start, int end, final int lhs)
    {
        Indexer eventTypeIndxer = ((Event3Model)model).getEventTypeNameIndexer();
        final CFGParams cfgParams = params.cfgParams;
        CFGNode node = new CFGNode(start, end, lhs);
        
        if(hypergraph.addSumNode(node))
        {
            String label = indexer.getObject(lhs);            
            int eventTypeIndex = label.equals("none") ? cfgParams.none_t : (eventTypeIndxer.contains(label) ? eventTypeIndxer.getIndex(label) : -1);
            // check if we are in a record leaf and the example contains events of this eventType (not always the case e.g., in ATIS), 
            // and generate the record / field set
            if (eventTypeIndex != -1 && (eventTypeIndex == cfgParams.none_t || ex.eventsByEventType.containsKey(eventTypeIndex)))
            {                
                hypergraph.addEdge(node, genRecord(start, end, eventTypeIndex));
            } // if
            else
            {
                final HashMap<CFGRule, Integer> candidateRules = ((Event3Model)model).getCfgCandidateRules(lhs);
                for(Entry<CFGRule, Integer> candidateRule : candidateRules.entrySet()) // try to expand every rule with the same lhs
                {
                    final int rhs1 = candidateRule.getKey().getRhs1();                    
                    final int indexOfRule =  candidateRule.getValue();
                    final boolean isUnary = candidateRule.getKey().isUnary();
                    if(isUnary) // unary trees
                    {
                        hypergraph.addEdge(node, genEdge(start, end, rhs1),
                          new Hypergraph.HyperedgeInfo<Widget>() {                              
                              public double getWeight() {
                                  return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                              }
                              public void setPosterior(double prob) {}
                              public Widget choose(Widget widget) {
                                  return widget;
                              }
                          }); 
                    }
                    else // binary trees only
                    {
                        final int rhs2 = candidateRule.getKey().getRhs2();                        
                        // break on and cross (hypothetical) punctuation
                        int nextBoundary = containsSentence(indexer, rhs1, rhs2) ? end(start, end)+1  : end;                                                                                      
                        for(int k = start+1; k < nextBoundary; k++)
                        {
                            hypergraph.addEdge(node, genEdge(start, k, rhs1), genEdge(k, end, rhs2),
                              new Hypergraph.HyperedgeInfo<Widget>() {                                      
                                  public double getWeight() {
                                      return get(cfgParams.getCfgRulesChoices().get(lhs), indexOfRule);
                                  }
                                  public void setPosterior(double prob) {}
                                  public Widget choose(Widget widget) {
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
}