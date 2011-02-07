package induction.problem.event3;

import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import induction.problem.event3.params.TrackParams;
import induction.Hypergraph;
import induction.NgramModel;
import induction.problem.AModel;
import induction.problem.InferSpec;
import induction.problem.event3.nodes.EventNode;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.NoneEventNode;
import induction.problem.event3.nodes.PCEventsNode;
import induction.problem.event3.nodes.SelectNoEventsNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.WordNode;

/**
 *
 * @author konstas
 */
public class InferStateSegOld extends Event3InferState
{
    public InferStateSegOld(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
    }

    @Override
    protected void initInferState(AModel model)
    {
        super.initInferState(model);
//        N = words.length;
        words = ex.text;
        nums = new int[words.length];
        for(int w = 0; w < nums.length; w++)
        {
            nums[w] = Constants.str2num(Event3Model.wordToString(words[w])) ;
        }
        labels = ex.labels;

        // Override bestWidget
        if (opts.fullPredRandomBaseline)
        {
            if (ex.events.length > 0)
            {
                // Just match each line in the text to a single randomly chosen event
                for(int l = 0; l < ex.startIndices.length - 1; l++)
                {
                    final int e = opts.fullPredRandom.nextInt(ex.events.length);
                    for(int i = ex.startIndices[l]; i < ex.startIndices[l+1]; i++)
                    {
                        bestWidget.events[0][i] = e; // Assume one track
                    } // for
                } // for
            } // if
        } // if
    }

