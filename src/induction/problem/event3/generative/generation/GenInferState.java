package induction.problem.event3.generative.generation;

import induction.problem.event3.generative.alignment.InferState;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.Indexer;
import fig.basic.StopWatchSet;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.NumFieldParams;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.BigDouble;
import induction.DepHead;
import induction.Hypergraph;
import induction.Hypergraph.HyperpathResult;
import induction.Utils;
import induction.ngrams.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.dmv.params.DMVParams;
import induction.problem.event3.CatField;
import induction.problem.event3.Event;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.NumField;
import induction.problem.event3.StrField;
import induction.problem.event3.SymField;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.nodes.FieldsNode;
import induction.problem.event3.nodes.NoneEventWordsNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.SelectNoEventsNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.StringFieldValueNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.TrackParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class GenInferState extends InferState
{
    Graph graph;
    //public static final int EXTRA_VOCABULARY_SYMBOLS = 5;
    protected NgramModel ngramModel, secondaryNgramModel;
    protected Indexer<String> vocabulary;

    public GenInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec);
        this.ngramModel = ngramModel;
        this.secondaryNgramModel = model.getSecondaryNgramModel();
    }

    public GenInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel, Graph graph)
    {
        this(model, ex, params, counts, ispec, ngramModel);
        this.graph = graph;
    }

    @Override
    protected void initInferState(AModel model)
    {
        wildcard_pc = -1;
        L = opts.maxPhraseLength;
        segPenalty = new double[L + 1];
        for(int l = 0; l < L +1; l++)
        {
            segPenalty[l] = Math.exp(-Math.pow(l, opts.segPenalty));
        }
//        segPenalty[1] = 1;
//        segPenalty[2] = 1;
//        segPenalty[3] = 1;
//        segPenalty[4] = 1;
//        segPenalty[5] = 0.05;
//        segPenalty[6] = 0.9;
        N = ex.N();
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
//                vocabulary.getIndex("<s>"),
//                vocabulary.getIndex("</s>"),
//                opts.ngramWrapper != Options.NgramWrapper.roark,
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
            list.add(genEvents(0, ((Event3Model)model).boundary_t()));
//            list.add(test());
            if(!opts.useStopNode)
                list.add(endSymbol);
            this.hypergraph.addEdge(hypergraph.sumStartNode(), list,
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
        } // else
    }            

    @Override
    public void doInference()
    {
        HyperpathResult result;
        if(opts.fullPredRandomBaseline)
        {
            StopWatchSet.begin("1-best Viterbi");
            result = hypergraph.oneBestViterbi(newWidget(), opts.initRandom);
            StopWatchSet.end();
        }
        else
        {
            StopWatchSet.begin("k-best Viterbi");
            result = hypergraph.kBestViterbi(newWidget());
            StopWatchSet.end();
        }
        bestWidget = (Widget) result.widget;
//            System.out.println(bestWidget);
        logVZ = result.logWeight;
        updateStats();
    }       
    
    public BigDouble getDepDerivationWeight(DepHead head, DepHead argument, int direction)
    {
//        BigDouble weight = new BigDouble(argument.getWeight());        
        BigDouble weight = BigDouble.one();
        boolean adj = Math.abs(head.getPos() - argument.getPos()) == 1;
        int r;
        if(adj)
        {
            r = direction == induction.problem.dmv.Constants.D_LEFT ?
                    induction.problem.dmv.Constants.R_LEFT0 : 
                    induction.problem.dmv.Constants.R_RIGHT0;
        }        
        else
        {
            r = direction == induction.problem.dmv.Constants.D_RIGHT ?
                    induction.problem.dmv.Constants.R_LEFT1 : 
                    induction.problem.dmv.Constants.R_RIGHT1;
        }
        DMVParams depsParams = getDepsParams();
//        System.out.println((((Event3Model)model).getDepsModel().wordToString(argument.getHead()))  + " " +
//                           (((Event3Model)model).getDepsModel().wordToString(head.getHead())));
        weight.mult(get(depsParams.continues[head.getHead()][r], induction.problem.dmv.Constants.F_CONT) *
                    get(depsParams.deps[head.getHead()][direction], 
                       ((Event3Model)model).getDepsModel().getLocalWordIndexer()[head.getHead()].indexOf(argument.getHead())) *
                    get(depsParams.continues[head.getHead()][r], induction.problem.dmv.Constants.F_STOP));
        return weight;
    }
    
    @Override
    protected Object genNumFieldValue(final int i, final int c, final int event, final int field, final int v)
    {
        NumFieldValueNode node = new NumFieldValueNode(i, c, event, field);
        if (hypergraph.addSumNode(node))
        {
            // Consider generating nums(i) from v            
            final NumFieldParams fparams = getNumFieldParams(event, field);

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDUP);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDUP), vocabulary.getIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDUP;
                    widget.getNums()[i] = roundUp(v);
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDUP;
                    widget.getNums()[i] = roundUp(v);
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDDOWN);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDDOWN), vocabulary.getIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDDOWN;
                    widget.getNums()[i] = roundDown(v);
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDDOWN;
                    widget.getNums()[i] = roundDown(v);
