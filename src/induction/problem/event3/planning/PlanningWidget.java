package induction.problem.event3.planning;

import induction.problem.event3.Widget;

/**
 *
 * @author konstas
 */
public class PlanningWidget extends Widget 
{
    private int[] eventTypeIds;
    private double logVZ;
    
    public PlanningWidget(int[] eventTypeIds) 
    {
        super(null, null, null, null, null, null, null);
        this.eventTypeIds = eventTypeIds;
    }

    public int[] getEventTypeIds() 
    {
        return eventTypeIds;
    }

    public void setLogVZ(double logVZ) 
    {
        this.logVZ = logVZ;
    }

    public double getLogVZ() 
    {
        return logVZ;
    }        
}
