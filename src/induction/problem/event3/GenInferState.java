package induction.problem.event3;

import fig.basic.Indexer;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.NumFieldParams;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.BigDouble;
import induction.Hypergraph;
import induction.NgramModel;
import induction.Options;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.nodes.FieldsNode;
import induction.problem.event3.nodes.NoneEventWordsNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.SelectNoEventsNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.WordNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class GenInferState extends InferState
{
    //public static final int EXTRA_VOCABULARY_SYMBOLS = 5;
    protected NgramModel ngramModel;
    protected Indexer<String> vocabulary;

    public GenInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec);        
        this.ngramModel = ngramModel;        
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
        N = ex.N();
        this.vocabulary = ((Event3Model)model).getWordIndexer();
    }
        
    protected int[] newMatrixOne()
    {
        int[] out = new int[ex.N()];
        for(int i = 0; i < out.length; i++)
        {
            Arrays.fill(out, -1);
        }
        return out;
    }

    @Override
    protected Widget newWidget()
    {       
//        int[] eventTypeIndices = new int[ex.events.length];
//        for(int i = 0; i < eventTypeIndices.length; i++)
//        {
//           eventTypeIndices[i] = ex.events[i].getEventTypeIndex();
//        }
        HashMap<Integer, Integer> eventTypeIndices =
                            new HashMap<Integer, Integer>(ex.events.size());
        for(Event e : ex.events.values())
        {
            eventTypeIndices.put(e.id, e.getEventTypeIndex());
        }
        return new GenWidget(newMatrix(), newMatrix(), newMatrix(), newMatrix(),
                               newMatrixOne(),
                               ((Event3Model)model).eventTypeAllowedOnTrack, eventTypeIndices);
    }
    
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {        
        // setup hypergraph preliminaries
        hypergraph.setupForGeneration(opts.debug, opts.modelType, true, opts.kBest, ngramModel, opts.ngramSize,
                opts.reorderType, opts.allowConsecutiveEvents,
                /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
                vocabulary.getIndex("<num>"),
                vocabulary.getIndex("ELIDED_SYMBOL"),
                vocabulary.getIndex("<s>"),
                vocabulary.getIndex("</s>"),
                opts.ngramWrapper != Options.NgramWrapper.roark,
                vocabulary, ex);
//        for(int i = 0; i < hypergraph.wordIndexer.size(); i++)
//        {
//            System.out.println(String.format("%d -> %s", i, hypergraph.wordIndexer.getObject(i)));
//        }

        if(opts.fullPredRandomBaseline)
        {
            this.hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).none_t()),
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
        } // if
        else
        {
//            ProbVec[] eventTransMatrix = params.trackParams[0].eventTypeChoices;
//            int length = eventTransMatrix.length;
//            for(int i = 1; i < length; i++)
//            {
//                for(int j = i - 1; j >= 0; j--)
//                {
//                    eventTransMatrix[i].set(j, 0);
//                }
//            }
            WordNode startSymbol = new WordNode(-1, 0, -1, -1);
            hypergraph.addSumNode(startSymbol);
            WordNode endSymbol = new WordNode(ex.N() + 1, 0, -1, -1);
            hypergraph.addSumNode(endSymbol);
            this.hypergraph.addEdge(startSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
            {
                public double getWeight()
                { return 1;}
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
            this.hypergraph.addEdge(endSymbol, new Hypergraph.HyperedgeInfoLM<GenWidget>()
            {
                public double getWeight()
                { return 1;}
                public Pair getWeightLM(int rank)
                {
                    if(rank > 0)
                        return null;
                    return new Pair(1.0, vocabulary.getIndex("</s>"));
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
            list.add(genEvents(0, ((Event3Model)model).none_t()));
//            list.add(test());
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
    protected Object genNumFieldValue(final int i, final int c, final int event, final int field, final int v)
    {
        NumFieldValueNode node = new NumFieldValueNode(i, c, event, field);
        if (hypergraph.addSumNode(node))
        {
            // Consider generating nums(i) from v            
            final NumFieldParams fparams = getNumFieldParams(event, field);

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDUP);
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDUP), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_ROUNDUP;
                    widget.nums[i] = roundUp(v);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_ROUNDUP;
                    widget.nums[i] = roundUp(v);
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDDOWN);
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDDOWN), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_ROUNDDOWN;
                    widget.nums[i] = roundDown(v);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_ROUNDDOWN;
                    widget.nums[i] = roundDown(v);
//                    widget.text[i] = word;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_ROUNDCLOSE);
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_ROUNDCLOSE), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_ROUNDCLOSE;
                    widget.nums[i] = roundClose(v);
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_ROUNDCLOSE;
                    widget.nums[i] = roundClose(v);
                    return widget;
                }
            });

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return get(fparams.methodChoices, Parameters.M_IDENTITY);
                }
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices,
                                        Parameters.M_IDENTITY), vocabulary.getIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_IDENTITY;
                    widget.nums[i] = v;
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_IDENTITY;
                    widget.nums[i] = v;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {

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
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEUP),
                                    vocabulary.getIndex("<num>"));