//                    widget.text[i] = word;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDCLOSE);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDCLOSE), vocabulary.getIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDCLOSE;
                    widget.getNums()[i] = roundClose(v);
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_ROUNDCLOSE;
                    widget.getNums()[i] = roundClose(v);
                    return widget;
                }
            });

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_IDENTITY);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_IDENTITY), vocabulary.getIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_IDENTITY;
                    widget.getNums()[i] = v;
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_IDENTITY;
                    widget.getNums()[i] = v;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {

                final double CONT = get(fparams.rightNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(fparams.rightNoiseChoices, Parameters.S_STOP);
                final int NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_NOISEUP);
//                    return get(fparams.methodChoices, Parameters.M_NOISEUP) * 0.5 *
//                               Math.pow(get(fparams.rightNoiseChoices,
//                               Parameters.S_CONTINUE), NOISE_MINUS_ONE) *
//                               get(fparams.rightNoiseChoices, Parameters.S_STOP);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEUP),
                                    vocabulary.getIndex("<num>"));
//                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEUP) * 0.5 *
//                                    Math.pow(get(fparams.rightNoiseChoices,
//                                    Parameters.S_CONTINUE), NOISE_MINUS_ONE) *
//                                    get(fparams.rightNoiseChoices, Parameters.S_STOP),
//                                    ((Event3Model)model).getWordIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_NOISEUP;
                    widget.getNums()[i] = NOISE_MINUS_ONE + 1 + v;
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_NOISEUP;
                    widget.getNums()[i] = NOISE_MINUS_ONE + 1 + v;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {

                final double CONT = get(fparams.leftNoiseChoices, Parameters.S_CONTINUE);
                final double STOP = get(fparams.leftNoiseChoices, Parameters.S_STOP);
                final int MINUS_NOISE_MINUS_ONE = (int) Math.round(CONT / STOP);
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_NOISEDOWN);
//                    return get(fparams.methodChoices, Parameters.M_NOISEDOWN) *
//                               Math.pow(get(fparams.leftNoiseChoices,
//                               Parameters.S_CONTINUE), MINUS_NOISE_MINUS_ONE) *
//                               get(fparams.leftNoiseChoices, Parameters.S_STOP);
                }
                public Pair getWeightAtRank(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEDOWN),
                                    vocabulary.getIndex("<num>"));
//                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEDOWN) *
//                                    Math.pow(get(fparams.leftNoiseChoices,
//                                    Parameters.S_CONTINUE), MINUS_NOISE_MINUS_ONE) *
//                                    get(fparams.leftNoiseChoices, Parameters.S_STOP),
//                                    ((Event3Model)model).getWordIndex("<num>"));
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafCDDepHead(i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.getNumMethods()[c][i] = Parameters.M_NOISEDOWN;
                    widget.getNums()[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    return widget;
                }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getNumMethods()[c][i] = Parameters.M_NOISEDOWN;
                    widget.getNums()[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    return widget;
                }
            });
        } // if (hypergraph.addSumNode(node))
        return node;
    }

    @Override
    protected  Object genCatFieldValueNode(final int i, int c, final int event, final int field)
    {
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        final CatFieldParams fparams = getCatFieldParams(event, field);
        // Consider generating words(i) from category v
        final int v = getValue(event, field);
        // (for generation only) in case the test set contains values that are not in the training set
        if (v >= fparams.emissions.length)
        {
            return hypergraph.invalidNode;
        }
        if(hypergraph.addSumNode(node))
        {            
            if(opts.fullPredRandomBaseline)
            {
                    final int w = BigDouble.normalizeAndSample(opts.fullPredRandom,
                            fparams.emissions[v].getCounts());
                    // Talk about the event type, not a particular field
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<GenWidget>() {
                    public double getWeight() {
                        return get(fparams.emissions[v], w);
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) {
                        widget.getText()[i] = w;
                        return widget;
                    }
                    });
            }
            else
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() {
                    return 1.0d;
                }
                public Pair getWeightAtRank(int rank)
                {                    
                    return getAtRank(fparams.emissions[v], rank);                    
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafDepHead(word, i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    return widget;
                }
                });
            }
        }
        return node;
    }

    protected Object genStrFieldValue(final int i, int c, int event, int field, final int posInFieldValue)
    {
        if(posInFieldValue == -1)
            return hypergraph.invalidNode;
        final ArrayList<Integer> valueWords = new ArrayList<Integer>();
        ((StrField)getField(event, field)).getWordLabel(getValue(event, field)).getWordsLabels(valueWords, null);
        StringFieldValueNode node = new StringFieldValueNode(i, c, event, field, posInFieldValue);
//            final StrFieldParams fparams = getStrFieldParams(event, field);
        if (hypergraph.addSumNode(node))
        {                            
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return 1.0;
                }
                public void setPosterior(double prob) { }
               
                @Override
                public Pair getWeightAtRank(int rank)
                {
                    if(rank > 0)
                        return null;
                    return new Pair(1.0, valueWords.get(posInFieldValue));
                }

                @Override
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    return widget;
                }

                @Override
                public GenWidget choose(GenWidget widget)
                {return widget;}
            });            
        }
        return node;

    }

    protected Object genFieldValue(int i, int c, int event, int field, int posInFieldValue)
    {
        Field tempField = getField(event, field);
        if(tempField instanceof NumField) return genNumFieldValue(i, c, event, field);
        else if(tempField instanceof CatField) return genCatFieldValueNode(i, c, event, field);
        else if(tempField instanceof SymField) return genSymFieldValue(i, c, event, field);
        else if(tempField instanceof StrField) return genStrFieldValue(i, c, event, field, posInFieldValue);
        else return Utils.impossible();
    }
    
    // Generate word at position i with event e and field f
