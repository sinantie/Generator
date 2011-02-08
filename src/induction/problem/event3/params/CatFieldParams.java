package induction.problem.event3.params;

import induction.problem.ProbVec;
import induction.problem.event3.CatField;
import induction.problem.event3.Event3Model;

/**
 *
 * @author konstas
 */
public class CatFieldParams extends FieldParams
{
    static final long serialVersionUID = 5817675789060800073L;
    public ProbVec[] emissions, filters;
    private String prefix;
    private CatField field;
    private int W;
    public CatFieldParams(int W, String prefix, CatField field)
    {
        super();
        this.W = W;
        this.prefix = prefix;
        this.field = field;
        // v, w -> express value v with word w
        emissions = ProbVec.zeros2(field.getV(), W);
        addVec(emissions);
        filters = ProbVec.zeros2(field.getV(), Parameters.B);
        addVec(filters);
    }

    @Override
    public String output()
    {
        String out = "";
        String[][] labels = getLabels(field.getV(), W, "catE " + prefix + " ",
                    field.valuesToStringArray(), Event3Model.wordsToStringArray());
        int i = 0;
        for(ProbVec v: emissions)
        {
            out += forEachProb(v, labels[i++]);
        }
        labels = getLabels(field.getV(), Parameters.B, "catFilter " + prefix + " ",
                    field.valuesToStringArray(), Parameters.booleanToString);
        i = 0;
        for(ProbVec v: filters)
        {
            out += forEachProb(v, labels[i++]);
        }
        return out;
    }

}