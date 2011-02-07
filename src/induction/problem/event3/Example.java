package induction.problem.event3;

import induction.problem.event3.params.Parameters;
import induction.Utils;
import induction.problem.wordproblem.WordExample;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 * @author konstas
 */
public class Example extends WordExample<Widget>
{
    private Event3Model model;
    protected String name;
    public Event[] events;
    protected int[] text, labels, startIndices;
    private Widget trueWidget;
    protected int[] eventTypeCounts = null;
    protected int[][] trackEvents = null;
    private final int C, N;
    protected boolean[] isPunctuationArray;

    public Example(Event3Model model, String name, Event[] events, int[] text,
                   int[] labels, int[] startIndices, int N, Widget trueWidget)
    {
        this.model = model;
        this.name = name;
        this.events = events;
        this.text = text;
        this.labels = labels;
        this.startIndices = startIndices;
        this.trueWidget = trueWidget;
        this.C = model.C;
        this.N = N;
                
        String s;
        if(text != null)
        {
            isPunctuationArray = new boolean[N];
            for(int i = 0; i < isPunctuationArray.length; i++)
            {
                s = Event3Model.wordToString(text[i]);
                isPunctuationArray[i] = model.getOpts().posAtSurfaceLevel ?
                        // if words have pos tag attached to them
                        s.equals("./.") || s.equals(",/,") || s.equals("--/:") ||
                        s.equals("-LRB-/-LRB-") || s.equals("-RRB-/-RRB-") ||
                        (model.getOpts().andIsPunctuation && s.equals("and/CC")) :

                        s.equals(".") || s.equals(",") || s.equals("--") ||
                        s.equals("(") || s.equals(")") ||
                        (model.getOpts().andIsPunctuation && s.equals("and"));
            }
        } 
    }

    @Override
    public int N()
    {
        return N;
    }

    // For each original line in the input, output the widgets which were aligned
    String widgetToEvalFullString(Widget widget)
    {
        String out[] = new String[startIndices.length - 1];
        TreeSet<Integer> alignedEvents = new TreeSet();
        for(int l = 0; l < out.length; l++)
        {
            alignedEvents.clear();
            for(int i = startIndices[l]; i < startIndices[l + 1]; i++)
            {
                for(Integer e: widget.foreachEvent(i))
                {
                    if (!e.equals(Parameters.none_e))
                        alignedEvents.add(e);
                }
            } // for
            if (alignedEvents.size() > 0)
            {
                out[l] = name + "\t" + l + " " + Utils.mkString(
                        Utils.uniq(alignedEvents.toArray()), " ");
            }
        } // for
        return Utils.mkString(out, " ");
    }

    private String genPrediction(GenWidget widget)
    {
        String out = "";
        for(int i = 0; i < widget.text.length; i++)
        {
            out += (widget.nums[i] > -1 ? widget.nums[i] : Event3Model.wordToString(widget.text[i])) + " ";
        }
        return out.trim();
    }
    String genWidgetToNiceFullString(GenWidget widget)
    {
        String out = name + "\n" + genPrediction(widget) +
                     "\n\n" + genWidgetToSemantics(widget) + "\n";
        if(trueWidget != null)
            out += trueWidget.performance + "\n";
        return out;
//        return out + "\n\n" + widgetToNiceFullString(widget);
    }

