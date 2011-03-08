package induction.problem.event3;

import edu.uci.ics.jung.graph.Graph;
import fig.basic.Indexer;
import induction.Hypergraph;
import induction.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.Parameters;
import induction.problem.event3.params.Params;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author konstas
 */
public class SemParseInferState extends GenInferState
{
    Graph graph;

    public SemParseInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
    }

    public SemParseInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, Graph graph)
    {
        super(model, ex, params, counts, ispec, null);
        this.graph = graph;
    }

    @Override
    protected void initInferState(AModel model)
    {
        super.initInferState(model);
        words = ex.text;
        nums = new int[words.length];
        for(int w = 0; w < nums.length; w++)
        {
            nums[w] = Constants.str2num(Event3Model.wordToString(words[w]));
        }
        labels = ex.labels;
        // map all field values to an Indexer
        vocabulary = new Indexer<String>();
        for(Event e : ex.events)
        {
            for(Field f : e.getFields())
            {
                if (f instanceof NumField)
                    vocabulary.add("<num>");
                else
                {
                    for(int i = 0; i < f.getV(); i++)
                    {
                        vocabulary.add(Event3Model.processWord(f.valueToString(i)));
                    }
                }
            }
        }
        vocabulary.add("(none)");
    }

//    @Override
//    protected void createHypergraph(Hypergraph<Widget> hypergraph)
//    {
//        // setup hypergraph preliminaries
//        hypergraph.setupForSemParse(opts.debug, opts.modelType, true, opts.kBest,
//                opts.reorderType, opts.allowConsecutiveEvents,
//                /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
//                vocabulary.getIndex("<num>"),
//                vocabulary.getIndex("ELIDED_SYMBOL"),
//                opts.ngramWrapper != Options.NgramWrapper.roark,
//                ((Event3Model)model).getWordIndexer(), ex, graph);
//
//        if(opts.fullPredRandomBaseline)
//        {
//            this.hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).none_t()),
//                           new Hypergraph.HyperedgeInfo<Widget>()
//            {
//                public double getWeight()
//                {
//                    return 1;
//                }
//                public void setPosterior(double prob)
//                { }
//                public Widget choose(Widget widget)
//                {
//                    return widget;
//                }
//            });
//        } // if
//        else
//        {
//            this.hypergraph.addEdge(hypergraph.sumStartNode(), genEvents(0, ((Event3Model)model).none_t()),
//                           new Hypergraph.HyperedgeInfo<Widget>()
//            {
//                public double getWeight()
//                {
//                    return 1;
//                }
//                public void setPosterior(double prob)
//                { }
//                public Widget choose(Widget widget)
//                {
//                    return widget;
//                }
//            });
//        } // else
//    }

    @Override
    protected Widget newWidget()
    {
        int[] eventTypeIndices = new int[ex.events.length];
        for(int i = 0; i < eventTypeIndices.length; i++)
        {
           eventTypeIndices[i] = ex.events[i].getEventTypeIndex();
        }
        return new SemParseWidget(newMatrix(), newMatrix(), newMatrix(), newMatrix(),
                               newMatrixOne(),
                               ((Event3Model)model).eventTypeAllowedOnTrack, eventTypeIndices);
    }

    @Override
    protected CatFieldValueNode genCatFieldValueNode(final int i, int c, final int event, final int field)
    {
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        if(hypergraph.addSumNode(node))
        {
            final CatFieldParams fparams = getCatFieldParams(event, field);
            // Consider generating category v from words(i)
            final int w = words[i];

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
            public double getWeight() {
                return 1.0d;
            }
            public Pair getWeightLM(int rank)
            {
//                return getAtRank(fparams.valueEmissions[w], rank); // p(v | w)
                int length = fparams.valueEmissions[w].getCounts().length;
                Pair p = rank < length ? getAtRank(fparams.valueEmissions[w], rank) :
                    getAtRank(fparams.valueEmissions[w], length-1);
                p.label = vocabulary.getIndex(ex.events[event].getFields()[field].valueToString((Integer)p.label));
//                p.value *=
//                        (w == Event3Model.getWordIndex("<unk>")? 0.1 : 1.0);
                return p;
            }
            public void setPosterior(double prob) { }
            public GenWidget choose(GenWidget widget) { return widget; }
            public GenWidget chooseLM(GenWidget widget, int word)
            {
//                System.out.println("word " + word);
                widget.text[i] = ex.events[event].getFields()[field].parseValue(-1, vocabulary.getObject(word));                
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
        final int eventTypeIndex = ex.events[event].getEventTypeIndex();
        final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];
        final int w = words[i];


        if(hypergraph.addSumNode(node))
        {
            if(field == eventTypeParams.none_f)
            {
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
                    public double getWeight() {
                        return 1.0;
                    }
                    public void setPosterior(double prob) { }
                    public GenWidget choose(GenWidget widget) { return widget; }
                    public Pair getWeightLM(int rank)
                    {
                        Pair p = getAtRank(eventTypeParams.noneFieldEmissions, rank);
                        p.label = vocabulary.getIndex("(none)");
//                        p.label = null;
                        return p;
//                        return new Pair(get(eventTypeParams.noneFieldEmissions, w), null);
//                        return new Pair(get(eventTypeParams.noneFieldEmissions, w), vocabulary.getIndex("(none)"));
                    }
                    public GenWidget chooseLM(GenWidget widget, int word)
                    {
//                        System.out.println("null");
                        widget.text[i] = -1;
                        return widget;
                    }
                });
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

//                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfoLM<GenWidget>() {
//                    public double getWeight() {
//                        return 1.0;
//                    }
//                    public void setPosterior(double prob) { }
//                    public GenWidget choose(GenWidget widget) { return widget; }
//                    public Pair getWeightLM(int rank)
//                    {
//                        Pair p =  getAtRank(params.genericEmissions, rank);
//                        p.value *= get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
//                        p.label = vocabulary.getIndex("(none)");
////                        double value = get(params.genericEmissions, w)*
////                                       get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
//                        return p;
////                        return new Pair(value, vocabulary.getIndex("(none)"));
//                    }
//                    public GenWidget chooseLM(GenWidget widget, int word)
//                    {
////                        System.out.println("generic");
//                        widget.gens[c][i] = Parameters.G_FIELD_GENERIC;
//                        widget.text[i] = -1;
//                        return widget;
//                    }
//                });
            } // else
        }
        return node;
    }
}

//TO-DO: missing genNumFieldValueNode, genNoneWord