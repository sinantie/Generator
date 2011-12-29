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
import induction.Options.ModelType;
import induction.ngrams.NgramModel;
import induction.Utils;
import induction.problem.AModel;
import induction.problem.AParams;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.CatField;
import induction.problem.event3.Event;
import induction.problem.event3.Event3InferState;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.NumField;
import induction.problem.event3.Widget;
import induction.problem.event3.discriminative.params.DiscriminativeEventTypeParams;
import induction.problem.event3.discriminative.params.DiscriminativeParams;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.nodes.FieldsNode;
import induction.problem.event3.nodes.NoneEventWordsNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.EmissionParams;
import induction.problem.event3.params.TrackParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class describes a hypregraph representation of the problem. The main
 * difference from the Generative Model is the calculation of the alignWeights in
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
     * Use k-best Viterbi in order to estimate parameters. Used mainly in the emission of terminals
     */
    boolean useKBest = false;
    /**
     * map of the count of features extracted during the Viterbi search.
     * It has to be set first to the corresponding map (inferState under train, or oracle)
     * before doing the recursive call to extract D_1 (top derivation)
     */
    protected HashMap<Feature, Double> features;
    /**
     * parameters only of the multinomials that emit terminal symbols. Necessary for
     * combining the weights of the model given by the local parameters and the baseline score, and
     * thus resorting in best-first order. Used for non-local features only (k-best)
     */    
    EmissionParams emissionsParams;

    Map<List<Integer>, Integer> wordBigramsMap, wordNgramsMap, wordNegativeNgramsMap;
    Map<List<Integer>, Integer>[] fieldNgramsMapPerEventTypeArray;
    
    protected int iteration;    
    
    WordNode startSymbol = new WordNode(-1, 0, -1, -1);
    
    public DiscriminativeInferState(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, boolean useKBest)
    {
        super(model, ex, params, counts, ispec);
        this.ngramModel = ngramModel;
        this.useKBest = useKBest;
        this.baseline = model.getBaselineModelParams();
        this.baselineFeature = new Feature(((DiscriminativeParams)params).baselineWeight, 0);        
        this.wordNgramsMap = ((DiscriminativeEvent3Model)model).getWordNgramMap();
        this.wordBigramsMap = ((DiscriminativeEvent3Model)model).getWordBigramMap();
        this.wordNegativeNgramsMap = ((DiscriminativeEvent3Model)model).getWordNegativeNgramMap();
        this.fieldNgramsMapPerEventTypeArray = ((DiscriminativeEvent3Model)model).getFieldNgramsMapPerEventTypeArray();
        this.iteration = ispec.getIter();        
    }

    public DiscriminativeInferState(DiscriminativeEvent3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, boolean useKBest, Graph graph)
    {
        this(model, ex, params, counts, ispec, ngramModel, useKBest);
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
    
    protected void increaseCounts(List<Feature> features)
    {
        for(Feature f: features)
            increaseCount(f, 1);
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

    protected void resortDiscriminativeEmissions()
    {
        int c = 0; // assume we only have 1 track (TODO: may need to remove that)        
        int W = ((DiscriminativeEvent3Model)model).vocabularySize;
//        int W = Event3Model.W() - 2; // ignore <s> and </s>
//        if(iteration == 0)
//            ((DiscriminativeParams)params).baselineWeight.set(0, 1);
        // treat catEmissions and noneFieldEmissions
        Map<Integer, Map<Integer, Vec[]>> localFieldEmissions = new HashMap<Integer, Map<Integer, Vec[]>>();
        Map<Integer, Vec> localNoneFieldEmissions = new HashMap<Integer, Vec>();
        for(final Event event : ex.events.values())       
        {
            int eventTypeIndex = event.getEventTypeIndex();
            int eventId = event.getId();            
            // treat catEmissions
            Map<Integer, Vec[]> fieldEmissions = new HashMap<Integer, Vec[]>(event.getF());
            for(int f = 0; f < event.getF(); f++)
            {
                Field field = event.getFields()[f];
                if(field instanceof CatField)
                {
                    CatFieldParams modelFieldParams = getCatFieldParams(eventId, f);
                    CatFieldParams baseFieldParams = getBaselineCatFieldParams(eventId, f);
                    // we keep track of the number of values from the model parameters, 
                    // rather than from the example, as in the test dataset there
                    // might be extra (unseen) values, for which we have no trained data.
                    Vec[] emissions = VecFactory.zeros2(VecFactory.Type.DENSE, modelFieldParams.emissions.length, W);
//                    for(int v = 0; v < field.getV(); v++)
                    for(int v = 0; v < modelFieldParams.emissions.length; v++)
                    {                        
                        emissions[v].addCount(modelFieldParams.emissions[v]);
                        if(opts.includeHasEmptyValueFeature && modelFieldParams.isEmptyValue(v))
                            emissions[v].addCount(((DiscriminativeParams)params).hasEmptyValueWeight.getCount(0));
                        emissions[v].addCount(getBaselineScore(baseFieldParams.emissions[v]));
                        emissions[v].setCountsSortedIndices();
                    } // for
                    fieldEmissions.put(f, emissions);
                } // if                
            } // for
            localFieldEmissions.put(eventTypeIndex, fieldEmissions);
            // treat noneFieldEmissions
            Vec noneFieldEmissions = VecFactory.zeros(VecFactory.Type.DENSE, W);
            noneFieldEmissions.addCount(params.eventTypeParams[eventTypeIndex].noneFieldEmissions);
            noneFieldEmissions.addCount(getBaselineScore(baseline.eventTypeParams[eventTypeIndex].noneFieldEmissions));
            noneFieldEmissions.setCountsSortedIndices();
            localNoneFieldEmissions.put(eventTypeIndex, noneFieldEmissions);            
        } // for
        // treat noneEventTypeEmissions
        Vec localNoneEventTypeEmissions = VecFactory.zeros(VecFactory.Type.DENSE, W);
//        localNoneEventTypeEmissions.addCount(params.trackParams[c].getNoneEventTypeEmissions());
        localNoneEventTypeEmissions.addCount(
                getBaselineScore(baseline.trackParams[c].getNoneEventTypeEmissions()));
        localNoneEventTypeEmissions.setCountsSortedIndices();
        //treat genericEmissions
        Vec localGenericEmissions = VecFactory.zeros(VecFactory.Type.DENSE, W);
        localGenericEmissions.addCount(params.genericEmissions);
        localGenericEmissions.addCount(getBaselineScore(baseline.genericEmissions));
        localGenericEmissions.setCountsSortedIndices();
        
        emissionsParams = new EmissionParams(
                localFieldEmissions,                
                localNoneFieldEmissions, 
                localNoneEventTypeEmissions,                 
                localGenericEmissions);
    }
    
    protected Vec getResortedCatFieldEmissions(int event, int field, int value)
    {
        return emissionsParams.getCatEmissions().
                get(ex.events.get(event).getEventTypeIndex()).
                get(field)[value];
    }
    
    protected Vec getResortedNoneFieldEmissions(int eventTypeIndex)
    {
        return emissionsParams.getNoneFieldEmissions().get(eventTypeIndex);
    }
    
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {
//        testGetHasConsecutiveNgramsWeight();
        if(useKBest)
            resortDiscriminativeEmissions();
        // setup hypergraph preliminaries
        hypergraph.setup(this, opts.debug, opts.modelType, true, opts.kBest, ngramModel, opts.ngramSize,
                opts.reorderType, opts.allowConsecutiveEvents,
                opts.oracleReranker,
                /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
                vocabulary.getIndex("<num>"),
                vocabulary.getIndex("ELIDED_SYMBOL"),
                opts.numAsSymbol,
                vocabulary, ex, graph);
        hypergraph.addSumNode(startSymbol);        
        this.hypergraph.addEdge(startSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
        {
            public double getWeight()
            { 
                return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking            
            }
            public Pair getWeightAtRank(int rank)
            {
                return rank > 0 ? null : new Pair(0.0, vocabulary.getIndex("<s>"));
            }
            public void setPosterior(double prob)
            { }
             public GenWidget choose(GenWidget widget)
            { return widget; }

            public GenWidget chooseWord(GenWidget widget, int word)
            { return widget; }
        });
        ArrayList<Object> list = new ArrayList(opts.ngramSize);
        for(int i = 0; i < opts.ngramSize - 1; i++) // Generate each word in this range using an LM
        {
            list.add(startSymbol);
        }
        list.add(genEvents(0, ((Event3Model)model).boundary_t()));
        this.hypergraph.addEdge(hypergraph.sumStartNode(), list,
                       new Hypergraph.HyperedgeInfoOnline<GenWidget>()
        {
            public double getWeight()
            {
                return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
            }
            public double getOnlineWeight(List<List<Integer>> ngrams)
            {
                return getNgramWeights(ngrams) + getLMWeights(ngrams);
            }
            public void setPosterior(double prob)
            { }
            public GenWidget choose(GenWidget widget)
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
            if(useKBest)
            {
                StopWatchSet.begin("rerank k-best Viterbi");
//                result = hypergraph.rerankKBestViterbi(newWidget(), opts.initRandom);
                result = hypergraph.kBestViterbi(newWidget());
                if(opts.modelType == ModelType.discriminativeTrain)
                {
                    // compute ngram features (we can do it  in the end,
                    // since we have created the resulting output text)
                    increaseNgramLMCounts(((GenWidget)result.widget).getText());
//                    increaseNegativeNgramCounts(((GenWidget)result.widget).getText());
                    if(opts.includeHasConsecutiveWordsFeature)
                        increaseHasConsecutiveWordsCount(((GenWidget)result.widget).getText());
                    if(opts.includeHasConsecutiveBigramsFeature && iteration >= 5)
                        increaseHasConsecutiveNgramsCount(((GenWidget)result.widget).getText(), 2);
                    if(opts.includeHasConsecutiveTrigramsFeature && iteration >= 5)
                        increaseHasConsecutiveNgramsCount(((GenWidget)result.widget).getText(), 3);
                    if(opts.includeFieldNgramsPerEventTypeFeature)
                        increaseFieldNgramCount((Widget)result.widget);
                }
            }
            else
            {
                StopWatchSet.begin("rerank 1-best Viterbi");
                result = hypergraph.rerankOneBestViterbi(newWidget(), opts.initRandom);
            }
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
    
    protected double normalise(double logD)
    {
        return logD / ex.N();
    }
    
    protected double getBaselineScore(double baseProb)
    {
//        return 0.0;
        return getCount(((DiscriminativeParams)params).baselineWeight, 0) * normalisedLog(baseProb);
    }        
    
    protected Vec getBaselineScore(Vec vec)
    {
//       double[] baseProbs = probVec.getCounts();
       double[] baseProbs = vec.getProbs();
       double baseWeight = getCount(((DiscriminativeParams)params).baselineWeight, 0);
       Vec res = VecFactory.zeros(VecFactory.Type.DENSE, baseProbs.length);
       for(int  i = 0; i < baseProbs.length; i++)
       {
           res.set(i, baseWeight * normalisedLog(baseProbs[i]));
       }           
       return res;
    }
        
    protected List<Feature> getNgramFeatures(Vec weights, List<Integer> weightIndices, boolean augment)
    {
        List<Feature> list = new ArrayList<Feature>();
        if(weightIndices != null)
            for(Integer index : weightIndices)
            {
                // special case of expandable count vectors, such as the negative ngrams.
                // If we see a new ngram add in the list.
                if(augment && weights.getCount(index) > Double.NEGATIVE_INFINITY)
                {
                    weights.addCount(index, 0.0);
                }
                list.add(new Feature(weights, index));
            }
        return list;
    }        
    
    /**
     * compute the total weight of the non-local ngram features, from the list of
     * ngrams given as a parameter
     * @param ngrams
     * @return 
     */
    protected double getNgramWeights(List<List<Integer>> ngrams)
    {
//         deal with positive ngrams, i.e. ngrams that exist in the gold standard text
        List<Integer> ngramIndices = NgramModel.getNgramIndices(wordNgramsMap, ngrams);
        double weight = 0.0;
        for(Integer index : ngramIndices)
            weight += getCount(((DiscriminativeParams)params).ngramWeights, index);
//         deal with positive bigrams, i.e. bigrams that exist in the gold standard text
        if(opts.includeBigramsFeature)
        {
            ngramIndices = NgramModel.getNgramIndices(wordBigramsMap, ngrams);        
            for(Integer index : ngramIndices)
                weight += getCount(((DiscriminativeParams)params).bigramWeights, index);
        }
        // deal with negative ngrams, i.e. ngrams never seen in the gold standard text
//        List<Integer> ngramNegativeIndices = NgramModel.getNgramIndices(wordNegativeNgramsMap, ngrams);
//        for(Integer index : ngramNegativeIndices)
//            weight += getCount(((DiscriminativeParams)params).ngramNegativeWeights, index);
        return weight;
//        return 0.0;        
    }
    
    /**
     * compute the lm score for the ngrams given in the parameter
     * @param ngrams a list of ngrams
     * @return 
     */
    protected double getLMWeights(List<List<Integer>> ngrams)
    {        
//        double weight = 0.0;
//        for(List<Integer> ngram : ngrams)
//            weight += //getCount(((DiscriminativeParams)params).lmWeight, 0) * 
//                    normalise(NgramModel.getNgramLMLogProb(ngramModel, vocabulary, 
//                              opts.numAsSymbol, ngram));
//        return weight;
        return 0.0;
    }
    
    protected double getHasConsecutiveWordsWeight(List<List<Integer>> ngrams)
    {
        if(!opts.includeHasConsecutiveWordsFeature)
            return 0.0;
        double weight = 0.0d;
        for(List<Integer> ngram : ngrams)
        {
            Integer prevWord = -1;
            for(Integer word : ngram)
            {
                if(word.equals(prevWord))
                    weight += getCount(((DiscriminativeParams)params).hasConsecutiveWordsWeight, 0);
                prevWord = word;
            }
        }
        return weight;
    }
    
    protected double getHasConsecutiveNgramsWeight(List<List<Integer>> ngrams, int n)
    {
        if(ngrams.isEmpty() || n == 2 && !opts.includeHasConsecutiveBigramsFeature || n == 3 && !opts.includeHasConsecutiveTrigramsFeature)
            return 0.0;
        double weight = 0.0d;        
        // concatenate input ngrams back into one utterance
        List<Integer> input = new ArrayList<Integer>(ngrams.get(0)); 
        for(int i = 1; i < ngrams.size(); i++)
        {
            List<Integer> ngram = ngrams.get(i);
            input.add(ngram.get(ngram.size() -1)); // add last word
        }
        if(input.size() < 2 * n) // we need at least two ngrams
            return 0.0d;
        double featureWeight = n == 2 ? getCount(((DiscriminativeParams)params).hasConsecutiveBigramsWeight, 0) :
                                 getCount(((DiscriminativeParams)params).hasConsecutiveTrigramsWeight, 0);
        List<Integer> subInput;
//        for(int k = 0; k <= input.size() - 2*n; k++)
        for(int k = 0; k < n; k++) // slide the window, at most n-1 times, to cover multiple-of-n sizes of text.length
        {
            List<Integer> prevSubInput = null;
            for(int i = k; i <= input.size() - n; i += n)
            {
                subInput = input.subList(i, i + n);
                if(subInput.equals(prevSubInput))
                    weight += featureWeight;
                prevSubInput = subInput;
            } // for
        }
        return weight;
    }
    
    protected double getFieldNgramWeights(List<List<Integer>> ngrams, int eventTypeIndex)
    {
        if(!opts.includeFieldNgramsPerEventTypeFeature)
            return 0.0;
        double weight = 0.0;      
        List<Integer> ngramFieldIndices = NgramModel.getNgramIndices(
                fieldNgramsMapPerEventTypeArray[eventTypeIndex], ngrams);
        for(Integer index : ngramFieldIndices)
            weight += getCount(((DiscriminativeEventTypeParams)params.eventTypeParams[eventTypeIndex]).fieldNgrams, index);
        return weight;
    }
    
    /**
     * increases the ngram and lm features of the model, given an indexed text 
     * @param textArray 
     */
    protected void increaseNgramLMCounts(int[] textArray)
    {        
        List<Integer> text = new ArrayList();
        for(int i = 0; i < opts.ngramSize - 1; i++)
            text.add(vocabulary.getIndex("<s>"));
        for(Integer word: textArray)
            text.add(word);
        text.add(vocabulary.getIndex("</s>"));
        // deal with positive ngrams
        List<Integer> ngramIndices = NgramModel.getNgramIndices(
            wordNgramsMap, 3, text, false);
        increaseCounts(getNgramFeatures(((DiscriminativeParams)params).ngramWeights, ngramIndices, false));   
        // deal with positive bigrams
        if(opts.includeBigramsFeature)
        {            
            ngramIndices = NgramModel.getNgramIndices(
                wordBigramsMap, 2, text, false);
            increaseCounts(getNgramFeatures(((DiscriminativeParams)params).bigramWeights, ngramIndices, false));        
        }
        
        // compute lm feature
//        increaseCount(new Feature(((DiscriminativeParams)params).lmWeight, 0),
//                normalise(NgramModel.getSentenceLMLogProb(ngramModel, vocabulary, opts.numAsSymbol, 3, text)));
    }
    
    protected void increaseNegativeNgramCounts(int[] textArray)
    {
        List<Integer> text = new ArrayList();
        for(int i = 0; i < opts.ngramSize - 1; i++)
            text.add(vocabulary.getIndex("<s>"));
        for(Integer word: textArray)
            text.add(word);
        text.add(vocabulary.getIndex("</s>"));
        // deal with negative ngrams
        List<Integer> ngramIndices = NgramModel.getNgramIndices(wordNegativeNgramsMap, 3, text, true);
        increaseCounts(getNgramFeatures(((DiscriminativeParams)params).ngramNegativeWeights, ngramIndices, true));
    }
    
    protected void increaseFieldNgramCount(Widget widget)
    {
        int n = widget.getEvents()[0].length;       
        for(int c = 0; c < widget.getEvents().length; c++)
        {
            int i = 0;
            while (i < n) // Segment into entries
            {
                int e = widget.getEvents()[c][i];
                int eventTypeIndex = ex.events.get(e).getEventTypeIndex();
                int j = i + 1;
                while (j < n && widget.getEvents()[c][j] == e)
                {
                    j++;
                }                                
                if (e != Parameters.none_e) // Tackle events with fields only
                {                    
                    if (widget.getFields() != null)                
                    {
                        List<Integer> fieldsString = new ArrayList<Integer>();
                        int k = i;
                        while (k < j) // Segment i...j into fields
                        {
                            int f = widget.getFields()[c][k];
                            int l = k+1;
                            while (l < j && widget.getFields()[c][l] == f)
                            {
                                l++;
                            }                        
                            if (f != -1)
                            {
                                fieldsString.add(f);                            
                            }                        
                            k = l;                            
                        } // while
                        List<Integer> ngramIndices = NgramModel.getNgramIndices(
                            fieldNgramsMapPerEventTypeArray[eventTypeIndex], 3, fieldsString, true);
                        increaseCounts(getNgramFeatures(((DiscriminativeEventTypeParams)params.eventTypeParams[eventTypeIndex]).
                            fieldNgrams, ngramIndices, true));
                    } // else                    
                } // if                
                i = j;
            } // while
        } // for
    }
    
    protected void increaseHasConsecutiveWordsCount(int[] textArray)
    {
        int prevWord = -1, count = 0;
        for(int word : textArray)
        {
            if(word == prevWord)
                count++;
            prevWord = word;
        }
        increaseCount(new Feature(((DiscriminativeParams)params).hasConsecutiveWordsWeight, 0), count);
    }   
    
    protected void increaseHasConsecutiveNgramsCount(int[] textArray, int n)
    {
        if(textArray.length < 2 * n) // we need at least two ngrams
            return;
        List<Integer> input = Utils.asList(textArray);
        List<Integer> subInput;
        int count = 0;
//        for(int k = 0; k <= input.size() - 2*n; k++)
        for(int k = 0; k < n; k++) // slide the window, at most n-1 times, to cover multiple-of-n sizes of text.length
        {
            List<Integer> prevSubInput = null;
            for(int i = k; i <= input.size() - n; i += n)
            {
                subInput = input.subList(i, i + n);
                if(subInput.equals(prevSubInput))
                    count++;
                prevSubInput = subInput;
            } // for
        }
        Feature feature = n == 2 ? new Feature(((DiscriminativeParams)params).hasConsecutiveBigramsWeight, 0) :
                                   new Feature(((DiscriminativeParams)params).hasConsecutiveTrigramsWeight, 0);
        increaseCount(feature, count);
            
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
                            final Vec alignWeights, final Vec baseProbVec,
                            final int value, final int c, final int i)
    {
        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
            double baseProb;
            public double getWeight() {
                baseProb = get(baseProbVec, method);
                return getCount(alignWeights, method) + getBaselineScore(baseProb); 
            }
            public Pair getWeightAtRank(int rank) {
                return rank > 0 ? null : new Pair(getCount(alignWeights, method), vocabulary.getIndex("<num>"));
            }
            public void setPosterior(double prob) { }
            public GenWidget choose(GenWidget widget) {
                widget.getText()[i] = vocabulary.getIndex("<num>");
                widget.getNumMethods()[c][i] = method;
                widget.getNums()[i] = value;
                Feature[] featuresArray = {new Feature(alignWeights, method)};
                increaseCounts(featuresArray, normalisedLog(baseProb));
                return widget;
            }
            public GenWidget chooseWord(GenWidget widget, int word)
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
            final Vec alignWeights = modelFParams.methodChoices;
            final Vec baseProbVec = baseFParams.methodChoices;
            
            addNumEdge(Parameters.M_IDENTITY, node, alignWeights, baseProbVec, v, c, i);
            addNumEdge(Parameters.M_ROUNDUP, node, alignWeights, baseProbVec, roundUp(v), c, i);
            addNumEdge(Parameters.M_ROUNDDOWN, node, alignWeights, baseProbVec, roundDown(v), c, i);
            addNumEdge(Parameters.M_ROUNDCLOSE, node, alignWeights, baseProbVec, roundClose(v), c, i);
            
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseProb; int method = Parameters.M_NOISEUP;
                final double CONT = getCount(modelFParams.rightNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = getCount(modelFParams.rightNoiseChoices, Parameters.S_STOP);
                final int NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    baseProb = get(baseFParams.methodChoices, method);
                    return getCount(alignWeights, method) + getBaselineScore(baseProb);
                }
                public Pair getWeightAtRank(int rank) {
                    return rank > 0 ? null : new Pair(getCount(modelFParams.methodChoices, method),
                                                        vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getText()[i] = vocabulary.getIndex("<num>");
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = NOISE_MINUS_ONE + 1 + v;
                    Feature[] featuresArray = {new Feature(alignWeights, method)};
                    increaseCounts(featuresArray, normalisedLog(baseProb));
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    return choose(widget);
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                double baseProb; int method = Parameters.M_NOISEDOWN;
                final double CONT = getCount(modelFParams.leftNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = getCount(modelFParams.leftNoiseChoices, Parameters.S_STOP);
                final int MINUS_NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    baseProb = get(baseFParams.methodChoices, method);
                    return getCount(alignWeights, method) + getBaselineScore(baseProb);
                }
                public Pair getWeightAtRank(int rank) {
                    return rank > 0 ? null : new Pair(getCount(modelFParams.methodChoices, method),
                                                        vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getText()[i] = vocabulary.getIndex("<num>");
                    widget.getNumMethods()[c][i] = method;
                    widget.getNums()[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    Feature[] featuresArray = {new Feature(alignWeights, method)};
                    increaseCounts(featuresArray, normalisedLog(baseProb));
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
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
            if(useKBest)
            {                                
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {                
                public double getWeight() {return 0.0d;}
                public Pair getWeightAtRank(int rank)
                {                   
                    return getAtRank(getResortedCatFieldEmissions(event, field, v), rank);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {return widget;}
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    Feature[] featuresArray = {new Feature(modelFParams.emissions[v], word)};
                    increaseCounts(featuresArray, normalisedLog(get(baseFParams.emissions[v], word)));
                    if(opts.includeHasEmptyValueFeature && modelFParams.isEmptyValue(v))
                        increaseCount(new Feature(((DiscriminativeParams)params).hasEmptyValueWeight, 0), 1);
                    return widget;
                }
                });
            }
            else
            {
                // add hyperedge for each word. COSTLY!
                final int maxWordIndex = modelFParams.emissions[v].size();// - 2; // don't consider start and end symbol here
                for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                {
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                    double baseProb; Vec alignWeights;
                    public double getWeight() {
                        baseProb = get(baseFParams.emissions[v], w);
                        alignWeights = modelFParams.emissions[v];
                        return getCount(alignWeights, w) + getBaselineScore(baseProb);
                    }
                    public Pair getWeightAtRank(int rank)
                    {return null;}
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {                    
                        widget.getText()[i] = w;
                        Feature[] featuresArray = {new Feature(alignWeights, w)};
                        increaseCounts(featuresArray, normalisedLog(baseProb));
                        return widget;
                    }
                    public GenWidget chooseWord(GenWidget widget, int word)
                    {return widget;}
                    });
                } // for            
            }            
        } // if
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
                if(useKBest)
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {                
                    public double getWeight() {
                        return 0.0d;
                    }
                    public Pair getWeightAtRank(int rank)
                    {                   
                        return getAtRank(getResortedNoneFieldEmissions(eventTypeIndex), rank);
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {                    
                        return widget;
                    }
                    public GenWidget chooseWord(GenWidget widget, int word)
                    {
                        widget.getText()[i] = word;
                        Feature[] featuresArray = {new Feature(modelEventTypeParams.noneFieldEmissions, word)};
                        increaseCounts(featuresArray, normalisedLog(get(baseEventTypeParams.noneFieldEmissions, word)));
                        return widget;
                    }
                    });
                }
                else
                {
                    // add hyperedge for each word. COSTLY!
                    final int maxWordIndex = modelEventTypeParams.noneFieldEmissions.size();// - 2; // don't consider start and end symbol here
                    for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                    { 
                        final int w = wIter;
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                            double baseProb; Vec alignWeights;
                            public double getWeight() {
                                baseProb = get(baseEventTypeParams.noneFieldEmissions, w);
                                alignWeights = modelEventTypeParams.noneFieldEmissions;
                                return getCount(alignWeights, w) + getBaselineScore(baseProb);
                            }
                            public void setPosterior(double prob) { }
                            public GenWidget choose(GenWidget widget)
                            {                            
                                widget.getText()[i] = w;
                                Feature[] featuresArray = {new Feature(alignWeights, w)};
                                increaseCounts(featuresArray, normalisedLog(baseProb));
                                return widget;
                            }
                            public Pair getWeightAtRank(int rank)
                            {return null;}
                            public GenWidget chooseWord(GenWidget widget, int word)
                            {return widget;}
                        });
                    } // for
                } // else                
            } // if none_f
            else
            {
                // G_FIELD_VALUE: generate based on field value
                hypergraph.addEdge(node, genFieldValue(i, c, event, field),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                double baseProb; Vec alignWeights;
                public double getWeight() {
                    baseProb = get(baseEventTypeParams.genChoices[field], 
                            Parameters.G_FIELD_VALUE);                    
                    alignWeights = modelEventTypeParams.genChoices[field];
                    return  getCount(alignWeights, Parameters.G_FIELD_VALUE) +
                            getBaselineScore(baseProb);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    Feature[] featuresArray = {new Feature(alignWeights, 
                            Parameters.G_FIELD_VALUE)};
                    increaseCounts(featuresArray, normalisedLog(baseProb));
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
                });
                // G_FIELD_GENERIC: generate based on event type                               
                if(useKBest)
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {                
                    public double getWeight() {return 0.0d;}
                    public Pair getWeightAtRank(int rank)
                    {                   
                        Pair p = getAtRank(emissionsParams.getGenericEmissions(), rank);
                        p.value += getCount(modelEventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
                        return p;
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {return widget;}
                    public GenWidget chooseWord(GenWidget widget, int word)
                    {
                        widget.getText()[i] = word;                        
                        widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                        Feature[] featuresArray = {
                            new Feature(modelEventTypeParams.genChoices[field], 
                                    Parameters.G_FIELD_GENERIC),
                            new Feature(params.genericEmissions, word)
                        };
                        increaseCounts(featuresArray, normalisedLog(get(baseEventTypeParams.genChoices[field], 
                                        Parameters.G_FIELD_GENERIC) * get(baseline.genericEmissions, word)));
                        return widget;
                    }
                    });
                } // if
                else // add hyperedge for each word. COSTLY!
                {
                    final int maxWordIndex = params.genericEmissions.size();// - 2; // don't consider start and end symbol here
                    for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                    {
                        final int w = wIter;
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                            double baseProb;
                            public double getWeight() {
                                baseProb = get(baseEventTypeParams.genChoices[field], 
                                        Parameters.G_FIELD_GENERIC) * get(baseline.genericEmissions, w);
                                return  getCount(modelEventTypeParams.genChoices[field], 
                                        Parameters.G_FIELD_GENERIC) + getCount(params.genericEmissions, w) +
                                        getBaselineScore(baseProb);
                            }
                            public void setPosterior(double prob) { }
                            public GenWidget choose(GenWidget widget) {                            
                                widget.getText()[i] = w;
                                widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                                Feature[] featuresArray = {
                                    new Feature(modelEventTypeParams.genChoices[field], 
                                            Parameters.G_FIELD_GENERIC),
                                    new Feature(params.genericEmissions, w)                                
                                };
                                increaseCounts(featuresArray, normalisedLog(baseProb));
                                return widget;
                            }
                            public Pair getWeightAtRank(int rank) // used for k-best
                            {return null;}
                            public GenWidget chooseWord(GenWidget widget, int word)
                            {return widget;}
                            });
                    } // for
                } // else                
            } // else field f
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
                                   new Hypergraph.HyperedgeInfoOnline<GenWidget>() {
                    int i = begin; int j = end;
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public double getOnlineWeight(List<List<Integer>> ngrams){
                        return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                               getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {
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
                hypergraph.addEdge(node, list, new Hypergraph.HyperedgeInfoOnline<Widget>()
                {
//                    List<Integer> ngramIndices;
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public double getOnlineWeight(List<List<Integer>> ngrams){
                        return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                               getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
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
                                       new Hypergraph.HyperedgeInfoOnline<GenWidget>() {
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
                                        getCount(((DiscriminativeEventTypeParams)modelEventTypeParams).numberOfWordsPerField[fIter], j - i - 1) +
                                        getBaselineScore(baseParam);
                        }
                        public double getOnlineWeight(List<List<Integer>> ngrams)
                        {
                           return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                                  getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
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
                                            modelEventTypeParams.boundary_f),
                                new Feature(((DiscriminativeEventTypeParams)modelEventTypeParams).numberOfWordsPerField[fIter], j - i - 1)
                            };
                            increaseCounts(featuresArray, normalisedLog(baseParam));
                            return widget;
                        }   
                   });
                } // if
                else
                {
                    hypergraph.addEdge(node, genField(i, j, c, event, f),
                                       genFields(j, end, c, event, remember_f, new_efs),
                                       new Hypergraph.HyperedgeInfoOnline<GenWidget>() {
                        double baseParam; Vec weights;  
                        public double getWeight() {                            
                            baseParam = get(baseEventTypeParams.fieldChoices[f0], fIter);                           
                            weights = modelEventTypeParams.fieldChoices[f0];
                            return calculateOracle ?
                                    baseParam : getCount(weights, fIter) + 
                                    getCount(((DiscriminativeEventTypeParams)modelEventTypeParams).numberOfWordsPerField[fIter], j - i - 1) + 
                                    getBaselineScore(baseParam);
                        }
                        public void setPosterior(double prob) { }
                        public double getOnlineWeight(List<List<Integer>> ngrams)
                        {
                           return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                                  getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
                        }
                        public GenWidget choose(GenWidget widget) {                            
                            for(int k = i; k < j; k++)
                            {
                                widget.getFields()[c][k] = fIter;
                            }
                            Feature[] featuresArray = {new Feature(weights, fIter),
                            new Feature(((DiscriminativeEventTypeParams)modelEventTypeParams).numberOfWordsPerField[fIter], j - i - 1)};
                            increaseCounts(featuresArray, normalisedLog(baseParam));
                            return widget;
                        }                      
                    });
                } // else
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
                                   new Hypergraph.HyperedgeInfoOnline<Widget>() {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public double getOnlineWeight(List<List<Integer>> ngrams){
                        return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                               getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
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
                hypergraph.addEdge(node, list, new Hypergraph.HyperedgeInfoOnline<Widget>()
                {
                    public double getWeight() {
                        return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                    }
                    public double getOnlineWeight(List<List<Integer>> ngrams){
                        return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                               getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
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
            if(useKBest)
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {                
                public double getWeight() {return 0.0d;}
                public Pair getWeightAtRank(int rank)
                {                   
                    return getAtRank(emissionsParams.getNoneEventTypeEmissions(), rank);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {return widget;}
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    Feature[] featuresArray = {new Feature(params.trackParams[c].getNoneEventTypeEmissions(), word)};
                    increaseCounts(featuresArray, normalisedLog(get(baseline.trackParams[c].getNoneEventTypeEmissions(), word)));
                    return widget;
                }
                });
            }
            else
            {
                // add hyperedge for each word. COSTLY!
                final int maxWordIndex = params.trackParams[c].getNoneEventTypeEmissions().size();// - 2; // don't consider start and end symbol here
                for(int wIter = 0; wIter < (opts.modelUnkWord ? maxWordIndex : maxWordIndex - 1); wIter++)
                {
                    final int w = wIter;
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        double baseParam; Vec weights;
                        public double getWeight() 
                        {
                            baseParam = get(baseline.trackParams[c].getNoneEventTypeEmissions(), w);
                            weights = params.trackParams[c].getNoneEventTypeEmissions();
                            return getCount(weights, w) + getBaselineScore(baseParam);
                        }
                        public Pair getWeightAtRank(int rank)
                        {return  null;}
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) 
                        {                         
                           widget.getText()[i] = w;
                           Feature[] featuresArray = {new Feature(weights, w)};
                           increaseCounts(featuresArray, normalisedLog(baseParam));
                           return widget;
                        }
                        public GenWidget chooseWord(GenWidget widget, int word)
                        {return widget;}
                    });
                } // for
            } // else            
        } // if
        return node;
    }

    protected StopNode genStopNode(int i, final int t0, final TrackParams modelCParams, 
                                                        final TrackParams baseCParams)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<Widget>() {
                double baseProb; Vec alignWeights;                
                public double getWeight() {
                    alignWeights = modelCParams.getEventTypeChoices()[modelCParams.boundary_t];                    
                    baseProb = get(baseCParams.getEventTypeChoices()[t0], modelCParams.boundary_t);
                    return calculateOracle ? baseProb :
                            getCount(alignWeights, modelCParams.boundary_t) + getBaselineScore(baseProb);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {                    
                    return chooseWord(widget, -1);
                }

                @Override
                public Pair getWeightAtRank(int rank)
                {
                    return rank > 0 ? null : new Pair(getWeight(), vocabulary.getIndex("</s>"));
                }

                @Override
                public Widget chooseWord(Widget widget, int word)
                {
                    Feature[] featuresArray = {new Feature(alignWeights, modelCParams.boundary_t)};
                    increaseCounts(featuresArray, normalisedLog(baseProb));
                    return widget;
                }
            });
        } // if
        return node;
    }

    /**
     * Generate track c in i...j (t0 is previous event type for track 0);
     * allowNone and allowReal specify what event types we can use
     */ 
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
                  new Hypergraph.HyperedgeInfoOnline<GenWidget>() {
                      double baseProb; Vec alignWeights;
                      public double getWeight() {                         
                          baseProb = get(baseCParams.getEventTypeChoices()[t0], 
                                 baseCParams.none_t);
                          alignWeights = modelCParams.getEventTypeChoices()[t0];
                          return calculateOracle ? 
                                 baseProb :
                                  // pecreptron weight * 1 (omitted, since 
                                 // it is equivalent to the count of the 
                                 // alignment inferState rule)
                                 getCount(alignWeights, modelCParams.boundary_t) + 
                                 getBaselineScore(baseProb);
                         
                      }
                      public double getOnlineWeight(List<List<Integer>> ngrams)
                      {
                          return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                                 getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
                      }
                      public void setPosterior(double prob) {}
                      public GenWidget choose(GenWidget widget) {
                          for(int k = i; k < j; k++)
                          {
                              widget.getEvents()[c][k] = Parameters.none_e;
                          }
                          Feature[] featuresArray = {new Feature(alignWeights, modelCParams.boundary_t)};
                          increaseCounts(featuresArray, normalisedLog(baseProb));
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
                            new Hypergraph.HyperedgeInfoOnlineFields<GenWidget>() {
                      double baseProb; Vec alignWeights;
                      public double getWeight()
                      {
                          baseProb = get(baseCParams.getEventTypeChoices()[t0], eventTypeIndex) *
                                  (1.0/(double)ex.getEventTypeCounts()[eventTypeIndex]);                          
                          alignWeights = modelCParams.getEventTypeChoices()[t0];
                          return calculateOracle ? baseProb : 
                                  getCount(alignWeights, eventTypeIndex) + 
                                  getBaselineScore(baseProb);                         
                      }
                      public double getOnlineWeight(List<List<Integer>> ngrams)
                      {
                          return getNgramWeights(ngrams) + getLMWeights(ngrams) + 
                                 getHasConsecutiveWordsWeight(ngrams) + getHasConsecutiveNgramsWeight(ngrams, 2);
                      }
                      public double getOnlineWeightFields(List<List<Integer>> fieldNgrams, int numOfFields)
                      {
                          return getFieldNgramWeights(fieldNgrams, eventTypeIndex) + 
                                 getNumFieldsWeight(numOfFields);
                      }
                      public void setPosterior(double prob) {}
                      public GenWidget choose(GenWidget widget) {
                          for(int k = i; k < j; k++)
                          {
                              widget.getEvents()[c][k] = eventId;
                          }                          
                          Feature[] featuresArray = {new Feature(alignWeights, eventTypeIndex)};
                          increaseCounts(featuresArray, normalisedLog(baseProb));                          
                          return widget;
                      }
                  });                  
              } // if
          } // for
        } // if        
        return node;
    }

    /**
     * Generate segmentation of i...N into event types; previous event type is t0
     * Incorporate eventType distributions
     */ 
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
        int c = 0; // assume we only have 1 track (TODO: may need to remove that)
        hypergraph.addEdge(node, genTrack(i, j, t0, c, opts.allowNoneEvent, true),
            new Hypergraph.HyperedgeInfoOnline<Widget>() {
//                List<Integer> ngramIndices;
                public double getWeight() {
                    return calculateOracle ? 1.0 : 0.0; // remember we are in log space (or just counting) during reranking
                }
                public double getOnlineWeight(List<List<Integer>> ngrams){
                    return getNgramWeights(ngrams) + getLMWeights(ngrams);
                }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) {
//                        if(useKBest)
//                            increaseCounts(getNgramFeatures(((DiscriminativeParams)params).ngramWeights, ngramIndices));
                    return widget;
                }
            });
    }
    
    private void testGetHasConsecutiveNgramsWeight()
    {
        List<Integer> l1 = Arrays.asList(new Integer[]{0, 1, 2});
        List<Integer> l2 = Arrays.asList(new Integer[]{1, 2, 0});
        List<Integer> l3 = Arrays.asList(new Integer[]{2, 0, 1});
        List<Integer> l4 = Arrays.asList(new Integer[]{0, 1, 2});
        List<List<Integer>> l = new ArrayList(); l.add(l1); l.add(l2); l.add(l3); l.add(l4);
        getHasConsecutiveNgramsWeight(l, 3);
    }
}