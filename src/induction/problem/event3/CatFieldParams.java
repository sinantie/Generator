package induction.problem.event3;

import induction.problem.ProbVec;

/**
 *
 * @author konstas
 */
class CatFieldParams extends FieldParams
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
        emissions = ProbVec.zeros2(field.V(), W);
        addVec(emissions);
        filters = ProbVec.zeros2(field.V(), Parameters.B);
        addVec(filters);
    }

    @Override
    public String output()
    {
        String out = "";
        String[][] labels = getLabels(field.V(), W, "catE " + prefix + " ",
                    field.valuesToStringArray(), Event3Model.wordsToStringArray());
        int i = 0;
        for(ProbVec v: emissions)
        {
            out += forEachProb(v, labels[i++]);
        }
        labels = getLabels(field.V(), Parameters.B, "catFilter " + prefix + " ",
                    field.valuesToStringArray(), Parameters.booleanToString);
        i = 0;
        for(ProbVec v: filters)
        {
            out += forEachProb(v, labels[i++]);
        }
        return out;
    }

}