    @Override
    protected Widget newWidget()
    {
        int[] eventTypeIndices = new int[ex.events.length];
        for(int i = 0; i < eventTypeIndices.length && ex.events[i] != null; i++)
        {
           eventTypeIndices[i] = ex.events[i].getEventTypeIndex();
        }
        return new Widget(newMatrix(), newMatrix(), newMatrix(), newMatrix(),
                               ex.startIndices, ((Event3Model)model).eventTypeAllowedOnTrack,
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

        hypergraph.addEdge(hypergraph.prodStartNode(), genEvents(0, ((Event3Model)model).none_t()),
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

    /**
     * Default: don't generate any event (there should be only one of these nodes)
     * Note: we don't need any state, but include i and c so that we get distinct
     * nodes (see note in Hypergraph)
     * @param c the track to generate events for
     * @return
     */
    protected Object selectNoEvents(int c)
    {
        if (ex.events.length == 0)
            return hypergraph.endNode;
        else
        {
            SelectNoEventsNode node = new SelectNoEventsNode(0, c);
            if (hypergraph.addProdNode(node))
            {
                for(int e = 0; e < ex.events.length && ex.events[e] != null; e++)
                {
                    final int eventTypeIndex = ex.events[e].getEventTypeIndex();
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
    protected Object genNoneEvent(int c)
    {
        if (opts.useEventSalienceModel)
        {
            NoneEventNode node = new NoneEventNode(0, 0, c);
//            if (hypergraph.addProdNode(node))
//                hypergraph.addEdge(node, genNoneEventWords(c), selectNoEvents(c));
            return node;
        }
        else
        {
//            return genNoneEventWords(c);
            WordNode node = new WordNode(-1, c, ((Event3Model)model).none_t(), 0);
            hypergraph.addSumNode(node);
            return node;
        }
    }

    /**
     * Generate the event, but make field sets respect efs
     * @param c the track to generate events for
     * @param event
     * @param efs
     * @return
     */
    protected Object genEFSEvent(int c, int event, int efs)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[ex.events[event].getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[ex.events[event].getEventTypeIndex()];
        if (opts.useEventSalienceModel)
        {
            EventNode node = new EventNode(0, 0, c, event);
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
            WordNode node = new WordNode(-1, c, event, 0);
            hypergraph.addSumNode(node);
            return node;
        }
    }

    /**
     * Generate event e from i to j; incorporate salience if necessary
     * @param c the track to generate events for
     * @param event
     * @return
     */
    protected Object genEvent(int c, int event)
    {
        final EventTypeParams eventTypeParams = params.eventTypeParams[ex.events[event].getEventTypeIndex()];
        final EventTypeParams eventTypeCounts = counts.eventTypeParams[ex.events[event].getEventTypeIndex()];
        if (useFieldSets(ex.events[event].getEventTypeIndex()))
        {
            EventNode node = new EventNode(0, 0, c, event);
            if(hypergraph.addSumNode(node))
            {
                // Choose which fields to use
                for(int fs = 0; fs < eventTypeParams.allowed_fs.length; fs++)
                {
                    final int fsIter = fs;
                    hypergraph.addEdge(node,
                        genEFSEvent(c, event, eventTypeParams.fs2efs(fs)),
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
            return genEFSEvent(c, event, eventTypeParams.dontcare_efs);
        }
    }

    Object genEndNode()
    {
        return hypergraph.endNode;
    }

    /**
     * Generate events for track c
     * @param t0 previous eventType
     * @param c specified the track we will generates events for
     * @param allowNone specifies whether to allow modelling words with the none
     * event, i.e. not aligning with a particular event
     * @param allowReal specifies what event types we may use
     * @return
     */
    TrackNode genTrack(final int seqNo, final int t0, final int c,
                       boolean allowNone, boolean allowReal)
    {
        TrackNode node = new TrackNode(seqNo, 0, t0, c, allowNone, allowReal);
        final TrackParams cparams = params.trackParams[c];
        final TrackParams ccounts = counts.trackParams[c];
        // WARNING: allowNone/allowReal might not result in any valid nodes
        if(hypergraph.addSumNode(node))
        {
          // (1) Choose the none event
//          if (allowNone && (!trueInfer || ex.getTrueWidget() == null ||
//              ex.getTrueWidget().hasNoReachableContiguousEvents(i, j, c)))
          if (allowNone) // in the current context, trueInfer = true, which reduces to one parameter only
          {
              final int remember_t = t0; // Don't remember none_t (since t == none_t, skip t)
              // if we generate all in one track (c == 0), then check whether we are in
              // the end of our sequence and generate the final end node (we don't want
              // to get stuck in infinite recursion. If we generate events in more than
              // one tracks then events are independent of each other (see Percy paper)
              // so just emit an endNode as the recurseNode
//             final Object recurseNode = (c == 0) ? (seqNo < ex.trackEvents[c].length ?
             final Object recurseNode = (c == 0) ? (seqNo < 2 ?
                      genEvents(seqNo+1, remember_t): genEndNode()) : hypergraph.endNode;
              if(opts.useEventTypeDistrib)
              {
                  hypergraph.addEdge(node,
                      genNoneEvent(c), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                                  return get(cparams.getEventTypeChoices()[t0], ((Event3Model)model).none_t());
                          }
                          public void setPosterior(double prob) {
                               update(ccounts.getEventTypeChoices()[t0], ((Event3Model)model).none_t(), prob);
//                               if (ex.getTrueWidget() != null && i == 0) // HACK
//                               {
//                                   ex.getTrueWidget().setEventPosterior(
//                                           Parameters.none_e, ex.events.length, prob);
//                               }
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
                  hypergraph.addEdge(node,
                      genNoneEvent(c), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                              return 1.0;
                          }
                          public void setPosterior(double prob) { }
                          public Widget choose(Widget widget) {
//                              for(int k = i; k < j; k++)
//                              {
//                                  widget.events[c][k] = Parameters.none_e;
//                              }
                              return widget;
                          }
                      });
              } // else
          } // if (none event)
          // (2) Choose an event type t and event e for track c
          int l=2;
//          for(int e = 0; e < ex.trackEvents[c].length && ex.events[e] != null; e++)
          for(int e = 0; e < l && ex.events[e] != null; e++)
          {
              final int eventId = e;
              final int eventTypeIndex = ex.events[eventId].getEventTypeIndex();
//              if (allowReal &&
////                      (!opts.disallowConsecutiveRepeatFields || eventTypeIndex != t0) && // Can't repeat events
//                      (!trueInfer || ex.getTrueWidget() == null ||
//                      ex.getTrueWidget().hasContiguousEvents(i, j, eventId)))
              if (allowReal) // in the current context, trueInfer = true, which reduces to one parameter only
              {
                  final int remember_t = (indepEventTypes()) ? ((Event3Model)model).none_t() : eventTypeIndex;
                  // if we generate all in one track (c == 0), then check whether we are in
                  // the end of our sequence and generate the final end node (we don't want
                  // to get stuck in infinite recursion. If we generate events in more than
                  // one tracks then events are independent of each other (see Percy paper)
                  // so just emit an endNode as the recurseNode
//                  final Object recurseNode = (c == 0) ? (seqNo < ex.trackEvents[c].length ?
                  final Object recurseNode = (c == 0) ? (seqNo < l ?
                      genEvents(seqNo+1, remember_t): genEndNode()) : hypergraph.endNode;
                  if (opts.useEventTypeDistrib)
                  {
                      hypergraph.addEdge(node,
                      genEvent(c, eventId), recurseNode,
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
//                               if (ex.getTrueWidget() != null && i == 0) // HACK
//                               {
//                                   ex.getTrueWidget().setEventPosterior(eventId, ex.events.length, prob);
//                               }
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
                      hypergraph.addEdge(node,
                      genEvent(c, eventId), recurseNode,
                      new Hypergraph.HyperedgeInfo<Widget>() {
                          public double getWeight() {
                                  return 1.0;
                          }
                          public void setPosterior(double prob) { }
                          public Widget choose(Widget widget) {
//                              for(int k = i; k < j; k++)
//                              {
//                                  widget.events[c][k] = eventId;
//                              }
                              return widget;
                          }
                      });
                  } // else
              } // if
          } // for
        }
        return node;
    }

    /**
     *
     * @param seqNo the sequence number of the event to be generated
     * @param t0 previous eventType
     * @param pc says for each c whether the event type on track c can be none_t or not
     * @return
     */
    //
    protected PCEventsNode genPCEvents(int seqNo, int t0, int pc)
    {
        PCEventsNode node = new PCEventsNode(seqNo, 0, t0, pc);
        if(hypergraph.addProdNode(node))
        {
            // For each track, do independently
            for(int c = 0; c < ((Event3Model)model).C; c++)
            {
                hypergraph.addEdge(node, genTrack(seqNo, t0, c, opts.allowNoneEvent,
                        allowReal(c, pc)) );
            }
           // Note: there might be nothing consistent with pc
           // (one track has no events and pc says we need events)
           if (pc == wildcard_pc)
               hypergraph.assertNonEmpty(node);
        }
        return node;
    }

    /**
     * Generate sequence of events ignoring segmentation
     * @param seqNo the sequence number of the event to be generated
     * @param t0 previous eventType
     * @return
     */
    protected Object genEvents(int seqNo, int t0)
    {
        /* The maximum sequence number is the total number of event types. */
//        if(seqNo >= ((Event3Model)model).T())
//            return hypergraph.endNode;
        EventsNode node = new EventsNode(seqNo, t0);
        if(hypergraph.addSumNode(node))
        {
            selectEnd(node, seqNo, t0);
            hypergraph.assertNonEmpty(node);
        }
        return node;
    }

    /**
     * Choose whether we will split events to tracks
     * @param node
     * @param seqNo the sequence number of the event to be generated
     * @param t0
     */
    protected void selectEnd(EventsNode node, int seqNo, int t0)
    {
        if (opts.jointEventTypeDecision)
        {
            for(int pc = 0; pc < ((Event3Model)model).PC; pc++) // Choose track bitmask pc
            {
                final int pcIter = pc;
                hypergraph.addEdge(node, genPCEvents(seqNo, t0, pc),
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
//            hypergraph.addEdge(node, genPCEvents(seqNo, t0, wildcard_pc));
            hypergraph.addEdge(node, genTrack(seqNo, t0, 0, opts.allowNoneEvent, true));
        }
    }
}