//    @Override
    protected WordNode genWord(final int i, final int c, int event, final int field, final int posInFieldValue)
    {
        WordNode node = new WordNode(i, c, event, field, posInFieldValue);
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
//        final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];

        if(hypergraph.addSumNode(node))
        {
            if(field == eventTypeParams.none_f)
            {
                if(opts.fullPredRandomBaseline)
                {
                    final int w = BigDouble.normalizeAndSample(opts.fullPredRandom,
                            eventTypeParams.noneFieldEmissions.getCounts());
                        // Talk about the event type, not a particular field
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<GenWidget>() {
                        public double getWeight() {
                            return get(eventTypeParams.noneFieldEmissions, w) *
                                   getEventTypeGivenWord(eventTypeIndex, w);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            widget.getText()[i] = w;
                            return widget;
                        }
                        });
                }
                else
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                        public double getWeight() {
                            return 1.0;
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) { return widget; }
                        public Pair getWeightAtRank(int rank)
                        {
                             return getAtRank(eventTypeParams.noneFieldEmissions, rank);
                        }
                        public Pair getDepWeight(int word)
                        {
                            return getLeafDepHead(word, i);
                        }
                        public GenWidget chooseWord(GenWidget widget, int word)
                        {
                            widget.getText()[i] = word;
                            return widget;
                        }
                        });
                }
            } // if
            else
            {
                // G_FIELD_VALUE: generate based on field value                
                hypergraph.addEdge(node, genFieldValue(i, c, event, field, posInFieldValue),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
                    return get(eventTypeParams.genChoices[field], Parameters.G_FIELD_VALUE);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
                });
                // G_FIELD_GENERIC: generate based on event type
                if(opts.fullPredRandomBaseline)
                {
                    final int w = BigDouble.normalizeAndSample(opts.fullPredRandom,
                            params.genericEmissions.getCounts());
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<GenWidget>() {
                        public double getWeight() {
                            return get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC) *
                                   get(params.genericEmissions, w) *
                                   getEventTypeGivenWord(eventTypeIndex, w);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                            widget.getText()[i] = w;
                            return widget;
                        }
                        });
                }
                else
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        public double getWeight() {
                            return 1.0;
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) { return widget; }
                        public Pair getWeightAtRank(int rank)
                        {
                            Pair p =  getAtRank(params.genericEmissions, rank);
                            p.value *= get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
                            return p;
                        }
                        public Pair getDepWeight(int word)
                        {
                            return getLeafDepHead(word, i);              
                        }
                        public GenWidget chooseWord(GenWidget widget, int word)
                        {
                            widget.getText()[i] = word;
                            return widget;
                        }
                        });
                }
            } // else
        }
        return node;
    }     

    // Generate field f of event e from begin to end