    String genWidgetToSGMLOutput(GenWidget widget)
    {
        String out = "<doc docid=\"" + name  + "\" genre=\"nw\">\n" +
                     "<p>\n<seg id=\"1\" " +
                     "bleu=\"" + widget.scores[Parameters.BLEU_METRIC] + "\"" +
                     " bleu_modified=\"" + widget.scores[Parameters.BLEU_METRIC_MODIFIED] + "\"" +
                     " meteor=\"" + widget.scores[Parameters.METEOR_METRIC] + "\"" +
                     " ter=\"" + widget.scores[Parameters.TER_METRIC] + "\"" +
                     ">" +
                     genPrediction(widget) +
                     "</seg>\n</p>\n</doc>";

        return out;
    }
    String genWidgetToSemantics(GenWidget widget)
    {
        int n = widget.events[0].length;
        StringBuilder buf = new StringBuilder();
        for(int c = 0; c < widget.events.length; c++)
        {
            int i = 0;
            while (i < n) // Segment into entries
            {
                int e = widget.events[c][i];

                int j = i + 1;
                while (j < n && widget.events[c][j] == e)
                {
                    j += 1;
                }
                if (e != Parameters.none_e)
                {
                    buf.append((e == Parameters.unreachable_e) ? "(unreachable)" : model.eventTypeToString(events[e].getEventTypeIndex())).append("(").append(events[e].id).append(  ")[");
                }
                if (widget.fields == null)
                {
                    for(int k = i; k < j; k++)
                    {
                        buf.append(Event3Model.wordToString(widget.text[k])).append(" ");
                    }
                    buf.deleteCharAt(buf.length() - 1);
                } // if
                else
                {
                    int k = i;
                    while (k < j) // Segment i...j into fields
                    {
                        int f = widget.fields[c][k];
                        int l = k+1;
                        while (l < j && widget.fields[c][l] == f)
                        {
                            l += 1;
                        }
                        if (k != i)
                        {
                            buf.append(" ");
                        }
                        if (f != -1)
                        {
                            buf.append(events[e].fieldToString(f)).append("[");
                        }
                        for(int m = k; m < l; m++)
                        {
                            String str = (widget.nums[m] > -1 ? widget.nums[m] : Event3Model.wordToString(widget.text[m])) + "";
                            if (widget.gens != null && widget.gens[c][m] != -1)
                            {
                                str += "_" + Parameters.short_gstr[widget.gens[c][m]];
                            }
                            if (widget.numMethods != null && widget.numMethods[c][m] != -1)
                            {
                                str += Parameters.short_mstr[widget.numMethods[c][m]];
                            }
                            buf.append(str).append(" ");
                        }
                        buf.deleteCharAt(buf.length() - 1);
                        if (f != -1)
                        {
                            buf.append("] ");
                        }
                        k = l;
                    }
                } // else
                if (e != Parameters.none_e)
                {
                    buf.append("] ");
                }
                i = j;
            } // while
        } // for
        return buf.toString();
    }

    String widgetToNiceFullString(Widget widget)
    {
        // Returns a string on one line; use tabs later to separate
        int n = Utils.same(N(), widget.events[0].length);
        StringBuffer buf = new StringBuffer();
        buf.append(name).append(":");

        // This is rough (do it for entire example)

        // track -> set of events that go on that track
        HashSet<Integer>[] trueEvents = new HashSet[C];
        for(int i = 0; i < C; i++)
        {
            trueEvents[i] = new HashSet();
        }
        if (trueWidget != null)
        {
            for(int i = 0; i < n; i++)
            {
                for(Integer e: trueWidget.foreachEvent(i))
                {
                    if(Parameters.isRealEvent(e))
                    {
                        for(int c = 0; c < C; c++)
                        {                            
                            if (model.eventTypeAllowedOnTrack[c].contains(
                                    events[e].getEventTypeIndex()))
                            {
                                trueEvents[c].add(e);
                            }
                        } // for
                    } // if
                } // for
            } // for
        } // if

        buf.append("\t- Pred:");
        renderWidget(widget, false, n, trueEvents, buf); // Prediction
        if (trueWidget != null) // Truth
        {
            buf.append("\t- True:");
            renderWidget(trueWidget, false, n, trueEvents, buf);
            buf.append("\t").append(trueWidget.performance).append(" (").append(events.length).append(" possible events)");
            /*if (trueWidget.eventPosterior != null)
                buf.append("\t" + trueWidget.eventPosteriorStr(events));*/
        }
        return buf.toString();
    }

