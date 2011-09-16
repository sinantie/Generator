package induction.problem.event3.generative.generation;

import induction.problem.event3.MRToken;
import induction.problem.event3.generative.generation.GenWidget;
import induction.problem.event3.params.Parameters;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author konstas
 */
public class SemParseWidget extends GenWidget
{
    // we use a collection of MRTokens for the gold-standard instead of the
    // conventional arrays because they are not based on N (text length)
    // because
    protected Collection<MRToken> trueMrTokens;
    
    public SemParseWidget(int [][]events, int[][] fields, int[][] gens,
                     int [][] numMethods,
                     int [] values,
                     HashSet<Integer>[] eventTypeAllowedOnTrack,
                     Map<Integer, Integer> eventTypeIndices)
    {
        super(events, fields, gens, numMethods, values,
              eventTypeAllowedOnTrack, eventTypeIndices);
        scores = new double[Parameters.NUMBER_OF_METRICS_SEM_PAR];
    }

    /**
     * Constructor for gold-standard widget.
     * @param trueMrTokens the true events, for calculating Precision, Recall and F-1
     * @param values the gold-standard values, for calculating generation-oriented metrics
     */
    public SemParseWidget(Collection<MRToken> trueMrTokens)
    {
         this(null, null, null, null, null, null, null);
         this.trueMrTokens = trueMrTokens;
    }

    public Collection<MRToken> getTrueMrTokens()
    {
        return trueMrTokens;
    }
        
}
