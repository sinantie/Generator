package induction.problem.event3.planning;

import induction.problem.wordproblem.Example;
import induction.problem.wordproblem.WordModel;

/**
 *
 * @author konstas
 */
public class PlanningExample extends Example<PlanningWidget>
{

    public PlanningExample(WordModel model, int[] eventTypeIds, PlanningWidget trueWidget) 
    {
        super(model, eventTypeIds, trueWidget);
    }

    public PlanningExample(WordModel model, int[] eventTypeIds, PlanningWidget trueWidget, String name) 
    {
        super(model, eventTypeIds, trueWidget, name);
    }
    
}