    // If we propose event e on track c, is that correct?
    private boolean isOkay(int c, int e, HashSet<Integer>[] trueEvents)
    {
        return trueWidget == null || ((e == Parameters.none_e) ?
            trueEvents[c].isEmpty() : trueEvents[c].contains(e));
    }

    private void renderWidget(Widget widget, boolean printUnused, int n,
                              HashSet<Integer>[] trueEvents, StringBuffer buf)
    {
        boolean[] used = new boolean[events.length];
        for(int c = 0; c < widget.events.length; c++)
        {
            int i = 0;
            while (i < n) // Segment into entries
            {
                int e = widget.events[c][i];
                if (Parameters.isRealEvent(e))
                {
                    used[e] = true;
                }
                int j = i + 1;
                while (j < n && widget.events[c][j] == e)
                {
                    j += 1;
                }
                buf.append("\t").append((widget == trueWidget || isOkay(c, e, trueEvents)) ? "" : "*").append("[TRACK").append(c).append("] ");
                if (e != Parameters.none_e)
                {
                    buf.append((e == Parameters.unreachable_e) ? "(unreachable)" : events[e]).append( "[");
                }
                if (widget.fields == null || !Parameters.isRealEvent(e))
                {
                    for(int k = i; k < j; k++)
                    {
                        buf.append(Event3Model.wordToString(text[k])).append(" ");
                    }
                    buf.deleteCharAt(buf.length() - 1);
                } // if
                else
                {
                    int k = i;
                    while (k < j) // Segment i...j into fields
                    {
                        int f = widget.fields[c][k];
                        int l = k+1;
                        while (l < j && widget.fields[c][l] == f)
                        {
                            l += 1;
                        }
                        if (k != i)
                        {
                            buf.append(" ");
                        }
                        if (f != -1)
                        {
                            buf.append(events[e].fieldToString(f)).append("[");
                        }
                        for(int m = k; m < l; m++)
                        {
                            String str = Event3Model.wordToString(text[m]);
                            if (widget.gens != null && widget.gens[c][m] != -1)
                            {
                                str += "_" + Parameters.short_gstr[widget.gens[c][m]];
                            }
                            if (widget.numMethods != null && widget.numMethods[c][m] != -1)
                            {
                                str += Parameters.short_mstr[widget.numMethods[c][m]];
                            }
                            buf.append(str).append(" ");
                        }
                        buf.deleteCharAt(buf.length() - 1);
                        if (f != -1)
                        {
//                            buf.setCharAt(buf.length()-1, ']');// append("]");
                            buf.append("]");
                        }
                        k = l;
                    }
                } // else
                if (e != Parameters.none_e)
                {
//                    buf.setCharAt(buf.length()-1, ']');
                    buf.append("]");
                }
                i = j;
            } // while
        } // for

        // Print out unused events
        if (printUnused)
        {
            for(int e = 0; e < events.length; e++)
            {
                if (!used[e])
                    buf.append(Utils.fmts("\t%s[]", events[e]));
            } // for
        } // if
    }

    // Compute number of events of each type we have
    void computeEventTypeCounts()
    {
        eventTypeCounts = new int[model.getT()];
//        for(Event event : events)
        for(int i = 0; i < events.length && events[i] != null; i++)
        {
            eventTypeCounts[events[i].getEventTypeIndex()]++;
        }
    }

    // Set up trackEvents: for each track
    void computeTrackEvents()
    {
        trackEvents = new int[C][events.length];
        for(int c = 0; c < C; c++)
        {
            for(int e = 0; e < events.length && events[e] != null; e++)
            {
                if(model.eventTypeAllowedOnTrack[c].contains(
                        events[e].getEventTypeIndex()))
                {
//                    trackEvents[c][e] = e; // not sure
                    trackEvents[c][e] = events[e].id; // id instead of index in the array
                }
            } // for
        } // for
    }

    @Override
    public Widget getTrueWidget()
    {
        return trueWidget;
    }
}
