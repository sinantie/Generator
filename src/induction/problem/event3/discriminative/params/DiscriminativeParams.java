package induction.problem.event3.discriminative.params;

import induction.Options;
import induction.problem.AParams;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;
import induction.problem.event3.params.Params;

/**
 *
 * @author konstas
 */
public class DiscriminativeParams extends AParams
{
    Params alignParams;
    public ProbVec baselineParams;
    
    public DiscriminativeParams(Event3Model model, Options opts)
    {
        alignParams = new Params(model, opts);
        addVec(alignParams.getVecs());
        baselineParams = ProbVec.zeros(1);
        addVec("baseline", baselineParams);
    }
    
    @Override
    public String output()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
