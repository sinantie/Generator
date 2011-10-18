package induction.problem.event3.discriminative;

import fig.basic.ListUtils;
import induction.MyList;
import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.generation.GenerationPerformance;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author konstas
 */
public class DiscriminativePerformance extends GenerationPerformance
{
    List<Double> gradientList = new ArrayList<Double>();
    
    public DiscriminativePerformance(Event3Model model)
    {
        super(model);
    }    

    public void add(double gradient)
    {
        gradientList.add(gradient);
    }
    
    private double getAverageGradientNorm()
    {
        return ListUtils.mean(gradientList);
    }
    
    @Override
    public double getAccuracy()
    {
        return getAverageGradientNorm();
    }
   
    @Override
    protected MyList<String> foreachStat()
    {
        MyList<String> list = new MyList();
        list.add( "Average Gradient Norm", Utils.fmt(getAverageGradientNorm()) );
        list.addAll(super.foreachStat());
        return list;
    }
    
    @Override
    public String output()
    {
        return "Average gradient norm: " + getAccuracy() + "\n" +
                super.output();
    }
    
}
