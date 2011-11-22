package induction.problem.event3.discriminative;

import edu.uci.ics.jung.graph.Graph;
import fig.basic.StopWatchSet;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.NumFieldParams;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.Hypergraph;
import induction.Hypergraph.HyperpathResult;
import induction.ngrams.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.ProbVec;
import induction.problem.event3.Constants;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.TrackParams;

/**
 * This class describes a hypregraph representation of the problem. The main
 * difference from the Generative Model is the calculation of the weights in
 * the various edges, via getWeight() method.
 * <code>Params params</code> contain the perceptron weight vector w.
 * <br/>
 * We allow for two different weight calculations: 
 * a) using the original generative baseline inferState
 * b) by computing w.f(e) at each edge:
 * w.f(e) = 
 *      perceptron_weight * 1 (omitted, since it is equivalent to the count of 
 *          the alignment inferState rule, which is always included in f(e)) + 
 *      ... (other features) + 
 *      logP(baseline)
 * 
 * @author konstas
 */
public class DiscriminativeInferStateOracle extends DiscriminativeInferState
{        
    public DiscriminativeInferStateOracle(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, boolean useKBest)
    {
        super(model, ex, params, counts, ispec, ngramModel, useKBest);
    }

    public DiscriminativeInferStateOracle(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, boolean useKBest, Graph graph)
    {
        super(model, ex, params, counts, ispec, ngramModel, useKBest, graph);
    }   
    
