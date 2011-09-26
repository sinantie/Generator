package induction.problem.event3.discriminative;

import induction.problem.AExample;
import induction.problem.APerformance;
import induction.problem.event3.Widget;

/**
 *
 * @author konstas
 */
public class DiscriminativePerformance extends APerformance<Widget>
{

    public DiscriminativePerformance()
    {
    }    

    @Override
    protected double getAccuracy()
    {
        return stats.getAvg_logVZ();
    }

    @Override
    protected void add(Widget trueWidget, Widget predWidget)
    {
        
    }

    @Override
    protected void add(AExample trueWidget, Widget predWidget)
    {
       
    }

    @Override
    protected String output()
    {
        return "Average Viterbi logZ: " + getAccuracy();
    }
    
}
