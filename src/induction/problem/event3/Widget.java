package induction.problem.event3;

import induction.problem.event3.params.Parameters;
import induction.Utils;
import induction.problem.AWidget;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A widget is a segmentation of text into events
 * 
 * @author konstas
 */
public class Widget implements AWidget
{
    protected int[][] events, fields, gens, numMethods;
    protected int[] startIndices;
    protected HashSet<Integer>[] eventTypeAllowedOnTrack;
    public String performance = ""; // HACK: store on the trueWidget how we did
    private double[] eventPosterior = null;
    protected int[] eventTypeIndices;

    public Widget(int [][]events, int[][] fields, int[][] gens,
                  int [][] numMethods,
                  int[] startIndices,
                  HashSet<Integer>[] eventTypeAllowedOnTrack, int[] eventTypeIndices)
                // These are auxiliary information needed for evaluation)
    {
        this.events = events;
        this.fields = fields;
        this.gens = gens;
        this.numMethods = numMethods;
        this.startIndices = startIndices;
        this.eventTypeAllowedOnTrack = eventTypeAllowedOnTrack;
        this.eventTypeIndices = eventTypeIndices;
    }

    // Tried to see if can use posteriors (of one track), but they're too
    // sharply peaked to be useful
    public void setEventPosterior(int e, int E, double prob)
    {
        if (eventPosterior == null)
        {
            eventPosterior = new double[E + 1];
        }
        eventPosterior[(e == Parameters.none_e) ? E : e] = prob;
    }

    public String eventPosteriorStr(Event[] events)
    {
        String out = "";
        double[] eventPosteriorSorted = Arrays.copyOf(eventPosterior, eventPosterior.length);
        Arrays.sort(eventPosteriorSorted);
        for(int e = 0; e < eventPosteriorSorted.length; e++)
        {
            if(eventPosteriorSorted[e] > 0.01)
            {
                out += "POST " + ( (e == events.length) ? "(none)" : events[e]) +
                       ":" + Utils.fmt(eventPosteriorSorted[e]) + "\t";
            }
        }
        return out;
    }

    public int[] foreachEvent(int i)
    {
        int[] out = new int[events.length];
        for(int c = 0; c < events.length; c++)
        {
            out[c] = events[c][i];
        }
        return out;
    }

    // Warning: this function is not optimized
    // Does event query_e exist at each position in i...j
    public boolean  hasContiguousEvents(int i, int j, int query_e)
    {
        boolean exists = false;
        for(int k = i; k < j; k++)
        {
            exists = false;
            for(Integer e: foreachEvent(k))
            {
                if(e.equals(query_e))
                {
                    exists = true;
                }
            } // for
            if(!exists) break;
        } // for
        return exists;
    }

    // Return true if there no event that could go in track c between i...j
    public boolean hasNoReachableContiguousEvents(int i, int j, int c)
    {
        boolean exists = false;
        for(int k = i; k < j; k++)
        {
            exists = false;
            for(Integer e : foreachEvent(k))
            {
                if(Parameters.isRealEvent(e) && eventTypeAllowedOnTrack[c].contains(eventTypeIndices[e]))
                {
                    exists = true;
                }
                if(exists) break;
            }
        }
        return !exists;
    }
}
