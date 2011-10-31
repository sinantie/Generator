package induction.problem.event3.discriminative;

import induction.problem.event3.generative.generation.*;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.Indexer;
import fig.basic.StopWatchSet;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.NumFieldParams;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.Hypergraph;
import induction.Hypergraph.HyperpathResult;
import induction.ngrams.NgramModel;
import induction.Options;
import induction.Utils;
import induction.problem.AModel;
import induction.problem.AParams;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.ProbVec;
import induction.problem.event3.CatField;
import induction.problem.event3.Constants;
import induction.problem.event3.Event;
import induction.problem.event3.Event3InferState;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.NumField;
import induction.problem.event3.Widget;
import induction.problem.event3.discriminative.params.DiscriminativeParams;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.nodes.FieldsNode;
import induction.problem.event3.nodes.Node;
import induction.problem.event3.nodes.NoneEventWordsNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.TrackParams;
import java.util.ArrayList;
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
public class DiscriminativeInferState extends Event3InferState
{
    Graph graph;
    //public static final int EXTRA_VOCABULARY_SYMBOLS = 5;
    protected NgramModel ngramModel;
    protected Indexer<String> vocabulary;
    /**
     * baseline inferState parameters (contains probabilities not logs)
     */
    Params baseline; 
    /**
     * the baseline inferState feature
     */
    Feature baselineFeature;
    /** 
     * set true when we are doing the calculation of f(y+), using the original baseline inferState's
     * parameters
     * set false when we are calculating w*f(y*), during the reranking stage
     */     
    boolean calculateOracle = false; 
    /**
     * map of the count of features extracted during the Viterbi search.
     * It has to be set first to the corresponding map (inferState under train, or oracle)
     * before doing the recursive call to extract D_1 (top derivation)
     */
    protected HashMap<Feature, Double> features;
    WordNode startSymbol = new WordNode(-1, 0, -1, -1);
    
