package induction.problem.event3.discriminative;

import induction.problem.event3.generative.generation.*;
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
import induction.problem.Pair;
import induction.problem.ProbVec;
import induction.problem.event3.Constants;
import induction.problem.event3.Event;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.Node;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.TrackParams;
import java.util.Arrays;
import java.util.HashMap;

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
 *      pecreptron_weight * 1 (omitted, since it is equivalent to the count of 
 *          the alignment inferState rule, which is always included in f(e)) + 
 *      ... (other features) + 
 *      logP(baseline)
 * 
 * @author konstas
 */
public class DiscriminativeInferStateOracle extends DiscriminativeInferState
{        
    public DiscriminativeInferStateOracle(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);        
    }

    public DiscriminativeInferStateOracle(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, Graph graph)
    {
        super(model, ex, params, counts, ispec, ngramModel, graph);
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
        
    protected int[] newMatrixOne()
    {
        int[] out = new int[N];        
        Arrays.fill(out, -1);
        return out;
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
                               newMatrixOne(),
                               ((Event3Model)model).eventTypeAllowedOnTrack, eventTypeIndices);
    }    

    /**
     * Gets a node emitting a terminal directly from the hypergraph viterbi search
     * and returns the corresponding position of the oracle in its edges' list. 
     * It relies on the fact that terminal hyperedges are added in the same order 
     * (from the word vocabulary or according to the method they are produced in the case of numbers)
     * @param node
     * @return 
     */
    public int getOracleEdgeIndex(Node node)
    {
        int i = node.getI();
        if(node instanceof NumFieldValueNode)
        {
            if (nums[i] == Constants.NaN)
                return -1;
            NumFieldValueNode numNode = (NumFieldValueNode)node;
            int numValue = getValue(numNode.getEvent(), numNode.getField());
            if (numValue == nums[i])
                return 0;
            if (roundUp(numValue) == nums[i])
                return 1;
            if (roundDown(numValue) == nums[i])
                return 2;
            if (roundClose(numValue) == nums[i])
                return 3;
            int noise = nums[i] - numValue;
            if (noise > 0)
                return 4;
            else
                return 5;
        }
        else if(node instanceof StopNode || node == startSymbol)
        {
            return 0;
        }
        return words[i];
    }       

    @Override
    public void doInference()
    {
        HyperpathResult result;        
        StopWatchSet.begin("oracle 1-best Viterbi");
        result = hypergraph.oracleOneBestViterbi(newWidget(), opts.initRandom);                    
        StopWatchSet.end();
        bestWidget = (Widget) result.widget;
//            System.out.println(bestWidget);        
    }
    
    @Override
    public void updateCounts()
    {
        // Do nothing, we don't use or update counts in this class
    }       
    
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
            if (v == nums[i]) // M_IDENTITY
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                    double baseScore; int method = Parameters.M_ROUNDUP;
                    public double getWeight() {
                        double baseParam = get(baseFParams.methodChoices, method);
                        baseScore = getBaselineScore(baseParam); 
                        return baseParam;
                    }
                    public Pair getWeightLM(int rank) {
                        if(rank > 0)
                            return null;
                        return new Pair(get(modelFParams.methodChoices,
                                            method), vocabulary.getIndex("<num>"));
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {
                        widget.getNumMethods()[c][i] = method;
                        widget.getNums()[i] = roundUp(v);
                        Feature[] featuresArray = {new Feature(weightProbVec, method)};
                        increaseCounts(featuresArray, baseScore);
                        return widget;
                    }
                    public GenWidget chooseLM(GenWidget widget, int word)
                    {
                        return choose(widget);
                    }
                });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseScore; int method = Parameters.M_ROUNDDOWN;
                public double getWeight() {
                    double baseParam = get(baseFParams.methodChoices, method);
                    baseScore = getBaselineScore(baseParam); 
                    return calculateOracle ?
                            baseParam : getCount(weightProbVec, method) + baseScore;
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(modelFParams.methodChoices,
                                        method), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = roundDown(v);
                    Feature[] featuresArray = {new Feature(weightProbVec, method)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseScore; int method = Parameters.M_ROUNDCLOSE;
                public double getWeight() {
                    double baseParam = get(baseFParams.methodChoices, method);
                    baseScore = getBaselineScore(baseParam); 
                    return calculateOracle ?
                            baseParam : getCount(weightProbVec, method) + baseScore;
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(modelFParams.methodChoices,
                                        method), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDCLOSE;
                    widget.getNums()[i] = roundClose(v);
                    Feature[] featuresArray = {new Feature(weightProbVec, method)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseScore; int method = Parameters.M_IDENTITY;
                public double getWeight() {
                    double baseParam = get(baseFParams.methodChoices, method);
                    baseScore = getBaselineScore(baseParam); 
                    return calculateOracle ?
                            baseParam : getCount(weightProbVec, method) + baseScore;
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(modelFParams.methodChoices,
                                        method), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = v;
                    Feature[] featuresArray = {new Feature(weightProbVec, method)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseScore; int method = Parameters.M_NOISEUP;
                final double CONT = get(modelFParams.rightNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(modelFParams.rightNoiseChoices, Parameters.S_STOP);
                final int NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    double baseParam = get(baseFParams.methodChoices, method);
                    baseScore = getBaselineScore(baseParam);
                    return calculateOracle ?
                            baseParam : getCount(weightProbVec, method) + baseScore;
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(modelFParams.methodChoices, method),
                                    vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = NOISE_MINUS_ONE + 1 + v;
                    Feature[] featuresArray = {new Feature(weightProbVec, method)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseScore; int method = Parameters.M_NOISEDOWN;
                final double CONT = get(modelFParams.leftNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(modelFParams.leftNoiseChoices, Parameters.S_STOP);
                final int MINUS_NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    double baseParam = get(baseFParams.methodChoices, method);
                    baseScore = getBaselineScore(baseParam);                    
                    return calculateOracle ?
                            baseParam : getCount(weightProbVec, method) + baseScore;
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(modelFParams.methodChoices, method),
                                    vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    Feature[] featuresArray = {new Feature(weightProbVec, method)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
        } // if (hypergraph.addSumNode(node))
        return node;
    }   
    
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
                double baseScore;
                public double getWeight() {
                    double baseParam = get(baseFParams.emissions[v], w);
                    baseScore = getBaselineScore(baseParam);
                    return baseParam;
                }            
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) {                                 
                    Feature[] featuresArray = {new Feature(modelFParams.emissions[v], w)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;   
                }            
            });
        }
        return node;
    }
  
    // Generate word at position i with event e and field f
    protected WordNode genWord(final int i, final int c, int event, final int field)
    {
        WordNode node = new WordNode(i, c, event, field);
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final EventTypeParams modelEventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams baseEventTypeParams = baseline.eventTypeParams[eventTypeIndex];

        if(hypergraph.addSumNode(node))
        {
            if(field == modelEventTypeParams.none_f)
            {
                // add hyperedge for each word. COSTLY!
                for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
                { 
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseScore; ProbVec weightProbVec;
                        public double getWeight() {
                            double baseParam = get(baseEventTypeParams.noneFieldEmissions, w);
                            baseScore = getBaselineScore(baseParam);
                            weightProbVec = modelEventTypeParams.noneFieldEmissions;
                            return calculateOracle ?
                                    baseParam : getCount(weightProbVec, w) + baseScore;
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget)
                        {                            
                            return chooseLM(widget, w);
                        }
                        public Pair getWeightLM(int rank) // used for k-best
                        {
                             return getAtRank(modelEventTypeParams.noneFieldEmissions, rank);
                        }
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            widget.getText()[i] = word;
                            Feature[] featuresArray = {new Feature(weightProbVec, w)};
                            increaseCounts(featuresArray, baseScore);
                            return widget;
                        }
                    });
                } // for
            } // if
            else
            {
                // G_FIELD_VALUE: generate based on field value
                hypergraph.addEdge(node, genFieldValue(i, c, event, field),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                double baseScore; ProbVec weightProbVec;
                public double getWeight() {
                    double baseParam = get(baseEventTypeParams.genChoices[field], 
                            Parameters.G_FIELD_VALUE);
                    baseScore = getBaselineScore(baseParam);
                    weightProbVec = modelEventTypeParams.genChoices[field];
                    return calculateOracle ?
                            baseParam :
                            getCount(weightProbVec, Parameters.G_FIELD_VALUE) +
                            baseScore;  
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    Feature[] featuresArray = {new Feature(weightProbVec, 
                            Parameters.G_FIELD_VALUE)};
                    increaseCounts(featuresArray, baseScore);
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
                });
                // G_FIELD_GENERIC: generate based on event type               
                // add hyperedge for each word. COSTLY!
                for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
                {
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseScore;
                        public double getWeight() {
                            double baseParam = get(baseEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC) * get(baseline.genericEmissions, w);
                            baseScore = getBaselineScore(baseParam);
                            return calculateOracle ?
                                    baseParam :
                                    getCount(modelEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC) + getCount(params.genericEmissions, w) +
                                    baseScore;
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {                            
                            return chooseLM(widget, w);
                        }
                        public Pair getWeightLM(int rank) // used for k-best
                        {
                            Pair p =  getAtRank(baseline.genericEmissions, rank);
                            p.value *= get(baseEventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
                            return p;
                        }
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            widget.getText()[i] = word;
                            widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                            Feature[] featuresArray = {
                                new Feature(modelEventTypeParams.genChoices[field], 
                                        Parameters.G_FIELD_GENERIC),
                                new Feature(params.genericEmissions, w)                                
                            };
                            increaseCounts(featuresArray, baseScore);
                            return widget;
                        }
                        });
                } // for
            } // else
        }
        return node;
    }     
    
    protected WordNode genNoneWord(final int i, final int c)
    {
        WordNode node = new WordNode(i, c, ((Event3Model)model).none_t(), -1);
        if(hypergraph.addSumNode(node))
        {
            // add hyperedge for each word. COSTLY!            
            final int w = words[i];
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                double baseScore; ProbVec weightProbVec;
                public double getWeight() 
                {
                    double baseParam = get(baseline.trackParams[c].getNoneEventTypeEmissions(), w);
                    baseScore = getBaselineScore(baseParam);
                    weightProbVec = params.trackParams[c].getNoneEventTypeEmissions();
                    return baseParam;
                }                
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) 
                {                                             
                    Feature[] featuresArray = {new Feature(params.trackParams[c].getNoneEventTypeEmissions(), w)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }                
            });
        }
        return node;
    }

    protected StopNode genStopNode(int i, final int t0, final TrackParams modelCParams, 
                                                        final TrackParams baseCParams)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                double baseScore; 
                int index = modelCParams.boundary_t;
                public double getWeight() {
                    double baseParam;                    
                    baseParam = get(baseCParams.getEventTypeChoices()[t0], index);
                    baseScore = getBaselineScore(baseParam);    
                    return baseParam; 
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {                    
                    Feature[] featuresArray = {new Feature(modelCParams.getEventTypeChoices()[index], index)};
                    increaseCounts(featuresArray, baseScore);
                    return widget;
                }                                
            });
        } // if
        return node;
    }   
}