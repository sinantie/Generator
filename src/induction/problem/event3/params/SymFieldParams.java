package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.generative.GenerativeEvent3Model;

/**
 *
 * @author konstas
 */
public class SymFieldParams extends AParams
{
    static final long serialVersionUID = 2095423547961557491L;

    private int LB;
    public Vec labelChoices;
    private String prefix;

    public SymFieldParams(VecFactory.Type vectorType, int LB, String prefix)
    {
        super();
        this.LB = LB;
        this.prefix = prefix;
        // lb -> probability of producing labels
        labelChoices = VecFactory.zeros(vectorType, LB);
//        addVec(labelChoices);
        addVec("labelChoices" + prefix, labelChoices);
    }

    @Override
    public String output(ParamsType paramsType)
    {
        if(paramsType == ParamsType.PROBS)
            return forEachProb(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray()));
        else
            return forEachCount(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray()));
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        if(paramsType == ParamsType.PROBS)
            return forEachProbNonZero(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray()));
        else
            return forEachCountNonZero(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray()));
    }

}