    @Override
    protected void initInferState(AModel model)
    {
        
        super.initInferState(model);
        // used for oracle only
        words = ex.getText();
        nums = new int[words.length];
        for(int w = 0; w < nums.length; w++)
        {
            nums[w] = Constants.str2num(Event3Model.wordToString(words[w]));
        }
        labels = ex.getLabels();        
    }                    

    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {        
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
                    double baseParam;
                    public double getWeight()
                    {
                       baseParam = get(params.genericLabelChoices, label);
                        return baseParam;
                    }
                    public void setPosterior(double prob)
                    { }
                    public Widget choose(Widget widget)
                    {
                        Feature[] featuresArray = {new Feature(params.genericLabelChoices, label)};
                        increaseCounts(featuresArray, normalisedLog(baseParam));
                        return widget;
                    }
                });
            } // for
        } // if
        hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).boundary_t()),
                           new Hypergraph.HyperedgeInfo<Widget>()
        {
            public double getWeight()
            {
                return 1;
            }
            public void setPosterior(double prob)
            { }
            public Widget choose(Widget widget)
            {
                return widget;
            }
        });       
    }
    
    @Override
    public void doInference()
    {
        HyperpathResult result;        
        StopWatchSet.begin("oracle 1-best Viterbi");
        result = hypergraph.oracleOneBestViterbi(newWidget(), opts.initRandom);                    
        StopWatchSet.end();
        if(useKBest)
        {
            // compute ngram features (we can do it  in the end, since we have created the resulting output text)
//            increaseNgramLMCounts(ex.getText());
        }        
        bestWidget = (Widget) result.widget;        
//            System.out.println(bestWidget);        
    }
    
    @Override
    public void updateCounts()
    {
        // Do nothing, we don't use or update counts in this class
    }       
    
    private void addNumEdge(final int method, final NumFieldValueNode node, 
                            final ProbVec weightProbVec, final ProbVec baseProbVec)
    {
        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
            double baseParam;
            public double getWeight() {
                baseParam = get(baseProbVec, method);                
                return baseParam;
            }                    
            public void setPosterior(double prob) { }
            public Widget choose(Widget widget) {                        
                Feature[] featuresArray = {new Feature(weightProbVec, method)};
                increaseCounts(featuresArray, normalisedLog(baseParam));
                return widget;
            }                    
        });
    }
    
    @Override
    protected Object genNumFieldValue(final int i, final int c, final int event, final int field, final int v)
    {
        if (nums[i] == Constants.NaN)
            return hypergraph.invalidNode; // Can't generate if not a number
        NumFieldValueNode node = new NumFieldValueNode(i, c, event, field);           
        if (hypergraph.addSumNode(node))
        {           
            final NumFieldParams modelFParams = getNumFieldParams(event, field);
            final NumFieldParams baseFParams = getBaselineNumFieldParams(event, field);
            final ProbVec weightProbVec = modelFParams.methodChoices;
            final ProbVec baseProbVec = baseFParams.methodChoices;
            
            if (v == nums[i]) // M_IDENTITY
                addNumEdge(Parameters.M_IDENTITY, node, weightProbVec, baseProbVec);
            if (roundUp(v) == nums[i]) // M_ROUNDUP
                addNumEdge(Parameters.M_ROUNDUP, node, weightProbVec, baseProbVec);
            if (roundDown(v) == nums[i]) // M_ROUNDDOWN
                addNumEdge(Parameters.M_ROUNDDOWN, node, weightProbVec, baseProbVec);
            if (roundClose(v) == nums[i]) // M_ROUNDCLOSE
                addNumEdge(Parameters.M_ROUNDCLOSE, node, weightProbVec, baseProbVec);
            final int noise = nums[i] - v; // M_NOISEUP and M_NOISEDOWN
            if(noise > 0)
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    double baseParam; final int method = Parameters.M_NOISEUP;
                    public double getWeight() {                        
                        baseParam = get(baseProbVec, method) * 0.5 *
                                   Math.pow(get(baseFParams.rightNoiseChoices,
                                   Parameters.S_CONTINUE), noise-1) *
                                   get(baseFParams.rightNoiseChoices, Parameters.S_STOP);                        
                        return baseParam;
                    }
                    public void setPosterior(double prob) {}
                    public Widget choose(Widget widget) {
                        widget.getNumMethods()[c][i] = method;
                        Feature[] featuresArray = {new Feature(weightProbVec, method), 
                                                   new Feature(modelFParams.rightNoiseChoices, Parameters.S_STOP),
                                                   new Feature(modelFParams.rightNoiseChoices, Parameters.S_CONTINUE)};
                        increaseCounts(featuresArray, normalisedLog(baseParam));
//                        increaseCount(new Feature(modelFParams.rightNoiseChoices, Parameters.S_CONTINUE), noise-1);                                                
                        return widget;
                    }
                });
            } // if
            else
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    double baseParam; final int method = Parameters.M_NOISEDOWN;
                    public double getWeight() {
                        baseParam = get(baseProbVec, method) *
                                   Math.pow(get(baseFParams.leftNoiseChoices,
                                   Parameters.S_CONTINUE), -noise-1) *
                                   get(baseFParams.leftNoiseChoices, Parameters.S_STOP);                                                
                        return baseParam;
                    }
                    public void setPosterior(double prob) {}
                    public Widget choose(Widget widget) {
                        widget.getNumMethods()[c][i] = method;
                        Feature[] featuresArray = {new Feature(weightProbVec, method), 
                                                   new Feature(modelFParams.leftNoiseChoices, Parameters.S_STOP),
                                                   new Feature(modelFParams.leftNoiseChoices, Parameters.S_CONTINUE)};
                        increaseCounts(featuresArray, normalisedLog(baseParam));
//                        increaseCount(new Feature(modelFParams.leftNoiseChoices, Parameters.S_CONTINUE), -noise-1);
                        return widget;
                    }
                });
            } // else            
        } // if (hypergraph.addSumNode(node))
        return node;
    }   
    
    @Override
    protected CatFieldValueNode genCatFieldValueNode(final int i, int c, final int event, final int field)
    {        
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        if(hypergraph.addSumNode(node))
        {
            final CatFieldParams modelFParams = getCatFieldParams(event, field);
            final CatFieldParams baseFParams = getBaselineCatFieldParams(event, field);
            // Consider generating words(i) from category v
            final int v = getValue(event, field);            
            final int w = words[i];
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                double baseParam;
                public double getWeight() {
                    baseParam = get(baseFParams.emissions[v], w);                    
                    return baseParam;
                }            
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) {                                 
                    Feature[] featuresArray = {new Feature(modelFParams.emissions[v], w)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;   
                }            
            });
        }
        return node;
    }
  
    // Generate word at position i with event e and field f
    @Override
    protected WordNode genWord(final int i, final int c, int event, final int field)
    {
        WordNode node = new WordNode(i, c, event, field);
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final EventTypeParams modelEventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams baseEventTypeParams = baseline.eventTypeParams[eventTypeIndex];
        final int w = words[i];
        if(hypergraph.addSumNode(node))
        {
            if(field == modelEventTypeParams.none_f)
            {                
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    double baseParam;
                    public double getWeight() {
                        baseParam = get(baseEventTypeParams.noneFieldEmissions, w);                        
                        return baseParam;
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget)
                    {
                        Feature[] featuresArray = {new Feature(modelEventTypeParams.noneFieldEmissions, w)};
                        increaseCounts(featuresArray, normalisedLog(baseParam));
                        return widget;
                    }
                });
            } // if
            else
            {
                // G_FIELD_VALUE: generate based on field value
                hypergraph.addEdge(node, genFieldValue(i, c, event, field),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                double baseParam;
                public double getWeight() {
                    baseParam = get(baseEventTypeParams.genChoices[field], 
                            Parameters.G_FIELD_VALUE);                    
                    return baseParam;  
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    Feature[] featuresArray = {
                        new Feature(modelEventTypeParams.genChoices[field], 
                        Parameters.G_FIELD_VALUE)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
                });
                // G_FIELD_GENERIC: generate based on event type
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    double baseParam;
                    public double getWeight() {
                        baseParam = get(baseEventTypeParams.genChoices[field], 
                                Parameters.G_FIELD_GENERIC) * get(baseline.genericEmissions, w);                        
                        return baseParam;
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {                            
                        widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                        Feature[] featuresArray = {
                            new Feature(modelEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC),
                            new Feature(params.genericEmissions, w)
                        };
                        increaseCounts(featuresArray, normalisedLog(baseParam));
                        return widget;
                    }                    
                    });
            } // else
        }
        return node;
    }     
    
    @Override
    protected WordNode genNoneWord(final int i, final int c)
    {
        WordNode node = new WordNode(i, c, ((Event3Model)model).none_t(), -1);
        if(hypergraph.addSumNode(node))
        {
            // add hyperedge for each word. COSTLY!            
            final int w = words[i];
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                double baseParam; ProbVec weightProbVec;
                public double getWeight() 
                {
                    baseParam = get(baseline.trackParams[c].getNoneEventTypeEmissions(), w);
                    weightProbVec = params.trackParams[c].getNoneEventTypeEmissions();
                    return baseParam;
                }                
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) 
                {                                             
                    Feature[] featuresArray = {new Feature(params.trackParams[c].getNoneEventTypeEmissions(), w)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;
                }                
            });
        }
        return node;
    }

    @Override
    protected StopNode genStopNode(int i, final int t0, final TrackParams modelCParams, 
                                                        final TrackParams baseCParams)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                double baseParam; 
                int index = modelCParams.boundary_t;
                public double getWeight() {
                    baseParam = get(baseCParams.getEventTypeChoices()[t0], index);
                    return baseParam; 
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {                    
                    Feature[] featuresArray = {new Feature(modelCParams.getEventTypeChoices()[index], index)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;
                }                                
            });
        } // if
        return node;
    }   
}