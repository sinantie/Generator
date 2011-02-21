package induction.problem.event3;

import induction.BigDouble;
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
public class ParserInferState extends GenInferState
{
    public ParserInferState(Event3Model model, Example ex, Params params,
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
                return getAtRank(fparams.emissions[rank], w); // p(v | w)
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
    
    // Generate word at position i with event e and field f
    @Override
    protected WordNode genWord(final int i, final int c, int event, final int field)
    {
        WordNode node = new WordNode(i, c, event, field);
        final int eventTypeIndex = ex.events[event].getEventTypeIndex();
        final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];

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
                         return getAtRank(eventTypeParams.noneFieldEmissions, rank);
                    }
                    public GenWidget chooseLM(GenWidget widget, int word)
                    {
                        widget.text[i] = word;
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
            } // else
        }
        return node;
    }
}

//TO-DO: missing genNumFieldValueNode, genNoneWord