//    @Override
    protected Object genField(final int begin, final int end, int c, int event, final int field, final int posInFieldValue)
    {
        boolean incrPosInFieldValue = posInFieldValue != -1;
        FieldNode node = new FieldNode(begin, end, c, event, field, posInFieldValue);
        if(opts.fullPredRandomBaseline)
        {
            if(hypergraph.addProdNode(node))
            {
                int iIter = 0;
                for(int i = begin; i < end; i++) // Generate each word in this range independently
                {
                    hypergraph.addEdge(node, genWord(i, c, event, field, posInFieldValue + iIter));
                    iIter = incrPosInFieldValue ? iIter + 1 : 0;
                }
            }
        }
        else if(opts.binariseAtWordLevel)
        {
            if (begin == end)
            {
                return hypergraph.endNode;
            }
            if(hypergraph.addSumNode(node))
            {
                hypergraph.addEdge(node,
                                   genWord(begin, c, event, field, posInFieldValue),
                                   genField(begin + 1, end, c, event, field, 
                                   incrPosInFieldValue ? posInFieldValue + 1 : posInFieldValue));
            }
        }
        else
        {
            if(hypergraph.addSumNode(node))
            {
                ArrayList<WordNode> list = new ArrayList(end - begin);
                int iIter = 0;
                for(int i = begin; i < end; i++) // Generate each word in this range independently
                {
                    list.add(genWord(i, c, event, field, posInFieldValue + iIter)); 
                    iIter = incrPosInFieldValue ? iIter + 1 : 0;
                }
                hypergraph.addEdge(node, list);
            }
        }
        return node;
    }   

     // Generate segmentation of i...end into fields; previous field is f0
    @Override
    protected Object genFields(final int i, final int end, int c, final int event, final int f0, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        FieldsNode node = new FieldsNode(i, end, c, event, f0, efs);
        if(hypergraph.addSumNode(node))
        {
            if(oneFieldPerEvent())
            {
                selectJ(end, i, end, c, event, f0, efs, eventTypeParams, null, node);
            }
            else if(newFieldPerWord())
            {
                selectJ(i+1, i, end, c, event, f0, efs, eventTypeParams, null, node);
            }
            else
            {
                for(int k = i+1; k < end+1; k++)
                {
                    selectJ(k, i, end, c, event, f0, efs, eventTypeParams, null, node);
                }
            }
        } // if
        return node;
    }

    // Choose ending position j
    @Override
    protected void selectJ(final int j, final int i, int end, final int c, final int event,
                         final int f0, int efs,
                         final EventTypeParams eventTypeParams,
                         final EventTypeParams eventTypeCounts, FieldsNode node)
    {
        // Choose a new field to talk about (including none field, but not boundary)
        for(int f = 0; f < ex.events.get(event).getF() + 1; f++)
        {
            final int fIter = f;
            if(f == eventTypeParams.none_f || // If not none, then...
               ((!opts.disallowConsecutiveRepeatFields || f != f0) && // Can't repeat fields
               eventTypeParams.efs_canBePresent(efs, f) && // Make sure f can be there
               (!opts.limitFieldLength ||
               j-i <= ex.events.get(event).getFields()[f].getMaxLength())))
            { // Limit field length
                int remember_f = indepFields() ? eventTypeParams.boundary_f : f;
                int new_efs = (f == eventTypeParams.none_f) ? efs :
                    eventTypeParams.efs_addAbsent(efs, f); // Now, allow f to be absent as we've already taken care of it

                if(j == end)
                {
                    hypergraph.addEdge(node, genField(i, j, c, event, f, -1),
                                       new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        public double getWeight() { // final field-phrase before boundary                            
                                return get(eventTypeParams.fieldChoices[f0], fIter) *
                                       get(eventTypeParams.fieldChoices[fIter],
                                           eventTypeParams.boundary_f);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            for(int k = i; k < j; k++)
                            {
                                widget.getFields()[c][k] = fIter;
                            }
                            return widget;
                        }

                        @Override                        
                        public Pair getWeightAtRank(int rank)
                        { // semantic parsing only
                            return new Pair(getWeight(),
                                    fIter < ex.events.get(event).getF() ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].getName().toLowerCase()) :
                                        vocabulary.getIndex("none_f"));
                        }

                        @Override
                        public GenWidget chooseWord(GenWidget widget, int word)
                        {
                            return widget;
                        }
                    });
                } // if
                else
                {
                    int posInFieldValue = f == ex.events.get(event).getF() ? -1 :
                            (getField(event, f) instanceof StrField && 
                            ((StrField)getField(event, f)).valueNumOfWords(getValue(event, f)) == j-i ? 0 : -1);
                    
                    hypergraph.addEdge(node, genField(i, j, c, event, f, posInFieldValue),
                                       genFields(j, end, c, event, remember_f, new_efs),
                                       new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        public double getWeight() {
                            if (prevIndepFields()) // f0 == boundary_f under indepFields, so use that
                                return get(eventTypeParams.fieldChoices[eventTypeParams.boundary_f], fIter);
                            else
                                return get(eventTypeParams.fieldChoices[f0], fIter);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            for(int k = i; k < j; k++)
                            {
                                widget.getFields()[c][k] = fIter;
                            }
                            return widget;
                        }

                        @Override
                        public Pair getWeightAtRank(int rank)
                        { // semantic parsing only
                            return new Pair(getWeight(), 
                                    fIter < ex.events.get(event).getF() ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].getName().toLowerCase()) :
                                        vocabulary.getIndex("none_f"));
                        }

                        @Override
                        public GenWidget chooseWord(GenWidget widget, int word)
                        {
                            return widget;
                        }
                    });
                } // else
            } // if
        } // for
    }

    // Default: don't generate any event (there should be only one of these nodes)
    // Note: we don't need any state, but include i and c so that we get distinct
    // nodes (see note in Hypergraph)
    @Override
    protected Object selectNoEvents(final int i, int c)
    {
        if (ex.events.isEmpty())
            return hypergraph.endNode;
        else
        {
            SelectNoEventsNode node = new SelectNoEventsNode(i, c);
            if (hypergraph.addProdNode(node))
            {
                for(final Event ev: ex.events.values())
                {
                    final int eventTypeIndex = ev.getEventTypeIndex();
                    final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                        public double getWeight() {
                                return get(eventTypeParams.filters, Parameters.B_FALSE);
                        }
                        public void setPosterior(double prob) {}
                        public Pair getWeightAtRank(int rank)
                        {
                            return new Pair(getWeight(), null);
                        }
                        public Pair getDepWeight(int word)
                        {
                            return getLeafDepHead(word, i);
                        }
                        public GenWidget chooseWord(GenWidget widget, int word) {return widget;}
                        public GenWidget choose(GenWidget widget) {return widget;}
                    });
                } // for
            } // if
            return node;
        } // else
    }

    @Override
    protected Object genNoneEventWords(final int i, final int j, final int c)
    {
        NoneEventWordsNode node = new NoneEventWordsNode(i, j, c);
        if(opts.fullPredRandomBaseline)
        {
            if(hypergraph.addProdNode(node))
            {
                for(int k = i; k < j; k++) // Generate each word in this range independently
                {
                    final int kIter = k;
                        final int w = BigDouble.normalizeAndSample(opts.fullPredRandom,
                                params.trackParams[c].getNoneEventTypeEmissions().getCounts());
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<GenWidget>() {
                        public double getWeight() {
                                return get(params.trackParams[c].getNoneEventTypeEmissions(), w) *
                                       getEventTypeGivenWord(params.trackParams[c].none_t, w);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            widget.getText()[kIter] = w;
                            return widget;
                        }
                        });
                } // for
            }
        }
        else if(opts.binariseAtWordLevel)
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
                        return 1.0;
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
                        return 1.0;
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
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoDepLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public Pair getWeightAtRank(int rank)
                {
                    return getAtRank(params.trackParams[c].getNoneEventTypeEmissions(), rank);
                }
                public Pair getDepWeight(int word)
                {
                    return getLeafDepHead(word, i);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public GenWidget chooseWord(GenWidget widget, int word)
                {
                    widget.getText()[i] = word;
                    return widget;
                }
            });
        }
        return node;
    }

    @Override
    protected StopNode genStopNode(final int i, final int t0, final TrackParams cparams, final TrackParams ccounts)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<Widget>() {
                public double getWeight() {
                    if (prevIndepEventTypes())
                        return 1.0;
                    else
                        return get(cparams.getEventTypeChoices()[t0],
                                cparams.boundary_t);
                }
                public void setPosterior(double prob) {}
                public Widget choose(Widget widget) {
                    return widget;
                }

                @Override
                public Pair getWeightAtRank(int rank)
                {
                    if(rank > 0)
                        return null;
                    return new Pair(getWeight(), vocabulary.getIndex("</s>"));
//                    return new Pair(1.0, vocabulary.getIndex("</s>"));
                }
//                public Pair getDepWeight(int word) // CAREFUL!
//                {
//                    return getLeafDepHead(word, i);
//                }
                @Override
                public Widget chooseWord(Widget widget, int word)
                {
                    return widget;
                }
            });
        } // if
        return node;
    }

    @Override
    protected void selectEnd(int j, EventsNode node, int i, int t0)
    {
        hypergraph.addEdge(node, genTrack(i, j, t0, 0, opts.allowNoneEvent, true));
    }
}