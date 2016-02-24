package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;
import java.io.PrintWriter;

/**
 *
 * @author konstas
 */
public class SymFieldParams extends AParams
{
    static final long serialVersionUID = 2095423547961557491L;

    private final int LB;
    public Vec labelChoices;
    private final String prefix;

    public SymFieldParams(Event3Model model, VecFactory.Type vectorType, int LB, String prefix)
    {
        super(model);
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
    public void outputNonZero(ParamsType paramsType, PrintWriter out)
    {
        if(paramsType == ParamsType.PROBS)
            out.append(forEachProbNonZero(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray())));
        else
            out.append(forEachCountNonZero(labelChoices,
                    getLabels(LB, "labelC " + prefix + " " ,
                              GenerativeEvent3Model.labelsToStringArray())));
    }

}