    public DiscriminativeInferState(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec);
        this.ngramModel = ngramModel;
        this.baseline = model.getBaselineModelParams();
        this.baselineFeature = new Feature(((DiscriminativeParams)params).baselineWeight, 0);               
    }

    public DiscriminativeInferState(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, Graph graph)
    {
        this(model, ex, params, counts, ispec, ngramModel);
        this.graph = graph;
    }

    public void setFeatures(HashMap features)
    {
        this.features = features;
    }

    public void setCalculateOracle(boolean calculateOracle)
    {
        this.calculateOracle = calculateOracle;
    }

    public boolean isCalculateOracle()
    {
        return calculateOracle;
    }
    
    protected void increaseCounts(Feature[] ar, double baseScore)
    {
        for(Feature f : ar)
        {
            increaseCount(f, 1);            
        }
        increaseCount(baselineFeature, baseScore);
    }
    
    protected void increaseCount(Feature feat, double increment)
    {
        Double oldCount = features.get(feat);
        if(oldCount != null)
            features.put(feat, oldCount + increment);
        else
            features.put(feat, increment);
    }
    
    @Override
    protected void initInferState(AModel model)
    {
        
        super.initInferState(model);

        this.vocabulary = ((Event3Model)model).getWordIndexer();
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
    
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {        
        // setup hypergraph preliminaries
        if(opts.modelType == Options.ModelType.generate)
            hypergraph.setupForGeneration(this, opts.debug, opts.modelType, true, opts.kBest, ngramModel, opts.ngramSize,
                    opts.reorderType, opts.allowConsecutiveEvents,
                    opts.oracleReranker,
                    /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
                    -1,
                    -1,
                    -1,
                    -1,
//                    vocabulary.getIndex("<num>"),
//                    vocabulary.getIndex("ELIDED_SYMBOL"),
//                    vocabulary.getIndex("<s>"),
//                    vocabulary.getIndex("</s>"),
                    opts.numAsSymbol,
                    vocabulary, ex, graph);
        else
        {
            hypergraph.setInferState(this);
            hypergraph.setNumbersAsSymbol(opts.numAsSymbol);
        }
        hypergraph.addSumNode(startSymbol);        
        this.hypergraph.addEdge(startSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
        {
            public double getWeight()
            { 
                return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking            
            }
            public Pair getWeightLM(int rank)
            {
                if(rank > 0)
                    return null;
                return new Pair(1.0, vocabulary.getIndex("<s>"));
            }
            public void setPosterior(double prob)
            { }
             public GenWidget choose(GenWidget widget)
            { return widget; }

            public GenWidget chooseLM(GenWidget widget, int word)
            { return widget; }
        });
        ArrayList<Object> list = new ArrayList(opts.ngramSize);
        for(int i = 0; i < opts.ngramSize - 1; i++) // Generate each word in this range using an LM
        {
            list.add(startSymbol);
        }
        list.add(genEvents(0, ((Event3Model)model).boundary_t()));
        this.hypergraph.addEdge(hypergraph.sumStartNode(), list,
                       new Hypergraph.HyperedgeInfo<Widget>()
        {
            public double getWeight()
            {
                return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
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
        if(calculateOracle)
        {
            StopWatchSet.begin("oracle 1-best Viterbi");
            result = hypergraph.oracleOneBestViterbi(newWidget(), opts.initRandom);            
        }
        else
        {
            StopWatchSet.begin("rerank 1-best Viterbi");
            result = hypergraph.rerankOneBestViterbi(newWidget(), opts.initRandom);
        }
        StopWatchSet.end();
        bestWidget = (Widget) result.widget;
//            System.out.println(bestWidget);
        if(!calculateOracle)
        {
            logVZ = result.logWeight;
            updateStats();
        }        
    }
    
    @Override
    public void updateCounts()
    {
        // Do nothing, we don't use or update counts in this class
    }
    
    protected double normalisedLog(double d)
    {
        return getLogProb(d) / ex.N();
    }
    
    protected double getBaselineScore(double baseWeight)
    {
        return getCount(((DiscriminativeParams)params).baselineWeight, 0) * normalisedLog(baseWeight);
//        return 1.0 * Math.abs(getLogProb(baseWeight));
//        return getCount(((DiscriminativeParams)params).baselineWeight, 0) * Math.log(baseWeight);
    }
    
    protected EventTypeParams getBaselineEventTypeParams(int event)
    {
        return baseline.eventTypeParams[ex.events.get(event).getEventTypeIndex()];
    }
    
    protected NumFieldParams getBaselineNumFieldParams(int event, int field)
    {
        AParams p = getBaselineEventTypeParams(event).fieldParams[field];
        if(p instanceof NumFieldParams) return (NumFieldParams)p;
        throw Utils.impossible();
    }
    
    protected CatFieldParams getBaselineCatFieldParams(int event, int field)
    {
        AParams p = getBaselineEventTypeParams(event).fieldParams[field];
        if(p instanceof CatFieldParams) return (CatFieldParams)p;
        throw Utils.impossible();
    }
    
    protected Object genNumFieldValue(final int i, final int c, int event, int field)
    {
        return genNumFieldValue(i, c, event, field, getValue(event, field));
    }
    
    private void addNumEdge(final int method, final NumFieldValueNode node, 
                            final ProbVec weightProbVec, final ProbVec baseProbVec,
                            final int value, final int c, final int i)
    {
        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
            double baseParam;
            public double getWeight() {
                baseParam = get(baseProbVec, method);
                return getCount(weightProbVec, method) + getBaselineScore(baseParam); 
            }
            public Pair getWeightLM(int rank) {
                if(rank > 0)
                    return null;
                return new Pair(get(weightProbVec, method), vocabulary.getIndex("<num>"));
            }
            public void setPosterior(double prob) { }
            public GenWidget choose(GenWidget widget) {
                widget.getNumMethods()[c][i] = method;
                widget.getNums()[i] = value;
                Feature[] featuresArray = {new Feature(weightProbVec, method)};
                increaseCounts(featuresArray, normalisedLog(baseParam));
                return widget;
            }
            public GenWidget chooseLM(GenWidget widget, int word)
            {
                return choose(widget);
            }
        });
    }
    
    protected Object genNumFieldValue(final int i, final int c, final int event, final int field, final int v)
    {
        NumFieldValueNode node = new NumFieldValueNode(i, c, event, field);
        if (hypergraph.addSumNode(node))
        {
            // Consider generating nums(i) from v            
            final NumFieldParams modelFParams = getNumFieldParams(event, field);
            final NumFieldParams baseFParams = getBaselineNumFieldParams(event, field);            
            final ProbVec weightProbVec = modelFParams.methodChoices;
            final ProbVec baseProbVec = baseFParams.methodChoices;
            
            addNumEdge(Parameters.M_IDENTITY, node, weightProbVec, baseProbVec, v, c, i);
            addNumEdge(Parameters.M_ROUNDUP, node, weightProbVec, baseProbVec, roundUp(v), c, i);
            addNumEdge(Parameters.M_ROUNDDOWN, node, weightProbVec, baseProbVec, roundDown(v), c, i);
            addNumEdge(Parameters.M_ROUNDCLOSE, node, weightProbVec, baseProbVec, roundClose(v), c, i);
            
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseParam; int method = Parameters.M_NOISEUP;
                final double CONT = get(modelFParams.rightNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(modelFParams.rightNoiseChoices, Parameters.S_STOP);
                final int NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    baseParam = get(baseFParams.methodChoices, method);
                    return getCount(weightProbVec, method) + getBaselineScore(baseParam);
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
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseParam; int method = Parameters.M_NOISEDOWN;
                final double CONT = get(modelFParams.leftNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(modelFParams.leftNoiseChoices, Parameters.S_STOP);
                final int MINUS_NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    baseParam = get(baseFParams.methodChoices, method);
                    return getCount(weightProbVec, method) + getBaselineScore(baseParam);
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
                    increaseCounts(featuresArray, normalisedLog(baseParam));
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
    
    protected Object genCatFieldValueNode(final int i, int c, final int event, final int field)
    {
        final CatFieldParams modelFParams = getCatFieldParams(event, field);
        final CatFieldParams baseFParams = getBaselineCatFieldParams(event, field);
        // Consider generating words(i) from category v
        final int v = getValue(event, field);
        // (for generation only) in case the test set contains values that are not in the training set
        if (v >= modelFParams.emissions.length)
        {
            return hypergraph.invalidNode;
        }
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        if(hypergraph.addSumNode(node))
        {            
            // add hyperedge for each word. COSTLY!
            final int maxWordIndex = modelFParams.emissions[v].getCounts().length;
//            for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
            for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
            {
                final int w = wIter;
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseParam; ProbVec weightProbVec;
                public double getWeight() {
                    baseParam = get(baseFParams.emissions[v], w);
                    weightProbVec = modelFParams.emissions[v];
                    return calculateOracle ? baseParam : getCount(weightProbVec, w) + 
                            getBaselineScore(baseParam);
                }
                public Pair getWeightLM(int rank)
                {
                    return getAtRank(modelFParams.emissions[v], rank);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {                    
                    return chooseLM(widget, w);
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    Feature[] featuresArray = {new Feature(weightProbVec, w)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;
                }
                });
            }            
        }
        return node;
    }

    protected Object genFieldValue(int i, int c, int event, int field)
    {
        Field tempField = ex.events.get(event).getFields()[field];
        if(tempField instanceof NumField) return genNumFieldValue(i, c, event, field);
        else if(tempField instanceof CatField) return genCatFieldValueNode(i, c, event, field);
//        else if(tempField instanceof SymField) return genSymFieldValue(i, c, event, field);
//        else if(tempField instanceof StrField) return genStrFieldValue(i, c, event, field);
        else return Utils.impossible();
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
                final int maxWordIndex = modelEventTypeParams.noneFieldEmissions.getCounts().length;
//            for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
                for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                { 
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseParam; ProbVec weightProbVec;
                        public double getWeight() {
                            baseParam = get(baseEventTypeParams.noneFieldEmissions, w);
                            weightProbVec = modelEventTypeParams.noneFieldEmissions;
                            return calculateOracle ?
                                    baseParam : getCount(weightProbVec, w) + 
                                    getBaselineScore(baseParam);
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
                            increaseCounts(featuresArray, normalisedLog(baseParam));
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
                double baseParam; ProbVec weightProbVec;
                public double getWeight() {
                    baseParam = get(baseEventTypeParams.genChoices[field], 
                            Parameters.G_FIELD_VALUE);                    
                    weightProbVec = modelEventTypeParams.genChoices[field];
                    return calculateOracle ?
                            baseParam :
                            getCount(weightProbVec, Parameters.G_FIELD_VALUE) +
                            getBaselineScore(baseParam);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    Feature[] featuresArray = {new Feature(weightProbVec, 
                            Parameters.G_FIELD_VALUE)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
                });
                // G_FIELD_GENERIC: generate based on event type               
                // add hyperedge for each word. COSTLY!
                final int maxWordIndex = params.genericEmissions.getCounts().length;
//            for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
                for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                {
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseParam;
                        public double getWeight() {
                            baseParam = get(baseEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC) * get(baseline.genericEmissions, w);
                            return calculateOracle ?
                                    baseParam :
                                    getCount(modelEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC) + getCount(params.genericEmissions, w) +
                                    getBaselineScore(baseParam);
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
                            increaseCounts(featuresArray, normalisedLog(baseParam));
                            return widget;
                        }
                        });
                } // for
            } // else
        }
        return node;
    }     

    // Generate field f of event e from begin to end
    protected Object genField(final int begin, final int end, int c, int event, final int field)
    {
        FieldNode node = new FieldNode(begin, end, c, event, field);
        if(opts.binariseAtWordLevel)
        {
            if (begin == end)
            {
                return hypergraph.endNode;
            }
            if(hypergraph.addSumNode(node))
            {
                hypergraph.addEdge(node,
                                   genWord(begin, c, event, field),
                                   genField(begin + 1, end, c, event, field),
                                   new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
        }
        else
        {
            if(hypergraph.addSumNode(node))
            {
                ArrayList<WordNode> list = new ArrayList(end - begin);
                for(int i = begin; i < end; i++) // Generate each word in this range independently
                {
                    list.add(genWord(i, c, event, field));
                }
                hypergraph.addEdge(node, list, new Hypergraph.HyperedgeInfo<Widget>()
                {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
        }
        return node;
    }   

     // Generate segmentation of i...end into fields; previous field is f0
    protected Object genFields(final int i, final int end, int c, final int event, final int f0, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams baseEventTypeParams = baseline.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        FieldsNode node = new FieldsNode(i, end, c, event, f0, efs);
        if(hypergraph.addSumNode(node))
        {
            if(oneFieldPerEvent())
            {
                selectJ(end, i, end, c, event, f0, efs, eventTypeParams, baseEventTypeParams, node);
            }
            else if(newFieldPerWord())
            {
                selectJ(i+1, i, end, c, event, f0, efs, eventTypeParams, baseEventTypeParams, node);
            }
            else
            {
                for(int k = i+1; k < end+1; k++)
                {
                    selectJ(k, i, end, c, event, f0, efs, eventTypeParams, baseEventTypeParams, node);
                }
            }
        } // if
        return node;
    }

    // Choose ending position j
    protected void selectJ(final int j, final int i, int end, final int c, final int event,
                         final int f0, int efs,
                         final EventTypeParams modelEventTypeParams,
                         final EventTypeParams baseEventTypeParams, FieldsNode node)
    {
        // Choose a new field to talk about (including none field, but not boundary)
        for(int f = 0; f < ex.events.get(event).getF() + 1; f++)
        {
            final int fIter = f;
            if(f == modelEventTypeParams.none_f || // If not none, then...
               ((!opts.disallowConsecutiveRepeatFields || f != f0) && // Can't repeat fields
               modelEventTypeParams.efs_canBePresent(efs, f) && // Make sure f can be there
               (!opts.limitFieldLength ||
               j-i <= ex.events.get(event).getFields()[f].getMaxLength())))
            { // Limit field length
//                int remember_f = indepFields() ? modelEventTypeParams.boundary_f : f;
                int remember_f = f;
                int new_efs = (f == modelEventTypeParams.none_f) ? efs :
                    modelEventTypeParams.efs_addAbsent(efs, f); // Now, allow f to be absent as we've already taken care of it

                if(j == end)
                {
                    hypergraph.addEdge(node, genField(i, j, c, event, f),
                                       new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseParam;
                        public double getWeight() { // final field-phrase before boundary   
                                baseParam = get(baseEventTypeParams.fieldChoices[f0], fIter) *
                                       get(baseEventTypeParams.fieldChoices[fIter],
                                           baseEventTypeParams.boundary_f);
                                return calculateOracle ?
                                        baseParam :
                                        getCount(modelEventTypeParams.fieldChoices[f0], fIter) +
                                        getCount(modelEventTypeParams.fieldChoices[fIter],
                                                 modelEventTypeParams.boundary_f) +
                                        getBaselineScore(baseParam);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            for(int k = i; k < j; k++)
                            {
                                widget.getFields()[c][k] = fIter;
                            }
                            Feature[] featuresArray = {
                                new Feature(modelEventTypeParams.fieldChoices[f0], fIter), 
                                new Feature(modelEventTypeParams.fieldChoices[fIter],
                                            modelEventTypeParams.boundary_f)                                    
                            };
                            increaseCounts(featuresArray, normalisedLog(baseParam));
                            return widget;
                        }

                        @Override
                        public Pair getWeightLM(int rank)
                        { // semantic parsing only
                            return new Pair(getWeight(),
                                    fIter < ex.events.get(event).getF() ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].getName().toLowerCase()) :
                                        vocabulary.getIndex("none_f"));
                        }

                        @Override
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            return widget;
                        }
                    });
                }
                else
                {
                    hypergraph.addEdge(node, genField(i, j, c, event, f),
                                       genFields(j, end, c, event, remember_f, new_efs),
                                       new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseParam; ProbVec weightProbVec;                   
                        public double getWeight() {                            
                            baseParam = get(baseEventTypeParams.fieldChoices[f0], fIter);                           
                            weightProbVec = modelEventTypeParams.fieldChoices[f0];
                            return calculateOracle ?
                                    baseParam : getCount(weightProbVec, fIter) + 
                                    getBaselineScore(baseParam);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {                            
                            for(int k = i; k < j; k++)
                            {
                                widget.getFields()[c][k] = fIter;
                            }
                            Feature[] featuresArray = {new Feature(weightProbVec, fIter)};
                            increaseCounts(featuresArray, normalisedLog(baseParam));
                            return widget;
                        }

                        @Override
                        public Pair getWeightLM(int rank)
                        { // semantic parsing only
                            return new Pair(getWeight(), 
                                    fIter < ex.events.get(event).getF() ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].getName().toLowerCase()) :
                                        vocabulary.getIndex("none_f"));
                        }

                        @Override
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            return widget;
                        }
                    });
                }
            } // if
        } // for
    }    

    protected Object genNoneEventWords(final int i, final int j, final int c)
    {
        NoneEventWordsNode node = new NoneEventWordsNode(i, j, c);
        if(opts.binariseAtWordLevel)
        {
            if (i == j)
            {
                return hypergraph.endNode;
            }
            if(hypergraph.addSumNode(node))
            {
                hypergraph.addEdge(node,
                                   genNoneWord(i, c),
                                   genNoneEventWords(i + 1, j, c),
                                   new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
        }
        else
        {
            if(hypergraph.addSumNode(node))
            {
                ArrayList<WordNode> list = new ArrayList(j - i);
                for(int k = i; k < j; k++) // Generate each word in this range using an LM but still independently
                {
                    list.add(genNoneWord(k, c));
                }
                hypergraph.addEdge(node, list, new Hypergraph.HyperedgeInfo<Widget>()
                {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            } // if
        }
        return node;
    }   

    protected WordNode genNoneWord(final int i, final int c)
    {
        WordNode node = new WordNode(i, c, ((Event3Model)model).none_t(), -1);
        if(hypergraph.addSumNode(node))
        {
            // add hyperedge for each word. COSTLY!
            final int maxWordIndex = params.trackParams[c].getNoneEventTypeEmissions().getCounts().length;
//            for(int wIter = 0; wIter < (opts.modelUnkWord ? vocabulary.size() : vocabulary.size() - 1); wIter++)
            for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
            {
                final int w = wIter;
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                    double baseParam; ProbVec weightProbVec;
                    public double getWeight() 
                    {
                        baseParam = get(baseline.trackParams[c].getNoneEventTypeEmissions(), w);
                        weightProbVec = params.trackParams[c].getNoneEventTypeEmissions();
                        return calculateOracle ? 
                                 baseParam : getCount(weightProbVec, w) + 
                                 getBaselineScore(baseParam);
                    }
                    public Pair getWeightLM(int rank) // used for k-best
                    {
                        return getAtRank(baseline.trackParams[c].getNoneEventTypeEmissions(), rank);
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) 
                    {                         
                        return chooseLM(widget, w);
                    }
                    public GenWidget chooseLM(GenWidget widget, int word)
                    {
                        widget.getText()[i] = word;
                        Feature[] featuresArray = {new Feature(weightProbVec, w)};
                        increaseCounts(featuresArray, normalisedLog(baseParam));
                        return widget;
                    }
                });
            } // for
        }
        return node;
    }

    protected StopNode genStopNode(int i, final int t0, final TrackParams modelCParams, 
                                                        final TrackParams baseCParams)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<Widget>() {
                double baseParam; ProbVec weightProbVec;
                int index = modelCParams.boundary_t;
                public double getWeight() {
                    weightProbVec = modelCParams.getEventTypeChoices()[index];                    
                    baseParam = get(baseCParams.getEventTypeChoices()[t0], index);
                    return calculateOracle ? baseParam :
                            getCount(weightProbVec, index) + 
                            getBaselineScore(baseParam);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {                    
                    return chooseLM(widget, -1);
                }

                @Override
                public Pair getWeightLM(int rank)
                {
                    if(rank > 0)
                        return null;
                    return new Pair(getWeight(), vocabulary.getIndex("</s>"));
//                    return new Pair(1.0, vocabulary.getIndex("</s>"));
                }

                @Override
                public Widget chooseLM(Widget widget, int word)
                {
                    Feature[] featuresArray = {new Feature(weightProbVec, index)};
                    increaseCounts(featuresArray, normalisedLog(baseParam));
                    return widget;
                }
            });
        } // if
        return node;
    }

    // Generate track c in i...j (t0 is previous event type for track 0);
    // allowNone and allowReal specify what event types we can use
    protected Object genTrack(final int i, final int j, final int t0, final int c,
                       boolean allowNone, boolean allowReal)
    {        
        final TrackParams modelCParams = params.trackParams[c];        
        final TrackParams baseCParams = baseline.trackParams[c];

        if(i == j)
        {                      
            return genStopNode(i, t0, modelCParams, baseCParams);
        } // if (i == j)
        TrackNode node = new TrackNode(i, j, t0, c, allowNone, allowReal);
        // WARNING: allowNone/allowReal might not result in any valid nodes
        if(hypergraph.addSumNode(node))
        {
            // (1) Choose the none event
          if (allowNone && (!trueInfer || ex.getTrueWidget() == null ||
              ex.getTrueWidget().hasNoReachableContiguousEvents(i, j, c)))
          {
              final int remember_t = opts.conditionNoneEvent ? modelCParams.none_t : t0; // Condition on none_t or not
              Object recurseNode = (c == 0) ? genEvents(j, remember_t) : hypergraph.endNode;
              hypergraph.addEdge(node,
                  genNoneEventWords(i, j, c), recurseNode,
                  new Hypergraph.HyperedgeInfoOnline<Widget>() {
                      double baseParam; ProbVec alignWeightProbVec;
                      int boundaryIndex = modelCParams.boundary_t;
                      public double getWeight() {                         
                          baseParam = get(baseCParams.getEventTypeChoices()[t0], 
                                 baseCParams.none_t);
                          alignWeightProbVec = modelCParams.getEventTypeChoices()[t0];
                          return calculateOracle ? 
                                 baseParam :
                                  // pecreptron weight * 1 (omitted, since 
                                 // it is equivalent to the count of the 
                                 // alignment inferState rule)
                                 getCount(alignWeightProbVec, boundaryIndex) + 
                                 getBaselineScore(baseParam);
                         
                      }
                      public double getOnlineWeight()
                      {
                          return 0.0;
                      }
                      public void setPosterior(double prob) {}
                      public Widget choose(Widget widget) {
                          for(int k = i; k < j; k++)
                          {
                              widget.getEvents()[c][k] = Parameters.none_e;
                          }
                          Feature[] featuresArray = {new Feature(alignWeightProbVec, boundaryIndex)};
                          increaseCounts(featuresArray, normalisedLog(baseParam));
                          return widget;
                      }
                  });                            
          } // if
          // (2) Choose an event type t and event e for track c
          for(final Event e : ex.events.values())
          {
              final int eventId = e.getId();
              final int eventTypeIndex = e.getEventTypeIndex();
              if (allowReal && 
                      (!trueInfer || ex.getTrueWidget() == null ||
                      ex.getTrueWidget().hasContiguousEvents(i, j, eventId)))
              {
//                  final int remember_t = (indepEventTypes()) ? modelCParams.boundary_t : eventTypeIndex;
                  final int remember_t = eventTypeIndex;
                  final Object recurseNode = (c == 0) ? genEvents(j, remember_t) : hypergraph.endNode;
                  final EventTypeParams eventTypeParams = params.eventTypeParams[e.getEventTypeIndex()];
                  hypergraph.addEdge(node,
                  genFields(i, j, c, eventId, eventTypeParams.boundary_f, 
                                              eventTypeParams.getDontcare_efs()), 
                                              recurseNode,
                            new Hypergraph.HyperedgeInfo<Widget>() {
                      double baseParam; ProbVec alignWeightProbVec;
                      public double getWeight()
                      {
                          baseParam = get(baseCParams.getEventTypeChoices()[t0], eventTypeIndex) *
                                  (1.0/(double)ex.getEventTypeCounts()[eventTypeIndex]);                          
                          alignWeightProbVec = modelCParams.getEventTypeChoices()[t0];
                          return calculateOracle ? baseParam : 
                                  getCount(alignWeightProbVec, eventTypeIndex) + 
                                  getBaselineScore(baseParam);                         
                      }
                      public void setPosterior(double prob) {}
                      public Widget choose(Widget widget) {
                          for(int k = i; k < j; k++)
                          {
                              widget.getEvents()[c][k] = eventId;
                          }                          
                          Feature[] featuresArray = {new Feature(alignWeightProbVec, eventTypeIndex)};
                          increaseCounts(featuresArray, normalisedLog(baseParam));
                          return widget;
                      }
                  });                  
              } // if
          } // for
        } // if        
        return node;
    }

    // Generate segmentation of i...N into event types; previous event type is t0
    // Incorporate eventType distributions
    protected Object genEvents(int i, int t0)
    {
        
        if (i == N)
        {
//            return hypergraph.endNode;
            EventsNode node = new EventsNode(N, t0);
            if(hypergraph.addSumNode(node))
            {
                selectEnd(N, node, N, t0);
                hypergraph.assertNonEmpty(node);
            }
            return node;
        }
        else
        {
            EventsNode node = new EventsNode(i, t0);
            if(hypergraph.addSumNode(node))
            {
                if(calculateOracle)
                {
                    if (oneEventPerExample())
                        selectEnd(N, node, i, t0);
                    else if (newEventTypeFieldPerWord())
                        selectEnd(i+1, node, i, t0);
                    else if (opts.onlyBreakOnPunctuation &&
                             opts.dontCrossPunctuation) // Break at first punctuation
                    {
                        selectEnd(Utils.find(i+1, N, 
                                ex.getIsPunctuationArray()), node, i, t0);
                    }
                    else if (opts.onlyBreakOnPunctuation) // Break at punctuation (but can cross)
                    {
                        for(int j = i+1; j < end(i, N)+1; j++)
                        {
                            if(j == N || ex.getIsPunctuationArray()[j-1])
                            {
                                selectEnd(j, node, i, t0);
                            }
                        }
                    }
                    else if (opts.dontCrossPunctuation) // Go up until the first punctuation
                    {
                        for(int k = i+1; k < Utils.find(i+1, N, 
                                ex.getIsPunctuationArray())+1; k++)
                        {
                            selectEnd(k, node, i, t0);
                        }
                    }
                    else // Allow everything
                    {
                        for(int k = i+1; k < end(i, N)+1; k++)
                        {
                            selectEnd(k, node, i, t0);
                        }
                    }
                } // if
                else // Allow everything
                {
                    for(int k = i+1; k < end(i, N)+1; k++)
                    {
                        selectEnd(k, node, i, t0);
                    }
                }                               
                hypergraph.assertNonEmpty(node);
            }
            return node;
        }
    }
        
    protected void selectEnd(int j, EventsNode node, int i, int t0)
    {
        hypergraph.addEdge(node, genTrack(i, j, t0, 0, opts.allowNoneEvent, true),
                new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public void setPosterior(double prob) { }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
    }
}