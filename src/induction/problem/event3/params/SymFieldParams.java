package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;

/**
 *
 * @author konstas
 */
public class SymFieldParams extends AParams
{
    static final long serialVersionUID = 2095423547961557491L;

    private int LB;
    public ProbVec labelChoices;
    private String prefix;

    public SymFieldParams(int LB, String prefix)
    {
        super();
        this.LB = LB;
        this.prefix = prefix;
        // lb -> probability of producing labels
        labelChoices = ProbVec.zeros(LB);
//        addVec(labelChoices);
        addVec("labelChoices", labelChoices);
    }

    @Override
    public String output()
    {
        return forEachProb(labelChoices,
                getLabels(LB, "labelC " + prefix + " " ,
                          Event3Model.labelsToStringArray()));
    }

}
