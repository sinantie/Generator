package induction.problem.event3.discriminative;

import induction.problem.AExample;
import induction.problem.APerformance;
import induction.problem.event3.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author konstas
 */
public class DiscriminativePerformance extends APerformance<Widget>
{
    List<Double> gradientList = new ArrayList<Double>();
    
    public DiscriminativePerformance()
    {
    }    

    @Override
    public double getAccuracy()
    {
        return stats.getAvg_logVZ();
    }

    @Override
    protected void add(Widget trueWidget, Widget predWidget)
    {
        
    }

    @Override
    public void add(AExample trueWidget, Widget predWidget)
    {
       
    }

    @Override
    public String output()
    {
        return "Average Viterbi logZ: " + getAccuracy();
    }
    
}
