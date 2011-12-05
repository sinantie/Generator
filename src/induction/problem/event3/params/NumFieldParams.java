package induction.problem.event3.params;

import induction.problem.Vec;
import induction.problem.VecFactory;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public class NumFieldParams extends FieldParams implements Serializable
{
    static final long serialVersionUID = 7731373078762243950L;    
    public  Vec methodChoices, leftNoiseChoices, rightNoiseChoices;
    private Vec[] filters;

    public NumFieldParams(VecFactory.Type vectorType, String prefix)
    {
        super(vectorType, prefix);

        methodChoices = VecFactory.zeros(vectorType, Parameters.M); // m -> use method m to generate numbers
        addVec("numMethodChoices" + prefix, methodChoices);
        leftNoiseChoices = VecFactory.zeros(vectorType, Parameters.S); // s -> generate noise s
        addVec("numLNoiseChoices" + prefix, leftNoiseChoices);
        rightNoiseChoices = VecFactory.zeros(vectorType, Parameters.S);// s -> generate noise s
        addVec("numRNoiseChoices" + prefix, rightNoiseChoices);
        // h, b -> If the field value is in histogram bin h,
        // should we talk about this field?
        filters = VecFactory.zeros2(vectorType, Parameters.H, Parameters.B);
        addVec(getLabels(Parameters.H, "numFilter " + prefix + " ",
                Parameters.histToString), filters);
    }

    @Override
    public String output(ParamsType paramsType)
    {
        String out = super.output(paramsType);
        if(paramsType == ParamsType.PROBS)
            out += forEachProb(methodChoices,
                   getLabels(Parameters.M, "numMethodC " + prefix + " ",
                              Parameters.numMethodsToString)) +
                   forEachProb(leftNoiseChoices,
                   getLabels(Parameters.S, "numLNoiseC " + prefix + " ",
                              Parameters.noiseToString)) +
                   forEachProb(rightNoiseChoices,
                   getLabels(Parameters.S, "numRNoiseC " + prefix + " ",
                              Parameters.noiseToString));
        else
            out += forEachCount(methodChoices,
                   getLabels(Parameters.M, "numMethodC " + prefix + " ",
                              Parameters.numMethodsToString)) +
                   forEachCount(leftNoiseChoices,
                   getLabels(Parameters.S, "numLNoiseC " + prefix + " ",
                              Parameters.noiseToString)) +
                   forEachCount(rightNoiseChoices,
                   getLabels(Parameters.S, "numRNoiseC " + prefix + " ",
                              Parameters.noiseToString));
        String[][] labels = getLabels(Parameters.H, Parameters.B, "numFilter " + prefix + " ",
                Parameters.histToString, Parameters.booleanToString);
        int i = 0;
        for(Vec v : filters)
        {
            if(paramsType == ParamsType.PROBS)
                out += forEachProb(v, labels[i++]);
            else
                out += forEachProb(v, labels[i++]);
        }
        return out;
    }    

}
