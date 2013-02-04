package induction.problem.event3.planning;

import induction.problem.AWidget;

/**
 *
 * @author konstas
 */
public class PlanningWidget implements AWidget 
{
    private int[] eventTypeIds;

    public PlanningWidget(int[] eventTypeIds) 
    {
        this.eventTypeIds = eventTypeIds;
    }

    public int[] getEventTypeIds() 
    {
        return eventTypeIds;
    }        
}
