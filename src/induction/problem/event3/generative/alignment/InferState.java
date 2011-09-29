package induction.problem.event3.generative.alignment;

import fig.basic.StopWatchSet;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.SymFieldParams;
import induction.problem.event3.params.StrFieldParams;
import induction.problem.event3.params.NumFieldParams;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.problem.event3.params.TrackParams;
import induction.Hypergraph;
import induction.Hypergraph.HyperpathResult;
import induction.Utils;
import induction.problem.AModel;
import induction.problem.AParams;
import induction.problem.InferSpec;
import induction.problem.event3.CatField;
import induction.problem.event3.Constants;
import induction.problem.event3.Event;
import induction.problem.event3.Event3InferState;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.NumField;
import induction.problem.event3.StrField;
import induction.problem.event3.SymField;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.CatFieldValueNode;
import induction.problem.event3.nodes.EventNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.nodes.FieldsNode;
import induction.problem.event3.nodes.NoneEventNode;
import induction.problem.event3.nodes.NoneEventWordsNode;
import induction.problem.event3.nodes.NumFieldValueNode;
import induction.problem.event3.nodes.PCEventsNode;
import induction.problem.event3.nodes.SelectNoEventsNode;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.StringFieldValueNode;
import induction.problem.event3.nodes.SymFieldValueNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.WordNode;
import induction.problem.event3.params.FieldParams;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class InferState extends Event3InferState
{    
    public InferState(Event3Model model, Example ex, Params params, Params counts,
            InferSpec ispec)
    {
        super(model, ex, params, counts, ispec);
    }

    @Override
    protected void initInferState(AModel model)
    {
        super.initInferState(model);
        words = ex.getText();
        nums = new int[words.length];
        for(int w = 0; w < nums.length; w++)
        {
            nums[w] = Constants.str2num(Event3Model.wordToString(words[w]));
        }
        labels = ex.getLabels();

        // Override bestWidget
        if (opts.fullPredRandomBaseline)
        {
            if (!ex.events.isEmpty())
            {
                Integer[] ids = ex.events.keySet().toArray(new Integer[0]);
                // Just match each line in the text to a single randomly chosen event
                for(int l = 0; l < ex.getStartIndices().length - 1; l++)
                {
                    final int e = opts.fullPredRandom.nextInt(ids.length);
                    for(int i = ex.getStartIndices()[l]; i < ex.getStartIndices()[l+1]; i++)
                    {
                        bestWidget.getEvents()[0][i] = ids[e]; // Assume one track
                    } // for
                } // for
            } // if
        } // if
    }
       
    @Override
    protected Widget newWidget()
    {
//        int[] eventTypeIndices = new int[ex.events.length];
//        for(int i = 0; i < eventTypeIndices.length && ex.events[i] != null; i++)
//        {
//           eventTypeIndices[i] = ex.events[i].getEventTypeIndex();
//        }
        HashMap<Integer, Integer> eventTypeIndices =
                            new HashMap<Integer, Integer>(ex.events.size());
        for(Event e : ex.events.values())
        {
            eventTypeIndices.put(e.getId(), e.getEventTypeIndex());
        }
        return new Widget(newMatrix(), newMatrix(), newMatrix(), newMatrix(),
                               ex.getStartIndices(), ((Event3Model)model).eventTypeAllowedOnTrack,
                               eventTypeIndices);
    }   

    @Override
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {
        hypergraph.debug = opts.debug;
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
                    public double getWeight()
                    {
                        return get(params.genericLabelChoices, label);
                    }
                    public void setPosterior(double prob)
                    {
                        if (genLabels())
                            update(counts.genericLabelChoices, label, prob);
                    }
                    public Widget choose(Widget widget)
                    {
                        return widget;
                    }
                });
            } // for
        } // if

//        hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).none_t()),
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
        StopWatchSet.begin("computePosteriors");
