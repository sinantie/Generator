package induction.problem.event3.params;

import induction.problem.ProbVec;
import induction.problem.event3.generative.Event3Model;

/**
 *
 * @author sinantie
 */
public class StrFieldParams extends FieldParams
{
    static final long serialVersionUID = 7621297923689181992L;
    private int LB;
    public ProbVec[] labelChoices;
    private Event3Model model;
//    private String prefix;

    public StrFieldParams(int LB, String prefix)
    {
        super(prefix);
        this.LB = LB;
//        this.prefix = prefix;
        // lb1, lb2 -> probability of transforming label lb1 to lb2
        labelChoices = ProbVec.zeros2(LB, LB);
//        addVec(labelChoices);
        addVec(getLabels(LB, "labelC " + prefix + " ",
                          Event3Model.labelsToStringArray()), labelChoices);
    }

    @Override
    public String output()
    {
        String out = super.output();
        String[][] labels = getLabels(LB, LB, "labelC " + prefix + " ",        
                          Event3Model.labelsToStringArray(),
                          Event3Model.labelsToStringArray());
        int i = 0;
        for(ProbVec v: labelChoices)
        {
            out += forEachProb(v, labels[i++]);
        }
        return out;
    }

}
