package induction.problem.event3.params;

import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.CatField;
import induction.problem.event3.generative.GenerativeEvent3Model;

/**
 *
 * @author konstas
 */
public class CatFieldParams extends FieldParams
{
    static final long serialVersionUID = 5817675789060800073L;
    public Vec[] emissions, filters, valueEmissions;
//    private String prefix;
    private CatField field;
    private int W;
    public CatFieldParams(VecFactory.Type vectorType, int W, String prefix, CatField field)
    {
        super(vectorType, prefix);
        this.W = W;
//        this.prefix = prefix;
        this.field = field;
        // v, w -> express value v with word w
        emissions = VecFactory.zeros2(vectorType, field.getV(), W);
        addVec(getLabels(field.getV(), "catE " + prefix + " ",
                    field.valuesToStringArray()), emissions);

// uncomment for semantic parsing
//        valueEmissions = VecFactory.zeros2(vectorType, W, field.getV());
//        addVec(getLabels(W, "catVE " + prefix + " ",
//                    GenerativeEvent3Model.wordsToStringArray()), valueEmissions);

        filters = VecFactory.zeros2(vectorType, field.getV(), Parameters.B);
        addVec(getLabels(field.getV(), "catFilter " + prefix + " ",
                    field.valuesToStringArray()), filters);
    }

    @Override
    public String output(ParamsType paramsType)
    {
        String out = super.output(paramsType);
        String[][] labels = getLabels(field.getV(), W, "catE " + prefix + " ",
                    field.valuesToStringArray(), GenerativeEvent3Model.wordsToStringArray());
        int i = 0;
        for(Vec v: emissions)
        {
            if(paramsType == ParamsType.PROBS)
                out += forEachProb(v, labels[i++]);
            else
                out += forEachCount(v, labels[i++]);
        }
        labels = getLabels(W, field.getV(), "catVE " + prefix + " ",
                    GenerativeEvent3Model.wordsToStringArray(), field.valuesToStringArray());
        i = 0;

// uncomment for semantic parsing
//        for(ProbVec v: valueEmissions)
//        {
//            out += forEachProb(v, labels[i++]);
//        }
        labels = getLabels(field.getV(), Parameters.B, "catFilter " + prefix + " ",
                    field.valuesToStringArray(), Parameters.booleanToString);
        i = 0;
        for(Vec v: filters)
        {
            if(paramsType == ParamsType.PROBS)
                out += forEachProb(v, labels[i++]);
            else
                out += forEachCount(v, labels[i++]);
                
        }
        return out;
    }
}