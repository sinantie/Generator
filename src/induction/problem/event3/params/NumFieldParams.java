package induction.problem.event3.params;

import induction.problem.ProbVec;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public class NumFieldParams extends FieldParams implements Serializable
{
    static final long serialVersionUID = 7731373078762243950L;
    
    private String prefix;
    public  ProbVec methodChoices, leftNoiseChoices, rightNoiseChoices;
    private ProbVec[] filters;

    public NumFieldParams(String prefix)
    {
        super();
        this.prefix = prefix;

        methodChoices = ProbVec.zeros(Parameters.M); // m -> use method m to generate numbers
        addVec(methodChoices);
        leftNoiseChoices = ProbVec.zeros(Parameters.S); // s -> generate noise s
        addVec(leftNoiseChoices);
        rightNoiseChoices = ProbVec.zeros(Parameters.S);// s -> generate noise s
        addVec(rightNoiseChoices);
        // h, b -> If the field value is in histogram bin h,
        // should we talk about this field?
        filters = ProbVec.zeros2(Parameters.H, Parameters.B);
        addVec(filters);
    }

    @Override
    public String output()
    {
        String out = "";
        out += forEachProb(methodChoices,
               getLabels(Parameters.M, "numMethodC " + prefix + " ",
                          Parameters.numMethodsToString)) +
               forEachProb(leftNoiseChoices,
               getLabels(Parameters.S, "numLNoiseC " + prefix + " ",
                          Parameters.noiseToString)) +
               forEachProb(rightNoiseChoices,
               getLabels(Parameters.S, "numRNoiseC " + prefix + " ",
                          Parameters.noiseToString));
        String[][] labels = getLabels(Parameters.H, Parameters.B, "numFilter " + prefix + " ",
                Parameters.histToString, Parameters.booleanToString);
        int i = 0;
        for(ProbVec v : filters)
        {
            out += forEachProb(v, labels[i++]);
        }
        return out;
    }    

}
