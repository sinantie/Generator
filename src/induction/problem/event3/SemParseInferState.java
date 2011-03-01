package induction.problem.event3;

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

/**
 *
 * @author konstas
 */
public class SemParseInferState extends GenInferState
{
    public SemParseInferState(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
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
    }

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
                Pair p = getAtRank(fparams.valueEmissions[w], rank);
//                p.value *=
//                        (w == Event3Model.getWordIndex("<unk>")? 0.1 : 1.0);
                return p;
            }
            public void setPosterior(double prob) { }
            public GenWidget choose(GenWidget widget) { return widget; }
            public GenWidget chooseLM(GenWidget widget, int word)
            {
//                System.out.println("word " + word);
                widget.text[i] = word;
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
//                        Pair p = getAtRank(eventTypeParams.noneFieldEmissions, w);
//                        p.label = null;
//                        return p;
                        return new Pair(get(eventTypeParams.noneFieldEmissions, w), null);
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
////                        Pair p =  getAtRank(params.genericEmissions, w);
////                        p.value *= get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
////                        p.label = null;
////                        return p;
//                        double value = get(params.genericEmissions, w)*
//                                       get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC);
//                        return new Pair(value,  Event3Model.getWordIndex("<s>"));
//                    }
//                    public GenWidget chooseLM(GenWidget widget, int word)
//                    {
////                        System.out.println("generic");
//                        widget.gens[c][i] = Parameters.G_FIELD_GENERIC;
//                        widget.text[i] = 1;
//                        return widget;
//                    }
//                });
            } // else
        }
        return node;
    }
}

//TO-DO: missing genNumFieldValueNode, genNoneWord