//        hypergraph.computePosteriors(ispec.isHardUpdate());
        hypergraph.computePosteriors(false);
        StopWatchSet.end();
        // Hard inference
        if (hardInfer)
        {
            HyperpathResult result = hypergraph.fetchBestHyperpath(newWidget());
//            HyperpathResult<Widget> result = hypergraph.fetchSampleHyperpath(opts.initRandom, newWidget());
            bestWidget = (Widget)result.widget;
            logVZ = result.logWeight;
        }
        else
        {
            bestWidget = newWidget();
            logVZ = Double.NaN;
        }
        updateStats();
    }
    
    protected Object genNumFieldValue(final int i, final int c, int event, int field)
    {
        return genNumFieldValue(i, c, event, field, getValue(event, field));
    }
    
    protected Object genNumFieldValue(final int i, final int c, int event, int field, int numValue)
    {
        if (nums[i] == Constants.NaN)
            return hypergraph.invalidNode; // Can't generate if not a number
        else
        {
            NumFieldValueNode node = new NumFieldValueNode(i, c, event, field);
            if (hypergraph.addSumNode(node))
            {
                // Consider generating nums(i) from v
                final int v = numValue;
                final NumFieldParams fparams = getNumFieldParams(event, field);
                final NumFieldParams fcounts = getNumFieldCounts(event, field);

                if (v == nums[i]) // M_IDENTITY
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_IDENTITY);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_IDENTITY, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_IDENTITY;
                            return widget;
                        }
                    });
                } // if
                if (roundUp(v) == nums[i]) // M_ROUNDUP
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_ROUNDUP);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_ROUNDUP, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_ROUNDUP;
                            return widget;
                        }
                    });
                } // if
                if (roundDown(v) == nums[i]) // M_ROUNDDOWN
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_ROUNDDOWN);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_ROUNDDOWN, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_ROUNDDOWN;
                            return widget;
                        }
                    });
                } // if
                if (roundClose(v) == nums[i]) // M_ROUNDCLOSE
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_ROUNDCLOSE);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_ROUNDCLOSE, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_ROUNDCLOSE;
                            return widget;
                        }
                    });
                } // if
                final int noise = nums[i] - v; // M_NOISEUP and M_NOISEDOWN
                if(noise > 0)
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_NOISEUP) * 0.5 *
                                       Math.pow(get(fparams.rightNoiseChoices,
                                       Parameters.S_CONTINUE), noise-1) *
                                       get(fparams.rightNoiseChoices, Parameters.S_STOP);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_NOISEUP, prob);
                            update(fcounts.rightNoiseChoices, Parameters.S_CONTINUE,
                                    (noise-1)*prob);
                            update(fcounts.rightNoiseChoices, Parameters.S_STOP, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_NOISEUP;
                            return widget;
                        }
                    });
                } // if
                else if(noise < 0)
                {
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(fparams.methodChoices, Parameters.M_NOISEDOWN) *
                                       Math.pow(get(fparams.leftNoiseChoices,
                                       Parameters.S_CONTINUE), -noise-1) *
                                       get(fparams.leftNoiseChoices, Parameters.S_STOP);
                        }
                        public void setPosterior(double prob) {
                            update(fcounts.methodChoices, Parameters.M_NOISEDOWN, prob);
                            update(fcounts.leftNoiseChoices, Parameters.S_CONTINUE,
                                    (-noise-1)*prob);
                            update(fcounts.leftNoiseChoices, Parameters.S_STOP, prob);
                        }
                        public Widget choose(Widget widget) {
                            widget.getNumMethods()[c][i] = Parameters.M_NOISEDOWN;
                            return widget;
                        }
                    });
                } // else if
            } // if (hypergraph.addSumNode(node))
            return node;
        } // else
    }
    
    protected CatFieldValueNode genCatFieldValueNode(int i, int c, int event, int field)
    {
        CatFieldValueNode node = new CatFieldValueNode(i, c, event, field);
        if(hypergraph.addSumNode(node))
        {
            // Consider generating words(i) from category v
            final int v = getValue(event, field);
            final int w = words[i];
            final CatFieldParams fparams = getCatFieldParams(event, field);
            final CatFieldParams fcounts = getCatFieldCounts(event, field);

            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
                    return get(fparams.emissions[v], w);// *
                         //  get(fparams.emissions[w], v);
                }
                public void setPosterior(double prob) {
                    update(fcounts.emissions[v], w, prob);
                    // uncomment for semantic parsing
                    update(fcounts.valueEmissions[w], v, prob); // values Emissions
                }
                public Widget choose(Widget widget) {
                    return widget;
                }
            });
        }
        return node;
    }
   
    protected Object genSymFieldValue(int i, int c, int event, int field)
    {
        final int v = getValue(event, field); // words[i] must match v exactly
        if (words[i] != v) return hypergraph.invalidNode;
        else if (genLabels() || prevGenLabels()) // Generate label
        {
            SymFieldValueNode node = new SymFieldValueNode(i, c, event, field);
            final SymFieldParams fparams = getSymFieldParams(event, field);
            final SymFieldParams fcounts = getSymFieldCounts(event, field);
            if (hypergraph.addSumNode(node))
            {
                final int label = labels[i];
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        // Remove default generation
                        return get(fparams.labelChoices, label) /
                               get(params.genericLabelChoices, label);
                    }
                    public void setPosterior(double prob) {
                        if (genLabels())
                        {
                          update(fcounts.labelChoices, label, prob);
                          updateKeepNonNegative(counts.genericLabelChoices, label, -prob);
                        }
                    }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
            return node;
        }
        else
            return hypergraph.endNode;
    }

    
    protected  Object genStrFieldValue(int i, int c, int event, int field)
    {
        final ArrayList<Integer> valueWords, valueLabels;
        Field tempField = ex.events.get(event).getFields()[field];
        if (tempField instanceof StrField) // nonsense!!
        {
            StrField.ArrayPair ap = ((StrField)tempField).indexer.
                    getObject(getValue(event, field));
            valueWords = ap.getWords();
            valueLabels = ap.getLabels();
        }
        else
        {
            throw Utils.impossible();
        }
        if (!valueWords.contains(words[i]))
        {
            return hypergraph.invalidNode;
        }
        else
        {
            StringFieldValueNode node = new StringFieldValueNode(i, c, event, field);
            final StrFieldParams fparams = getStrFieldParams(event, field);
            final StrFieldParams fcounts = getStrFieldCounts(event, field);
            if (hypergraph.addSumNode(node))
            {
                final int label = labels[i];
                // Note: previous versions of this code just generated the first
                // word instead of all of them.
                // That was mathematically wrong, but overfit slightly less
                for(int v_i = 0; i < Utils.same(valueWords.size(), valueLabels.size()); v_i++)
                {
                    if(valueWords.get(v_i) == words[i]) // Match
                    {
                        final int valueLabel = valueLabels.get(v_i);
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                            public double getWeight() {
                                return 1.0/valueWords.size() * // Pick uniformly at random
                                ( (genLabels() || prevGenLabels()) ?
                                  get(fparams.labelChoices[valueLabel], label) / // Remove default generation
                                  get(params.genericLabelChoices, label)
                                 : 1.0);
                            }
                            public void setPosterior(double prob) {
                               if (genLabels())
                               {
                                  update(fcounts.labelChoices[valueLabel], label, prob);
                                  updateKeepNonNegative(counts.genericLabelChoices, label, -prob);
                               }
                            }
                            public Widget choose(Widget widget) {
                                return widget;
                            }
                        });
                    }
                }
            }
            return node;
        }
    }

    protected Object genFieldValue(int i, int c, int event, int field)
    {
        Field tempField = ex.events.get(event).getFields()[field];
        if(tempField instanceof NumField) return genNumFieldValue(i, c, event, field);
        else if(tempField instanceof CatField) return genCatFieldValueNode(i, c, event, field);
        else if(tempField instanceof SymField) return genSymFieldValue(i, c, event, field);
        else if(tempField instanceof StrField) return genStrFieldValue(i, c, event, field);
        else return Utils.impossible();
    }
    
    // Generate word at position i with event e and field f
    protected WordNode genWord(final int i, final int c, int event, final int field)
    {
        WordNode node = new WordNode(i, c, event, field);
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];
        final int w = words[i];

        if(hypergraph.addSumNode(node))
        {
            if(field == eventTypeParams.none_f)
            {
                // Talk about the event type, not a particular field
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
                    return get(eventTypeParams.noneFieldEmissions, w) *
                           getEventTypeGivenWord(eventTypeIndex, w);
                }
                public void setPosterior(double prob) {
                    update(eventTypeCounts.noneFieldEmissions, w, prob);
                    updateEventTypeGivenWord(eventTypeIndex, w, prob);
                }
                public Widget choose(Widget widget) {
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
                    return get(eventTypeParams.genChoices[field], Parameters.G_FIELD_VALUE) *
                           getEventTypeGivenWord(eventTypeIndex, w);
                }
                public void setPosterior(double prob) {
                    update(eventTypeCounts.genChoices[field], Parameters.G_FIELD_VALUE, prob);
                    updateEventTypeGivenWord(eventTypeIndex, w, prob);
                }
                public Widget choose(Widget widget) {
                    widget.getGens()[c][i] = Parameters.G_FIELD_VALUE;
                    return widget;
                }
            });
                // G_FIELD_GENERIC: generate based on event type
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
                    return get(eventTypeParams.genChoices[field], Parameters.G_FIELD_GENERIC) *
                           get(params.genericEmissions, w) *
                           getEventTypeGivenWord(eventTypeIndex, w);
                }
                public void setPosterior(double prob) {
                    update(eventTypeCounts.genChoices[field], Parameters.G_FIELD_GENERIC, prob);
                    update(counts.genericEmissions,w , prob);
                    updateEventTypeGivenWord(eventTypeIndex, w, prob);
                }
                public Widget choose(Widget widget) {
                    widget.getGens()[c][i] = Parameters.G_FIELD_GENERIC;
                    return widget;
                }
            });
            } // else
        }
        return node;
    }
    
    // Generate field f of event e from begin to end
    protected Object genField(final int begin, final int end, int c, int event, final int field)
    {
        final int eventTypeIndex = ex.events.get(event).getEventTypeIndex();
        final int none_f = params.eventTypeParams[eventTypeIndex].none_f;

        final AParams aparams = field == none_f ? params.eventTypeParams[eventTypeIndex] :
                                                  getFieldParams(event, field);
        final AParams acounts = field == none_f ? counts.eventTypeParams[eventTypeIndex] :
                                                  getFieldCounts(event, field);                
        FieldNode node = new FieldNode(begin, end, c, event, field);
//        if(opts.binariseAtWordLevel) // integrate bigram probabilities between words
        if(!indepWords())
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
                        if (prevIndepWords())
                            return 1.0;
                        else
                        {
                            if(field == none_f)
                            {
                                return get(((EventTypeParams)aparams).noneFieldBigramChoices[
                                        begin > 0 ? words[begin] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[begin]);
                            }
                            else
                            {
                                return get(((FieldParams)aparams).wordBigramChoices[
                                        begin > 0 ? words[begin] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[begin]);
                            }

                        }
                    }
                    public void setPosterior(double prob) {
                        if(field == none_f)
                        {
                            update(((EventTypeParams)acounts).noneFieldBigramChoices[
                                        begin > 0 ? words[begin] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[begin], prob);
                        }
                        else
                        {
                            update(((FieldParams)acounts).wordBigramChoices[
                                        begin > 0 ? words[begin] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[begin], prob);
                        }
                    }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
        }
        else
        {
            if(hypergraph.addProdNode(node))
            {
                for(int i = begin; i < end; i++) // Generate each word in this range independently
                {
                    hypergraph.addEdge(node, genWord(i, c, event, field));
                }
            }
        }
        return node;
    }
 
    // Generate segmentation of i...end into fields; previous field is f0
    protected Object genFields(final int i, final int end, int c, final int event,
            final int f0, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        if(i == end)
        {
            // Make sure we've used all the fields we agreed to see
            if (eventTypeParams.efs_canBeEmpty(efs))
            {
                if(indepFields())
                    return hypergraph.endNode;
                else
                {
                    FieldsNode node = new FieldsNode(end, end, c, event, f0, efs);
                    if(hypergraph.addSumNode(node))
                    {   // Transition to boundary_f
                        hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                            public double getWeight() {
                                if (prevIndepFields())
                                    return 1.0;
                                else
                                    return get(eventTypeParams.fieldChoices[f0],
                                            eventTypeParams.boundary_f);
                            }
                            public void setPosterior(double prob) {
                                update(eventTypeCounts.fieldChoices[f0],
                                        eventTypeParams.boundary_f, prob);
                            }
                            public Widget choose(Widget widget) {
//                                System.out.println(String.format("FieldsNode i=%d, end=%d, e=%s, f0=%s", i,
//                                                    end, model.eventTypeToString(event),
//                                                    model.getEventTypes()[event].fieldToString(f0)));
                                return widget;
                            }
                        });
                    } // if
                    return node;
                } // else
            } // if
            else
            {
                return hypergraph.invalidNode;
            }
        } // if (i == end)
        else
        {
            FieldsNode node = new FieldsNode(i, end, c, event, f0, efs);            
            if(hypergraph.addSumNode(node))
            {
                if(oneFieldPerEvent())
                {
                    selectJ(end, i, end, c, event, f0, efs, eventTypeParams,
                            eventTypeCounts, node);
                }
                else if(newFieldPerWord())
                {
                    selectJ(i+1, i, end, c, event, f0, efs, eventTypeParams,
                            eventTypeCounts, node);
                }
                else
                {
                    for(int k = i+1; k < end+1; k++)
                    {
                        selectJ(k, i, end, c, event, f0, efs, eventTypeParams,
                                eventTypeCounts, node);
                    }
                }
            } // if
            return node;
        } // else
    }

    // Choose ending position j
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
//               j-i <= ((Event3Model)model).getEventTypes()[ex.events[event].getEventTypeIndex()].fields[f].maxLength)))
               j-i <= ex.events.get(event).getFields()[f].getMaxLength())))
            { // Limit field length
                int remember_f = indepFields() ? eventTypeParams.boundary_f : f;
                int new_efs = (f == eventTypeParams.none_f) ? efs :
                    eventTypeParams.efs_addAbsent(efs, f); // Now, allow f to be absent as we've already taken care of it
                hypergraph.addEdge(node, genField(i, j, c, event, f),
                                   genFields(j, end, c, event, remember_f, new_efs),
                                   new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        if (prevIndepFields()) // f0 == boundary_f under indepFields, so use that
                            return get(eventTypeParams.fieldChoices[eventTypeParams.boundary_f], fIter);
                        else
                            return get(eventTypeParams.fieldChoices[f0], fIter);
                    }
                    public void setPosterior(double prob) {
                        update(eventTypeCounts.fieldChoices[f0], fIter, prob);
                    }
                    public Widget choose(Widget widget) {
//                        System.out.println(String.format("event=%s, i=%d, j=%d, f0=%s, f=%s",
//                              ex.events[event].toString(), i, j,
//                              model.getEventTypes()[ex.events[event].getEventTypeIndex()].fieldToString(f0),
//                              model.getEventTypes()[ex.events[event].getEventTypeIndex()].fieldToString(fIter)));
                        for(int k = i; k < j; k++)
                        {
                            widget.getFields()[c][k] = fIter;                            
                        }
                        return widget;
                    }
                });
            } // if
        } // for
    }
   
    // Default: don't generate any event (there should be only one of these nodes)
    // Note: we don't need any state, but include i and c so that we get distinct
    // nodes (see note in Hypergraph)
    protected Object selectNoEvents(int i, int c)
    {
        if (ex.events.isEmpty())
            return hypergraph.endNode;
        else
        {
            SelectNoEventsNode node = new SelectNoEventsNode(i, c);
            if (hypergraph.addProdNode(node))
            {
//                for(int e = 0; e < ex.events.length && ex.events[e] != null; e++)
                for(final Event e: ex.events.values())
                {
//                    final int eventTypeIndex = ex.events[e].getEventTypeIndex();
                    final int eventTypeIndex = e.getEventTypeIndex();
                    final EventTypeParams eventTypeParams = params.eventTypeParams[eventTypeIndex];
                    final EventTypeParams eventTypeCounts = counts.eventTypeParams[eventTypeIndex];
                    hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                                return get(eventTypeParams.filters, Parameters.B_FALSE);
                        }
                        public void setPosterior(double prob) {
                             update(eventTypeCounts.filters, Parameters.B_FALSE, prob);
                        }
                        public Widget choose(Widget widget) {
                            return widget;
                        }
                    });
                } // for
            } // if
            return node;
        } // else
    }
    
    protected Object genNoneEventWords(final int i, int j, final int c)
    {
        NoneEventWordsNode node = new NoneEventWordsNode(i, j, c);
        final TrackParams cparams = params.trackParams[c];
        final TrackParams ccounts = counts.trackParams[c];

        if(!indepWords())
        {
            if (i == j)
            {
                return hypergraph.endNode;
            }
            if(hypergraph.addSumNode(node))
            {
                hypergraph.addEdge(node,
                                   genNoneEventWords(i + 1, j, c),
//                                   genNoneWord(i, c),
                                   new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                        return get(cparams.getNoneEventTypeBigramChoices()[
                                        i > 0 ? words[i] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[i]) *
                                get(params.trackParams[c].getNoneEventTypeEmissions(), words[i]) *
                                   getEventTypeGivenWord(params.trackParams[c].none_t, words[i]);
                    }
                    public void setPosterior(double prob)
                    {
                        update(ccounts.getNoneEventTypeBigramChoices()[
                                        i > 0 ? words[i] - 1 :
                                        Event3Model.getWordIndex("(boundary)")
                                        ], words[i], prob);
                        update(counts.trackParams[c].getNoneEventTypeEmissions(), words[i], prob);
                        updateEventTypeGivenWord(params.trackParams[c].none_t, words[i], prob);
                    }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            }
        }
        else if(hypergraph.addProdNode(node))
        {
            for(int k = i; k < j; k++) // Generate each word in this range independently
            {
                final int w = words[k];
                hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                            return get(params.trackParams[c].getNoneEventTypeEmissions(), w) *
                                   getEventTypeGivenWord(params.trackParams[c].none_t, w);
                    }
                    public void setPosterior(double prob) {
                         update(counts.trackParams[c].getNoneEventTypeEmissions(), w, prob);
                         updateEventTypeGivenWord(params.trackParams[c].none_t, w, prob);
                    }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            } // for
        } // if
        return node;
    }
    
    // Generate no event from i to j; incorporate salience if necessary
    protected Object genNoneEvent(int i, int j, int c)
    {
        if (opts.useEventSalienceModel)
        {
            NoneEventNode node = new NoneEventNode(i, j, c);
            if (hypergraph.addProdNode(node))
                hypergraph.addEdge(node, genNoneEventWords(i, j, c), selectNoEvents(i, c));
            return node;
        }
        else
            return genNoneEventWords(i, j, c);
    }
    
    // Generate the event, but make field sets respect efs
    protected Object genEFSEvent(int i, int j, int c, int event, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        if (opts.useEventSalienceModel)
        {
            EventNode node = new EventNode(i, j, c, event);
            if(hypergraph.addProdNode(node))
            {
                // We have to choose event e and not the others
                hypergraph.addEdge(node,
                        genFields(i, j, c, event, eventTypeParams.boundary_f, efs), selectNoEvents(i, c),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                    public double getWeight() {
                            return get(eventTypeParams.filters, Parameters.B_TRUE) /
                                   get(eventTypeParams.filters, Parameters.B_FALSE); // Compensate
                    }
                    public void setPosterior(double prob) {
                         update(eventTypeCounts.filters, Parameters.B_TRUE, prob);
                         update(eventTypeCounts.filters, Parameters.B_FALSE, -prob); // Compensate
                    }
                    public Widget choose(Widget widget) {
                        return widget;
                    }
                });
            } // if
            return node;
        } // if
        else
        {
            return genFields(i, j, c, event, eventTypeParams.boundary_f, efs);
        }
    }

   
    // Generate event e from i to j; incorporate salience if necessary
    protected Object genEvent(int i, int j, int c, int event)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        if (useFieldSets(ex.events.get(event).getEventTypeIndex()))
        {
            EventNode node = new EventNode(i, j, c, event);
            if(hypergraph.addSumNode(node))
            {
                // Choose which fields to use
                for(int fs = 0; fs < eventTypeParams.getAllowed_fs().length; fs++)
                {
                    final int fsIter = fs;
                    hypergraph.addEdge(node,
                        genEFSEvent(i, j, c, event, eventTypeParams.fs2efs(fs)),
                        new Hypergraph.HyperedgeInfo<Widget>() {
                            public double getWeight() {
                                    return get(eventTypeParams.fieldSetChoices, fsIter);
                            }
                            public void setPosterior(double prob) {
                                 update(eventTypeCounts.fieldSetChoices, fsIter, prob);
                            }
                            public Widget choose(Widget widget) {
                                return widget;
                            }
                        });
                } // for
            } // if
            return node;
        } // if
        else // Can use any field set
        {
            return genEFSEvent(i, j, c, event, eventTypeParams.getDontcare_efs());
        }
    }

    protected StopNode genStopNode(int i, final int t0, final TrackParams cparams, final TrackParams ccounts)
    {
        StopNode node = new StopNode(i, t0);
        if(hypergraph.addSumNode(node))
        {   // Transition to boundary_t
            hypergraph.addEdge(node, new Hypergraph.HyperedgeInfo<Widget>() {
                public double getWeight() {
//                            return 1.0;
                    if (prevIndepEventTypes())
                        return 1.0;
                    else
                        return get(cparams.getEventTypeChoices()[t0],
                                cparams.boundary_t);
                }
                public void setPosterior(double prob) {
                    update(ccounts.getEventTypeChoices()[t0],
                            cparams.boundary_t, prob);
                }
                public Widget choose(Widget widget) {
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
        
        final TrackParams cparams = params.trackParams[c];
        final TrackParams ccounts = counts.trackParams[c];

        if(i == j)
        {
            if(indepEventTypes())
                return hypergraph.endNode;
            else
            {                
                return genStopNode(i, t0, cparams, ccounts);
            } // else
        } // if (i == j)
        TrackNode node = new TrackNode(i, j, t0, c, allowNone, allowReal);
        // WARNING: allowNone/allowReal might not result in any valid nodes
        if(hypergraph.addSumNode(node))
        {
            // (1) Choose the none event
          if (allowNone && (!trueInfer || ex.getTrueWidget() == null ||
              ex.getTrueWidget().hasNoReachableContiguousEvents(i, j, c)))
          {
//              final int remember_t = t0; // Don't remember none_t (since [if] t == none_t, skip t)
              final int remember_t = opts.conditionNoneEvent ? cparams.none_t : t0; // Condition on none_t or not
              Object recurseNode = (c == 0) ? genEvents(j, remember_t) : hypergraph.endNode;
              if(opts.useEventTypeDistrib)
              {
                  hypergraph.addEdge(node,
                      genNoneEvent(i, j, c), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                              if(prevIndepEventTypes())
                                  return get(cparams.getEventTypeChoices()[cparams.boundary_t], cparams.none_t);
                              else
                                  return get(cparams.getEventTypeChoices()[t0], cparams.none_t);
                          }
                          public void setPosterior(double prob) {
                               update(ccounts.getEventTypeChoices()[t0], cparams.none_t, prob);
//                               if (ex.getTrueWidget() != null && i == 0) // HACK
//                               {
//                                   ex.getTrueWidget().setEventPosterior(
//                                           Parameters.none_e, ex.events.size(), prob);
//                               }
                          }
                          public Widget choose(Widget widget) {
                              for(int k = i; k < j; k++)
                              {
                                  widget.getEvents()[c][k] = Parameters.none_e;
                              }
                              return widget;
                          }
                      });
              } // if
              else
              {
                  hypergraph.addEdge(node,
                      genNoneEvent(i, j, c), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                              return 1.0;
                          }
                          public void setPosterior(double prob) { }
                          public Widget choose(Widget widget) {
                              for(int k = i; k < j; k++)
                              {
                                  widget.getEvents()[c][k] = Parameters.none_e;
                              }
                              return widget;
                          }
                      });
              } // else
          } // if
          // (2) Choose an event type t and event e for track c
//          for(int e = 0; e < ex.trackEvents[c].length && ex.events[e] != null; e++)
          for(final Event e : ex.events.values())
          {
              final int eventId = e.getId();
              final int eventTypeIndex = e.getEventTypeIndex();
//              final int eventTypeIndex = ex.events[eventId].getEventTypeIndex();
//              final int eventTypeIndex = ex.events.get(eventId).getEventTypeIndex();
              if (allowReal && 
                      (!trueInfer || ex.getTrueWidget() == null ||
                      ex.getTrueWidget().hasContiguousEvents(i, j, eventId)))
              {
//                  final int remember_t = (indepEventTypes()) ? ((Event3Model)model).none_t() : eventTypeIndex;
                  final int remember_t = (indepEventTypes()) ? cparams.boundary_t : eventTypeIndex;
                  final Object recurseNode = (c == 0) ? genEvents(j, remember_t) : hypergraph.endNode;
                  if (opts.useEventTypeDistrib)
                  {
                      hypergraph.addEdge(node,
                      genEvent(i, j, c, eventId), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight()
                          {
                              if(prevIndepEventTypes())
//                                  return get(cparams.getEventTypeChoices()[((Event3Model)model).none_t()],
                                  return get(cparams.getEventTypeChoices()[cparams.boundary_t],
                                          eventTypeIndex) *
                                          (1.0d/(double)ex.getEventTypeCounts()[eventTypeIndex]); // remember_t = t under indepEventTypes
                              else
                                  return get(cparams.getEventTypeChoices()[t0], eventTypeIndex) *
                                          (1.0/(double)ex.getEventTypeCounts()[eventTypeIndex]);
                          }
                          public void setPosterior(double prob) {
                               update(ccounts.getEventTypeChoices()[t0], eventTypeIndex, prob);
//                               if (ex.getTrueWidget() != null && i == 0) // HACK
//                               {
////                                   ex.getTrueWidget().setEventPosterior(eventId, ex.events.size(), prob);
//                                   ex.getTrueWidget().setEventPosterior(eventTypeIndex, ex.events.size(), prob);
//                               }
                          }
                          public Widget choose(Widget widget) {
                              for(int k = i; k < j; k++)
                              {
                                  widget.getEvents()[c][k] = eventId;
//                                  System.out.println(String.format("TrackNode i=%d, j=%d, t0=%s, e=%s",
//                                        i, j,
//                                        model.eventTypeToString(t0),
//                                        model.eventTypeToString(eIter)));
                              }
                              return widget;
                          }
                      });
                  } // if
                  else
                  {
                      hypergraph.addEdge(node,
                      genEvent(i, j, c, eventId), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                                  return 1.0;
                          }
                          public void setPosterior(double prob) { }
                          public Widget choose(Widget widget) {
                              for(int k = i; k < j; k++)
                              {
                                  widget.getEvents()[c][k] = eventId;
                              }
                              return widget;
                          }
                      });
                  } // else
              } // if
          } // for
        } // if        
        return node;
    }

    // pc says for each c whether the event type on track c can be none_t or not
    protected PCEventsNode genPCEvents(int i, int j, int t0, int pc)
    {
        PCEventsNode node = new PCEventsNode(i, j, t0, pc);
        if(hypergraph.addProdNode(node))
        {
            // For each track, do independently
            for(int c = 0; c < ((Event3Model)model).getC(); c++)
            {
                hypergraph.addEdge(node, genTrack(i, j, t0, c, opts.allowNoneEvent,
                        allowReal(c, pc)) );
            }
           // Note: there might be nothing consistent with pc
           // (one track has no events and pc says we need events)
           if (pc == wildcard_pc)
               hypergraph.assertNonEmpty(node);
        }
        return node;
    }    
   
    // Generate segmentation of i...N into event types; previous event type is t0
    // Incorporate eventType distributions
    protected Object genEvents(int i, int t0)
    {
        
        if (i == N)
        {
//            System.out.println(String.format("END : [%d]", i));
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
//            System.out.println(String.format("Father : [%d]", i));
            EventsNode node = new EventsNode(i, t0);
            if(hypergraph.addSumNode(node))
            {
                if (oneEventPerExample())
                    selectEnd(N, node, i, t0);
                else if (newEventTypeFieldPerWord())
                    selectEnd(i+1, node, i, t0);
                else if (opts.onlyBreakOnPunctuation &&
                         opts.dontCrossPunctuation) // Break at first punctuation
                {
                    selectEnd(Utils.find(i+1, N, ex.getIsPunctuationArray()), node, i, t0);
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
                    for(int k = i+1; k < Utils.find(i+1, N, ex.getIsPunctuationArray())+1; k++)
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
                hypergraph.assertNonEmpty(node);
            }
            return node;
        }
    }

    protected void selectEnd(int j, EventsNode node, int i, int t0)
    {       
        if (opts.jointEventTypeDecision)
        {
            for(int pc = 0; pc < ((Event3Model)model).getPC(); pc++) // Choose track bitmask pc
            {
                final int pcIter = pc;
                hypergraph.addEdge(node, genPCEvents(i, j, t0, pc),
                    new Hypergraph.HyperedgeInfo<Widget>() {
                        public double getWeight() {
                            return get(params.trackChoices, pcIter);
                        }
                        public void setPosterior(double prob) {
                             update(counts.trackChoices, pcIter, prob);
                        }
                        public Widget choose(Widget widget) {
                            return widget;
                        }
                    });
            } // for
        } // if
        else
        { // Do each track independently
//            System.out.println(String.format("[%d] => [%d,%d]", i, i, j));
            hypergraph.addEdge(node, genPCEvents(i, j, t0, wildcard_pc));
        }
    }
}