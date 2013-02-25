package induction.problem.event3.planning;

import fig.basic.Fmt;
import induction.problem.AExample;
import induction.problem.APerformance;
import induction.problem.event3.Event3Model;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maria
 */
class PlanningPerformance extends APerformance<PlanningWidget> 
{

    Map<String, Double> goldLogVZ;
    int totalCount, totalCorrect;
    double accuracy;
    
    public PlanningPerformance(Event3Model model) 
    {
        goldLogVZ = new HashMap<String, Double>();
    }

    @Override
    public void add(AExample example, PlanningWidget predWidget)
    {
        add(example.getName(), predWidget);
    }
        
    /**
     * Compute the accuracy pairwise. If the example is the gold-standard
     * save it on the goldLogVZ map. If not, the example is a random permutation 
     * of the gold standard, so compute accuracy: 
     * correct = the predicted log score is lower
     * wrong = the predicted log score is higher
     * @param name
     * @param predWidget 
     */
    protected void add(String name, PlanningWidget predWidget)
    {
        int indexOfGold = name.indexOf("_GOLD");
        if(indexOfGold != -1)
        {
            goldLogVZ.put(name.substring(0, indexOfGold), predWidget.getLogVZ());
        }
        else
        {
            double gold = goldLogVZ.get(name);
            double pred = predWidget.getLogVZ();
            if(gold > pred)
            {
                totalCorrect++;
            }
            totalCount++;
            accuracy = (double)totalCorrect / (double)totalCount;
        }
    }
    
    @Override
    protected void add(PlanningWidget trueWidget, PlanningWidget predWidget)
    {        
        throw new UnsupportedOperationException("Not supported.");
    }   
    
    @Override
    public double getAccuracy() 
    {
        return accuracy;
    }
    
    @Override
    public String output() 
    {
         return String.format("Accuracy = %s (%s/%s)", Fmt.D(accuracy), totalCorrect, totalCount);
    }   

}
