package induction.problem.event3.planning;

import induction.problem.AExample;
import induction.problem.APerformance;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Widget;

/**
 *
 * @author maria
 */
class PlanningPerformance extends APerformance<Widget> 
{

    public PlanningPerformance(Event3Model model) 
    {
        
    }

    @Override
    public double getAccuracy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void add(Widget trueWidget, Widget predWidget) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(AExample trueWidget, Widget predWidget) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String output() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
