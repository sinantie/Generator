package induction.problem.event3;

import fig.basic.Indexer;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.problem.event3.params.TrackParams;
import induction.Hypergraph;
import induction.NgramModel;
import induction.Options;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.Pair;
import induction.problem.event3.nodes.EventNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.NoneEventNode;
import induction.problem.event3.nodes.SelectNoEventsNode;
import induction.problem.event3.nodes.WordNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class GenInferStateSeg extends Event3InferState
{
    protected NgramModel ngramModel;
    protected Indexer<String> vocabulary;
    private int numberOfEvents;

    public GenInferStateSeg(Event3Model model, Example ex, Params params,
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
        this.vocabulary = ((Event3Model)model).getWordIndexer();
        numberOfEvents = ex.events.values().size();
    }

    protected int[] newMatrixOne()
    {
        int[] out = new int[ex.N()]; // CAREFUL WITH N
        for(int i = 0; i < out.length; i++)
        {
            Arrays.fill(out, -1);
        }
        return out;
    }

    @Override
    protected Widget newWidget()
    {
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

    @Override
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
        if(opts.fullPredRandomBaseline)
        {
            this.hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0,
                    ((Event3Model)model).boundary_t(), opts.allowNoneEvent),
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
            list.add(genEvents(0, ((Event3Model)model).boundary_t(), opts.allowNoneEvent));
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

    /**
     * Default: don't generate any event (there should be only one of these nodes)
     * Note: we don't need any state, but include i and c so that we get distinct
     * nodes (see note in Hypergraph)
     * @param c the track to generate events for
     * @return
     */
    protected Object selectNoEvents(int c)
    {
        if (ex.events.isEmpty())
            return hypergraph.endNode;
        else
        {
            SelectNoEventsNode node = new SelectNoEventsNode(0, c);
            if (hypergraph.addProdNode(node))
            {
//                for(int e = 0; e < ex.events.length && ex.events[e] != null; e++)
                for(final Event e : ex.events.values())
                {
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

    /**
     * Generate no event from i to j; incorporate salience if necessary
     * @param c the track to generate events for
     * @return
     */
    protected Object genNoneEvent(int seqNo)
    {
        if (opts.useEventSalienceModel)
        {
            NoneEventNode node = new NoneEventNode(seqNo, 0, 0);
//            if (hypergraph.addProdNode(node))
//                hypergraph.addEdge(node, genNoneEventWords(c), selectNoEvents(c));
            return node;
        }
        else
        {
//            return genNoneEventWords(c);
            WordNode node = new WordNode(-1, 0, ((Event3Model)model).none_t(), 0);
            hypergraph.addSumNode(node);
            return node;
        }
    }

    /**
     * Generate the event, but make field sets respect efs
     * @param the sequence number of the event
     * @param event
     * @param efs
     * @return
     */
    protected Object genEFSEvent(int seqNo, int event, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        if (opts.useEventSalienceModel)
        {
            EventNode node = new EventNode(0, 0, seqNo, event);
//            if(hypergraph.addProdNode(node))
//            {
//                // We have to choose event e and not the others
//                hypergraph.addEdge(node,
//                        genFields(c, event, eventTypeParams.boundary_f, efs), selectNoEvents(c),
//                        new Hypergraph.HyperedgeInfo<Widget>() {
//                    public double getWeight() {
//                            return get(eventTypeParams.filters, Parameters.B_TRUE) /
//                                   get(eventTypeParams.filters, Parameters.B_FALSE); // Compensate
//                    }
//                    public void setPosterior(double prob) {
//                         update(eventTypeCounts.filters, Parameters.B_TRUE, prob);
//                         update(eventTypeCounts.filters, Parameters.B_FALSE, -prob); // Compensate
//                    }
//                    public Widget choose(Widget widget) {
//                        return widget;
//                    }
//                });
//            } // if
            return node;
        } // if
        else
        {
//            return genFields(c, event, eventTypeParams.boundary_f, efs);
            WordNode node = new WordNode(-1, 0, event, 0);
            hypergraph.addSumNode(node);
            return node;
        }
    }

    /**
     * Generate event e at the position seqNo of the sequence; incorporate salience if necessary
     * @param seqNo the sequence number of the event
     * @param event
     * @return
     */
    protected Object genEvent(int seqNo, int event)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[
                ex.events.get(event).getEventTypeIndex()];
        if (useFieldSets(ex.events.get(event).getEventTypeIndex()))
        {
            EventNode node = new EventNode(seqNo, 0, 0, event);
            if(hypergraph.addSumNode(node))
            {
                // Choose which fields to use
                for(int fs = 0; fs < eventTypeParams.getAllowed_fs().length; fs++)
                {
                    final int fsIter = fs;
                    hypergraph.addEdge(node,
                        genEFSEvent(seqNo, event, eventTypeParams.fs2efs(fs)),
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
            return genEFSEvent(seqNo, event, eventTypeParams.getDontcare_efs());
        }
    }

    Object genEndNode()
    {
        return hypergraph.endNode;
    }   

    /**
     * Generate sequence of events ignoring segmentation. Generate STOP event as well.
     * @param seqNo the sequence number of the event to be generated
     * @param t0 previous eventType
     * @return
     */
    protected Object genTrack(int seqNo, final int t0, boolean allowNone)
    {
        EventsNode node = new EventsNode(seqNo, t0);
        final TrackParams cparams = params.trackParams[0];
        final TrackParams ccounts = counts.trackParams[0];
        if(hypergraph.addSumNode(node))
        {            
            if (allowNone) // (1) Choose the none event
            {
              final int remember_t = opts.conditionNoneEvent ? cparams.none_t : t0; // Condition on none_t or not
              // Check whether we are in the end of our sequence and generate
              // the final end node (we don't want to get stuck in infinite recursion).
              final Object recurseNode = seqNo < ex.events.values().size() ?
                 genEvents(seqNo+1, remember_t, allowNone): genEndNode();
              if(opts.useEventTypeDistrib)
              {
                  hypergraph.addEdge(node,
                      genNoneEvent(seqNo), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                                  return get(cparams.getEventTypeChoices()[t0], cparams.none_t);
                          }
                          public void setPosterior(double prob) {
                               update(ccounts.getEventTypeChoices()[t0], cparams.none_t, prob);
                          }
                          public Widget choose(Widget widget) {
//                              for(int k = i; k < j; k++)
//                              {
//                                  widget.events[c][k] = Parameters.none_e;
//                              }
                              return widget;
                          }
                      });
              } // if
              else
              {
                  hypergraph.addEdge(node, genNoneEvent(seqNo), recurseNode);
              } // else
            } // if (none event)
            // (2) Choose an event type t and event e for track c
            for(final Event e: ex.events.values())
            {
              final int eventId = e.id;
              final int eventTypeIndex = e.getEventTypeIndex();
              final int remember_t = (indepEventTypes()) ? cparams.boundary_t : eventTypeIndex;
              // Check whether we are in the end of our sequence and generate
              // the final end node (we don't want to get stuck in infinite recursion).
              final Object recurseNode = seqNo < ex.events.values().size() ?
                  genEvents(seqNo+1, remember_t, allowNone): genEndNode();
              if (opts.useEventTypeDistrib)
              {
                  hypergraph.addEdge(node,
                  genEvent(seqNo, eventId), recurseNode,
                  new Hypergraph.HyperedgeInfo<Widget>() {
                      public double getWeight()
                      {
                          if(prevIndepEventTypes())
                              return get(cparams.getEventTypeChoices()[((Event3Model)model).none_t()],
                                      eventTypeIndex) *
                                      (1.0d/(double)ex.eventTypeCounts[eventTypeIndex]); // remember_t = t under indepEventTypes
                          else
                              return get(cparams.getEventTypeChoices()[t0], eventTypeIndex) *
                                      (1.0/(double)ex.eventTypeCounts[eventTypeIndex]);
                      }
                      public void setPosterior(double prob) {
                           update(ccounts.getEventTypeChoices()[t0], eventTypeIndex, prob);
                      }
                      public Widget choose(Widget widget) {
            //                              for(int k = i; k < j; k++)
            //                              {
            //                                  widget.events[c][k] = eventId;
            ////                                  System.out.println(String.format("TrackNode i=%d, j=%d, t0=%s, e=%s",
            ////                                        i, j,
            ////                                        model.eventTypeToString(t0),
            ////                                        model.eventTypeToString(eIter)));
            //                              }
                          return widget;
                      }
                  });
              } // if
              else
              {
                  hypergraph.addEdge(node, genEvent(seqNo, eventId), recurseNode);
              } // else
            } // for
            hypergraph.assertNonEmpty(node);
        }
        return node;
    }

    protected Object genEvents(int seqNo, final int t0)
    {

        if (seqNo == numberOfEvents)
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
            EventsNode node = new EventsNode(seqNo, t0);
            if(hypergraph.addSumNode(node))
            {
                if (oneEventPerExample())
                    selectEnd(N, node, i, t0);
                else if (newEventTypeFieldPerWord())
                    selectEnd(i+1, node, i, t0);
                else // Allow everything
                {
                    for(int k = i+1; k < end(i, Integer.MAX_VALUE)+1; k++)
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
        hypergraph.addEdge(node, genTrack(i, j, t0, 0, opts.allowNoneEvent, true));
    }
}
