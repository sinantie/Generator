package induction.problem.event3.generative.planning;

import induction.Hypergraph;
import induction.problem.InferSpec;
import induction.problem.event3.Event3InferState;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.TrackParams;
import induction.problem.event3.planning.PlanningExample;
import induction.problem.event3.planning.PlanningWidget;

/**
 *
 * @author konstas
 */
public class PlanningInferState extends Event3InferState 
{

    public PlanningInferState(Event3Model model, Example ex, Params params, Params counts, InferSpec ispec) 
    {
        super(model, ex, params, counts, ispec);
    }
       
    @Override
    protected PlanningWidget newWidget() 
    {
        return new PlanningWidget(newMatrixOne());
    }
       
    @Override
    protected void createHypergraph(Hypergraph<Widget> hypergraph) 
    {
        // No need to build a hypergraph. Just multiply the stationary probabilities
        // for each transition in the input sequence.
    }

    @Override
    public void doInference() 
    {
        final TrackParams cparams = params.trackParams[0];
        int[] eventTypeIndices = ((PlanningExample)ex).getEventTypeIds();
        // chain probabilities
        double logFit = 0;
        for(int i = 1; i < eventTypeIndices.length; i++)
        {
            logFit += getLogProb(cparams.getEventTypeChoices()[eventTypeIndices[i-1]], eventTypeIndices[i]);
        }
        if(opts.useStopNode)
        {
            logFit += getLogProb(cparams.getEventTypeChoices()[eventTypeIndices[N-1]], cparams.boundary_t);
        }
        PlanningWidget result = newWidget();
        result.setLogVZ(logFit);
        bestWidget = result;
    }
    
    @Override
    public void updateCounts() {}

}
