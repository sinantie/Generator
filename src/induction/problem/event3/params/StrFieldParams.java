package induction.problem.event3.params;

import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;

/**
 *
 * @author sinantie
 */
public class StrFieldParams extends FieldParams
{
    static final long serialVersionUID = 7621297923689181992L;
    private int LB;
    public Vec[] labelChoices;
//    private GenerativeEvent3Model model;
//    private String prefix;

    public StrFieldParams(Event3Model model, VecFactory.Type vectorType, int LB, String prefix)
    {
        super(model, vectorType, prefix);
        this.LB = LB;
//        this.prefix = prefix;
        // lb1, lb2 -> probability of transforming label lb1 to lb2
        labelChoices = VecFactory.zeros2(vectorType, LB, LB);
//        addVec(labelChoices);
        addVec(getLabels(LB, "labelC " + prefix + " ",
                          GenerativeEvent3Model.labelsToStringArray()), labelChoices);
    }

    @Override
    public String output(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder(super.output(paramsType));
        String[][] labels = getLabels(LB, LB, "labelC " + prefix + " ",        
                          GenerativeEvent3Model.labelsToStringArray(),
                          GenerativeEvent3Model.labelsToStringArray());
        int i = 0;
        for(Vec v: labelChoices)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProb(v, labels[i++]));
            else
                out.append(forEachCount(v, labels[i++]));
        }
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder(super.output(paramsType));
        String[][] labels = getLabels(LB, LB, "labelC " + prefix + " ",        
                          GenerativeEvent3Model.labelsToStringArray(),
                          GenerativeEvent3Model.labelsToStringArray());
        int i = 0;
        for(Vec v: labelChoices)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProbNonZero(v, labels[i++]));
            else
                out.append(forEachCountNonZero(v, labels[i++]));
        }
        return out.toString();
    }

}