//                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEUP) * 0.5 *
//                                    Math.pow(get(fparams.rightNoiseChoices,
//                                    Parameters.S_CONTINUE), NOISE_MINUS_ONE) *
//                                    get(fparams.rightNoiseChoices, Parameters.S_STOP),
//                                    Event3Model.getWordIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_NOISEUP;
                    widget.nums[i] = NOISE_MINUS_ONE + 1 + v;
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_NOISEUP;
                    widget.nums[i] = NOISE_MINUS_ONE + 1 + v;
                    return widget;
                }
            });
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {

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
                public Pair getWeightLM(int rank) {
                    if(rank > 0)
                        return null;
                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEDOWN),
                                    vocabulary.getIndex("<num>"));
//                    return new Pair(get(fparams.methodChoices, Parameters.M_NOISEDOWN) *
//                                    Math.pow(get(fparams.leftNoiseChoices,
//                                    Parameters.S_CONTINUE), MINUS_NOISE_MINUS_ONE) *
//                                    get(fparams.leftNoiseChoices, Parameters.S_STOP),
//                                    Event3Model.getWordIndex("<num>"));
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) {
                    widget.numMethods[c][i] = Parameters.M_NOISEDOWN;
                    widget.nums[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    return widget;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.numMethods[c][i] = Parameters.M_NOISEDOWN;
                    widget.nums[i] = (-MINUS_NOISE_MINUS_ONE) - 1 + v;
                    return widget;
                }
            });
        } // if (hypergraph.addSumNode(node))
        return node;
    }

    @Override
    protected  CatFieldValueNode genCatFieldValueNode(final int i, int c, final int event, final int field)
    {
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        if(hypergraph.addSumNode(node))
        {
            final CatFieldParams fparams = getCatFieldParams(event, field);
            // Consider generating words(i) from category v
            final int v = getValue(event, field);

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
                        widget.text[i] = w;
                        return widget;
                    }
                    });
            }
            else
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() {
                    return 1.0d;
                }
                public Pair getWeightLM(int rank)
                {
                    return getAtRank(fparams.emissions[v], rank);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[i] = word;
                    return widget;
                }
                });
            }
        }
        return node;
    }

    // Generate word at position i with event e and field f
    @Override
    protected WordNode genWord(final int i, final int c, int event, final int field)
    {
        WordNode node = new WordNode(i, c, event, field);
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];

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
                            widget.text[i] = w;
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
                        public Pair getWeightLM(int rank)
                        {
                             return getAtRank(eventTypeParams.noneFieldEmissions, rank);
                        }
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            widget.text[i] = word;
                            return widget;
                        }
                        });
                }
            } // if
            else
            {
                // G_FIELD_VALUE: generate based on field value
                hypergraph.addEdge(node, genFieldValue(i, c, event, field),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
                    return get(eventTypeParams.genChoices[field], Parameters.G_FIELD_VALUE);
                }
                public void setPosterior(double prob) {
                    update(eventTypeCounts.genChoices[field], Parameters.G_FIELD_VALUE, prob);
                }
                public Widget choose(Widget widget) {
                    widget.gens[c][i] = Parameters.G_FIELD_VALUE;
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
                            widget.gens[c][i] = Parameters.G_FIELD_GENERIC;
                            widget.text[i] = w;
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
                        public Pair getWeightLM(int rank)
                        {
                            Pair p =  getAtRank(params.genericEmissions, rank);
                            p.value *= get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
                            return p;
                        }
                        public GenWidget chooseLM(GenWidget widget, int word)
                        {
                            widget.text[i] = word;
                            return widget;
                        }
                        });
                }
            } // else
        }
        return node;
    }     

    // Generate field f of event e from begin to end
    @Override
    protected FieldNode genField(int begin, int end, int c, int event, int field)
    {
        FieldNode node = new FieldNode(begin, end, c, event, field);
        if(opts.fullPredRandomBaseline)
        {
            if(hypergraph.addProdNode(node))
            {
                for(int i = begin; i < end; i++) // Generate each word in this range independently
                {
                    hypergraph.addEdge(node, genWord(i, c, event, field));
                }
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
                        return 1.0;
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
    @Override
    protected Object genFields(final int i, final int end, int c, final int event, final int f0, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
//        if(i == end)
//        {
//            // Make sure we've used all the fields we agreed to see
//            if (eventTypeParams.efs_canBeEmpty(efs))
//            {
//                if(indepFields())
//                    return hypergraph.endNode;
//                else
//                {
//                    FieldsNode node = new FieldsNode(end, end, c, event, f0, efs);
//                    if(hypergraph.addSumNode(node))
//                    {   // Transition to boundary_f
//                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
//                            public double getWeight() {
//                                if (prevIndepFields())
//                                    return 1.0;
//                                else
//                                    return get(eventTypeParams.fieldChoices[f0], eventTypeParams.boundary_f);
//                            }
//                            public Pair getWeightLM(int rank)
//                            {
//                                return new Pair(getWeight(), null);
//                            }
//                            public void setPosterior(double prob) { }
//                            public GenWidget choose(GenWidget widget) {
//
//                                return widget;
//                            }
//                            public GenWidget chooseLM(GenWidget widget, int word)
//                            {
////                                System.out.print(i + " " + Event3Model.wordToString(word));
//                                widget.text[i] = word;
//                                return widget;
//                            }
//                        });
//                    } // if
//                    return node;
//                } // else
//            } // if
//            else
//            {
//                return hypergraph.invalidNode;
//            }
//        } // if (i == end)
//        else
//        {
            FieldsNode node = new FieldsNode(i, end, c, event, f0, efs);
            if(hypergraph.addSumNode(node))
            {
                if(oneFieldPerEvent())
                {
                    selectJ(end, i, end, c, event, f0, efs, eventTypeParams, eventTypeCounts, node);
                }
                else if(newFieldPerWord())
                {
                    selectJ(i+1, i, end, c, event, f0, efs, eventTypeParams, eventTypeCounts, node);
                }
                else
                {
                    for(int k = i+1; k < end+1; k++)
                    {
                        selectJ(k, i, end, c, event, f0, efs, eventTypeParams, eventTypeCounts, node);
                    }
                }
            } // if
            return node;
//        } // else
    }

    // Choose ending position j
    @Override
    protected void selectJ(final int j, final int i, int end, final int c, final int event,
                         final int f0, int efs,
                         final EventTypeParams eventTypeParams,
                         final EventTypeParams eventTypeCounts, FieldsNode node)
    {
        // Choose a new field to talk about (including none field, but not boundary)
        for(int f = 0; f < ex.events.get(event).F + 1; f++)
        {
            final int fIter = f;
            if(f == eventTypeParams.none_f || // If not none, then...
               ((!opts.disallowConsecutiveRepeatFields || f != f0) && // Can't repeat fields
               eventTypeParams.efs_canBePresent(efs, f) && // Make sure f can be there
               (!opts.limitFieldLength ||
               j-i <= ex.events.get(event).getFields()[f].maxLength)))
            { // Limit field length
                int remember_f = indepFields() ? eventTypeParams.boundary_f : f;
                int new_efs = (f == eventTypeParams.none_f) ? efs :
                    eventTypeParams.efs_addAbsent(efs, f); // Now, allow f to be absent as we've already taken care of it

                if(j == end)
                {
                    hypergraph.addEdge(node, genField(i, j, c, event, f),
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
                                widget.fields[c][k] = fIter;
                            }
                            return widget;
                        }

                        @Override
                        public Pair getWeightLM(int rank)
                        {
                            return new Pair(getWeight(),
                                    fIter < ex.events.get(event).F ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].name.toLowerCase()) :
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
                        public double getWeight() {
                            if (prevIndepFields()) // f0 == boundary_f under indepFields, so use that
                                return get(eventTypeParams.fieldChoices[eventTypeParams.boundary_f], fIter);
                            else
                                return get(eventTypeParams.fieldChoices[f0], fIter);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
//                            System.out.println(String.format("event=%s, i=%d, j=%d, f0=%s, f=%s",
//                                  ex.events[event].toString(), i, j,
//                                  model.getEventTypes()[ex.events[event].getEventTypeIndex()].fieldToString(f0),
//                                  model.getEventTypes()[ex.events[event].getEventTypeIndex()].fieldToString(fIter)));
                            for(int k = i; k < j; k++)
                            {
                                widget.fields[c][k] = fIter;
                            }
                            return widget;
                        }

                        @Override
                        public Pair getWeightLM(int rank)
                        {
                            return new Pair(getWeight(), 
                                    fIter < ex.events.get(event).F ?
                                        vocabulary.getIndex(ex.events.get(event).
                                        getFields()[fIter].name.toLowerCase()) :
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

    // Default: don't generate any event (there should be only one of these nodes)
    // Note: we don't need any state, but include i and c so that we get distinct
    // nodes (see note in Hypergraph)
    @Override
    protected Object selectNoEvents(int i, int c)
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
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                        public double getWeight() {
                                return get(eventTypeParams.filters, Parameters.B_FALSE);
                        }
                        public void setPosterior(double prob) {}
                        public Pair getWeightLM(int rank)
                        {
                            return new Pair(getWeight(), null);
                        }
                        public GenWidget chooseLM(GenWidget widget, int word) {return widget;}
                        public GenWidget choose(GenWidget widget) {return widget;}
                    });
                } // for
            } // if
            return node;
        } // else
    }

    @Override
    protected NoneEventWordsNode genNoneEventWords(final int i, final int j, final int c)
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
                                       getEventTypeGivenWord(((Event3Model)model).none_t(), w);
                        }
                        public void setPosterior(double prob) { }
                        public GenWidget choose(GenWidget widget) {
                            widget.text[kIter] = w;
                            return widget;
                        }
                        });
                } // for
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
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public Pair getWeightLM(int rank)
                {
                    return getAtRank(params.trackParams[c].getNoneEventTypeEmissions(), rank);
                }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[i] = word;
                    return widget;
                }
            });
        }
        return node;
    }

    @Override
    protected void selectEnd(int j, EventsNode node, int i, int t0)
    {
        hypergraph.addEdge(node, genTrack(i, j, t0, 0, opts.allowNoneEvent, true));
//                           new Hypergraph.HyperedgeInfo<Widget>()
//        {
//            public double getWeight()
//            {
//                return 1;
//            }
//            public void setPosterior(double prob)
//            { }
//            public Widget choose(Widget widget)
//            {
//                return widget;
//            }
//        });
    }

    private Object testOrder()
    {
        final double pTempStart = 0.9;
        final double pWindStart = 0.8;
        final double pTempTemp = 0.02;
        final double pTempWind = 0.85;
        final double pWindWind = 0.01;
        final double pWindTemp = 0.7;
        EventsNode ev0Start = new EventsNode(0, ((Event3Model)model).none_t()); hypergraph.addSumNode(ev0Start);
        TrackNode tr01Start = new TrackNode(0, 1, ((Event3Model)model).none_t(), 0, false, false); hypergraph.addSumNode(tr01Start);
        TrackNode tr02Start = new TrackNode(0, 2, ((Event3Model)model).none_t(), 0, false, false); hypergraph.addSumNode(tr02Start);
        TrackNode tr03Start = new TrackNode(0, 3, ((Event3Model)model).none_t(), 0, false, false); hypergraph.addSumNode(tr03Start);
        
        final FieldsNode fs01Temp = new FieldsNode(0, 1, 0, 1, 0, 0); hypergraph.addSumNode(fs01Temp);
        EventsNode ev1Temp = new EventsNode(1, 1); hypergraph.addSumNode(ev1Temp);
        final FieldsNode fs01Wind = new FieldsNode(0, 1, 0, 2, 0, 0); hypergraph.addSumNode(fs01Wind);
        EventsNode ev1Wind = new EventsNode(1, 2); hypergraph.addSumNode(ev1Wind);
        
        final FieldsNode fs02Temp = new FieldsNode(0, 2, 0, 1, 0, 0); hypergraph.addSumNode(fs02Temp);
        final FieldsNode fs02Wind = new FieldsNode(0, 2, 0, 2, 0, 0); hypergraph.addSumNode(fs02Wind);
        
        final FieldsNode fs03Temp = new FieldsNode(0, 3, 0, 1, 0, 0); hypergraph.addSumNode(fs03Temp);
        final FieldsNode fs03Wind = new FieldsNode(0, 3, 0, 2, 0, 0); hypergraph.addSumNode(fs03Wind);
        
        TrackNode tr12Wind = new TrackNode(1, 2, 2, 0, false, false); hypergraph.addSumNode(tr12Wind);
        TrackNode tr12Temp = new TrackNode(1, 2, 1, 0, false, false); hypergraph.addSumNode(tr12Temp);
        
        TrackNode tr13Temp = new TrackNode(1, 3, 1, 0, false, false); hypergraph.addSumNode(tr13Temp);
        TrackNode tr13Wind = new TrackNode(1, 3, 2, 0, false, false); hypergraph.addSumNode(tr13Wind);
        
        final FieldsNode fs12Temp = new FieldsNode(1, 2, 0, 1, 0, 0); hypergraph.addSumNode(fs12Temp);
        EventsNode ev2Temp = new EventsNode(2, 1); hypergraph.addSumNode(ev2Temp);
        final FieldsNode fs12Wind = new FieldsNode(1, 2, 0, 2, 0, 0); hypergraph.addSumNode(fs12Wind);
        EventsNode ev2Wind = new EventsNode(2, 2); hypergraph.addSumNode(ev2Wind);
        
        final FieldsNode fs13Temp = new FieldsNode(1, 3, 0, 1, 0, 0); hypergraph.addSumNode(fs13Temp);
        final FieldsNode fs13Wind = new FieldsNode(1, 3, 0, 2, 0, 0); hypergraph.addSumNode(fs13Wind);
        
        TrackNode tr23Temp = new TrackNode(2, 3, 1, 0, false, false); hypergraph.addSumNode(tr23Temp);
        TrackNode tr23Wind = new TrackNode(2, 3, 2, 0, false, false); hypergraph.addSumNode(tr23Wind);

        final FieldsNode fs23Temp = new FieldsNode(2, 3, 0, 1, 0, 0); hypergraph.addSumNode(fs23Temp);
        final FieldsNode fs23Wind = new FieldsNode(2, 3, 0, 2, 0, 0); hypergraph.addSumNode(fs23Wind);
        
        hypergraph.addEdge(ev0Start, tr01Start, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev0Start, tr02Start, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev0Start, tr03Start, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr01Start, fs01Temp, ev1Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr01Start, fs01Wind, ev1Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr02Start, fs02Temp, ev2Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr02Start, fs02Wind, ev2Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr03Start, fs03Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr03Start, fs03Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindStart; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev1Temp, tr12Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev1Temp, tr13Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev1Wind, tr12Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev1Wind, tr13Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr12Wind, fs12Temp, ev2Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr12Wind, fs12Wind, ev2Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr12Temp, fs12Temp, ev2Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr12Temp, fs12Wind, ev2Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr13Temp, fs13Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr13Temp, fs13Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr13Wind, fs13Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr13Wind, fs13Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev2Temp, tr23Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(ev2Wind, tr23Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr23Temp, fs23Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr23Temp, fs23Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindTemp; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr23Wind, fs23Temp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pTempWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(tr23Wind, fs23Wind, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return pWindWind; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });

        hypergraph.addEdge(fs01Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("a")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("low")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("around")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs01Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs12Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("a")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("low")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("around")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs12Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs23Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("a")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("low")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("around")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs23Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs01Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("south")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("west")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("wind")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs01Wind.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs12Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("south")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("west")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("wind")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs12Wind.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs23Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.8, Event3Model.getWordIndex("south")); break;
                        case 1 : p = new Pair(0.6, Event3Model.getWordIndex("west")); break;
                        default: case 2 : p = new Pair(0.5, Event3Model.getWordIndex("wind")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs23Wind.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs02Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.038, Event3Model.getWordIndex("mostly")); break;
                        case 1 : p = new Pair(0.036, Event3Model.getWordIndex("cloudy")); break;
                        default: case 2 : p = new Pair(0.035, Event3Model.getWordIndex(",")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs02Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs02Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.038, Event3Model.getWordIndex("partly")); break;
                        case 1 : p = new Pair(0.036, Event3Model.getWordIndex("becoming")); break;
                        default: case 2 : p = new Pair(0.035, Event3Model.getWordIndex("sunny")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs02Wind.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs03Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.08, Event3Model.getWordIndex("patchy")); break;
                        case 1 : p = new Pair(0.06, Event3Model.getWordIndex("gusts")); break;
                        default: case 2 : p = new Pair(0.05, Event3Model.getWordIndex("before")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs03Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs03Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.08, Event3Model.getWordIndex("showers")); break;
                        case 1 : p = new Pair(0.06, Event3Model.getWordIndex("thunderstorms")); break;
                        default: case 2 : p = new Pair(0.05, Event3Model.getWordIndex("after")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs03Wind.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs13Temp, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.08, Event3Model.getWordIndex("calm")); break;
                        case 1 : p = new Pair(0.06, Event3Model.getWordIndex("clear")); break;
                        default: case 2 : p = new Pair(0.05, Event3Model.getWordIndex("near")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs13Temp.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(fs13Wind, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    Pair p = null;
                    switch(rank)
                    {
                        case 0 : p = new Pair(0.08, Event3Model.getWordIndex("calm")); break;
                        case 1 : p = new Pair(0.06, Event3Model.getWordIndex("clear")); break;
                        default: case 2 : p = new Pair(0.05, Event3Model.getWordIndex("near")); break;
                    }
                    return p;
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[fs13Wind.getI()] = word;
                    return widget;
                }
            });
        return ev0Start;
    }
    
    private Object test()
    {
        EventsNode sentence = new EventsNode(0, 0);
        TrackNode np = new TrackNode(0, 0, 0, 0, false, false);
        FieldNode vp = new FieldNode(0, 0, 0, 0, 0);
        FieldNode nnp = new FieldNode(0, 0, 0, 0, 1);
        final WordNode word1 = new WordNode(0, 1, 1, 1);
        final WordNode word2 = new WordNode(1, 1, 1, 2);
        final WordNode word3 = new WordNode(2, 1, 1, 2);
        final WordNode word4 = new WordNode(3, 1, 1, 2);
        final WordNode word5 = new WordNode(4, 1, 1, 2);
        hypergraph.addSumNode(sentence);
        hypergraph.addSumNode(np);
        hypergraph.addSumNode(vp);
        hypergraph.addSumNode(nnp);
        hypergraph.addSumNode(word1);
        hypergraph.addSumNode(word2);
        hypergraph.addSumNode(word3);
        hypergraph.addSumNode(word4);
        hypergraph.addSumNode(word5);
        hypergraph.addEdge(sentence, np, vp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(sentence, nnp, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(nnp, word5, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(np, word1, word2, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(vp, word3, word4, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public Widget choose(Widget widget) { return widget; }
            });
        hypergraph.addEdge(word1, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    if(rank == 0)
                    {
                        return new Pair(0.8, Event3Model.getWordIndex("mostly"));
                    }
                    else
                    {
                        return new Pair(0.5, Event3Model.getWordIndex("mainly"));
                    }
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[word1.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(word2, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    if(rank == 0)
                    {
                        return new Pair(0.9, Event3Model.getWordIndex("cloudy"));
                    }
                    else
                    {
                        return new Pair(0.4, Event3Model.getWordIndex("after"));
                    }
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[word2.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(word3, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    if(rank == 0)
                    {
                        return new Pair(0.2, Event3Model.getWordIndex("and"));
                    }
                    else
                    {
                        return new Pair(0.01, Event3Model.getWordIndex("midnight"));
                    }
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[word3.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(word4, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    if(rank == 0)
                    {
                        return new Pair(0.002, Event3Model.getWordIndex("."));
                    }
                    else
                    {
                        return new Pair(0.00018, Event3Model.getWordIndex("cold"));
                    }
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[word4.getI()] = word;
                    return widget;
                }
            });
        hypergraph.addEdge(word5, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                public double getWeight() { return 1.0; }
                public void setPosterior(double prob) { }
                public GenWidget choose(GenWidget widget) { return widget; }
                public Pair getWeightLM(int rank)
                {
                    if(rank == 0)
                    {
                        return new Pair(0.9, Event3Model.getWordIndex("high"));
                    }
                    else
                    {
                        return new Pair(0.5, Event3Model.getWordIndex("low"));
                    }
                }
                public GenWidget chooseLM(GenWidget widget, int word)
                {
                    widget.text[word4.getI()] = word;
                    return widget;
                }
            });

        return sentence;
    }
}