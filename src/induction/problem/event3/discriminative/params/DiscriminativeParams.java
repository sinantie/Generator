package induction.problem.event3.discriminative.params;

import induction.Options;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;
import induction.problem.event3.params.Params;

/**
 * The weights of the perceptron model
 * @author konstas
 */
public class DiscriminativeParams extends Params
{
//    Params alignWeights;
    public ProbVec baselineWeight;
    
    public DiscriminativeParams(Event3Model model, Options opts)
    {
        super(model, opts);
//        alignWeights = new Params(model, opts);
//        addVec(alignWeights.getVecs());
        baselineWeight = ProbVec.zeros(1);
        addVec("baseline", baselineWeight);
    }
    
    @Override
    public String output()
    {
        String out = "";
        out += forEachProb(baselineWeight, getLabels(1, "baseline", null));
        out += super.output();
        return out;
    }
    
}
