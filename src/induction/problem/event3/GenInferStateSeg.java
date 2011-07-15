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
public class GenInferStateSeg extends GenInferState
{
    private int numberOfEvents;

    public GenInferStateSeg(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
    }

    @Override
    protected void initInferState(AModel model)
    {
        super.initInferState(model);
        numberOfEvents = ex.events.values().size();
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
//                 genEvents(seqNo+1, remember_t, allowNone): genEndNode();
                 genEvents(seqNo+1, remember_t): genEndNode();
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
                  genEvents(seqNo+1, remember_t): genEndNode();
//                  genEvents(seqNo+1, remember_t, allowNone): genEndNode();
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
        final TrackParams cparams = params.trackParams[0];

        if (seqNo == numberOfEvents)
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
            EventsNode node = new EventsNode(seqNo, t0);
            if(hypergraph.addSumNode(node))
            {
                final int start = seqNo * L;
                final int nextSeqNo = seqNo + 1;
                for(int k = start; k < (start + L) + 1; k++)
                {
                    final int end = k;
                    // (1) Choose the none event
                    if (opts.allowNoneEvent && (!trueInfer || ex.getTrueWidget() == null ||
                          ex.getTrueWidget().hasNoReachableContiguousEvents(start, end, 0)))
                    {
                        final int remember_t = opts.conditionNoneEvent ? cparams.none_t : t0; // Condition on none_t or not
                        hypergraph.addEdge(node,
                          genNoneEvent(start, end, 0), genEvents(nextSeqNo, remember_t),
                          new Hypergraph.HyperedgeInfo<Widget>() {
                              public double getWeight() {
                                  if(prevIndepEventTypes())
                                      return get(cparams.getEventTypeChoices()[cparams.boundary_t], cparams.none_t);
                                  else
                                      return get(cparams.getEventTypeChoices()[t0], cparams.none_t);
                              }
                              public void setPosterior(double prob) { }
                              public Widget choose(Widget widget) {
                                  for(int p = start; p < end; p++)
                                  {
                                      widget.events[0][p] = Parameters.none_e;
                                  }
                                  return widget;
                              }
                          });
                    } // if none_t
                    // (2) Choose an event type t and event e for track c
                    for(final Event e : ex.events.values())
                    {
                        final int eventId = e.id;
                        final int eventTypeIndex = e.getEventTypeIndex();
                        if (!trueInfer || ex.getTrueWidget() == null ||
                            ex.getTrueWidget().hasContiguousEvents(start, end, eventId))
                        {
                            final int remember_t = (indepEventTypes()) ? cparams.boundary_t : eventTypeIndex;
                            hypergraph.addEdge(node,
                              genEvent(start, end, 0, eventId), genEvents(nextSeqNo, remember_t),
                              new Hypergraph.HyperedgeInfo<Widget>() {
                                  public double getWeight()
                                  {
                                      if(prevIndepEventTypes())
                                          return get(cparams.getEventTypeChoices()[cparams.boundary_t],
                                                  eventTypeIndex) *
                                                  (1.0d/(double)ex.eventTypeCounts[eventTypeIndex]); // remember_t = t under indepEventTypes
                                      else
                                          return get(cparams.getEventTypeChoices()[t0], eventTypeIndex) *
                                                  (1.0/(double)ex.eventTypeCounts[eventTypeIndex]);
                                  }
                                  public void setPosterior(double prob) { }
                                  public Widget choose(Widget widget) {
                                      for(int p = start; p < end; p++)
                                      {
                                          widget.events[0][p] = eventId;
                                      }
                                      return widget;
                                  }
                              });
                          } // if
                    } // for
                    // (3) Choose to STOP
                    hypergraph.addEdge(node, genStopNode(nextSeqNo, t0, cparams, null));
                } // for k = start to k < (start + L) + 1
                hypergraph.assertNonEmpty(node);
            }
            return node;
        }
    }

    protected void selectEvent(int seqNo, EventsNode node, int t0)
    {

    }

    protected void selectEnd(int j, EventsNode node, int i, int t0)
    {
        hypergraph.addEdge(node, genTrack(i, t0, opts.allowNoneEvent));
    }
}
