package induction.problem.event3.generative.generation;

import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.problem.event3.params.TrackParams;
import induction.Hypergraph;
import induction.ngrams.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.event3.Event;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.nodes.EventsNode;

/**
 *
 * @author konstas
 */
public class GenInferStateSeg extends GenInferState
{
    private int numberOfEvents;

    public GenInferStateSeg(GenerativeEvent3Model model, Example ex, Params params,
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
  
    protected Object genEvents(int seqNo, final int t0)
    {
        final TrackParams cparams = params.trackParams[0];

        if (seqNo == numberOfEvents)
        {
//            return hypergraph.endNode;
//            EventsNode node = new EventsNode(N, t0);
//            if(hypergraph.addSumNode(node))
//            {
//                selectEnd(N, node, N, t0);
//                hypergraph.assertNonEmpty(node);
//            }
//            return node;
            return genStopNode(seqNo, t0, cparams, null);
        }
        else
        {
            EventsNode node = new EventsNode(seqNo, t0);
            if(hypergraph.addSumNode(node))
            {
                final int start = seqNo * L;
                final int nextSeqNo = seqNo + 1;
                for(int k = start + 1; k < (start + L) + 1; k++)
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
                                      widget.getEvents()[0][p] = Parameters.none_e;
                                  }
                                  return widget;
                              }
                          });
                    } // if none_t
                    // (2) Choose an event type t and event e for track c
                    for(final Event e : ex.events.values())
                    {
                        final int eventId = e.getId();
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
                                                  (1.0d/(double)ex.getEventTypeCounts()[eventTypeIndex]); // remember_t = t under indepEventTypes
                                      else
                                          return get(cparams.getEventTypeChoices()[t0], eventTypeIndex) *
                                                  (1.0/(double)ex.getEventTypeCounts()[eventTypeIndex])*(1-segPenalty[end-start]);
                                  }
                                  public void setPosterior(double prob) { }
                                  public Widget choose(Widget widget) {
                                      for(int p = start; p < end; p++)
                                      {
                                          widget.getEvents()[0][p] = eventId;
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
}
