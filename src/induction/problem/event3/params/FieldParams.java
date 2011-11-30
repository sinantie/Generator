package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.Example;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public abstract class FieldParams extends AParams implements Serializable
{
    static final long serialVersionUID = -4262178904641568053L;
    public Vec[] wordBigramChoices;
    private int W;
    protected String prefix;

    public FieldParams(VecFactory.Type vectorType, String prefix)
    {
        super();
        this.W = GenerativeEvent3Model.W();
        this.prefix = prefix;
        wordBigramChoices = VecFactory.zeros2(vectorType, W, W);
        addVec(getLabels(W, "wordBi "  + prefix + " ",
                    GenerativeEvent3Model.wordsToStringArray()), wordBigramChoices);
    }

    // Provide potentials for salience
    protected double getFilter(Example ex, int e, int f)
    {
        return 1.0;
    }
    protected void updateFilter(Example ex, int e, int f, double prob)
    { }

    @Override
    public String output()
    {
        String out = "";
        String[][] labels = getLabels(W, W, "wordBiC " + prefix + " ",
                    GenerativeEvent3Model.wordsToStringArray(), GenerativeEvent3Model.wordsToStringArray());
        int i = 0;
        // if too huge parameter set, comment
//        for(ProbVec v: wordBigramChoices)
//        {
//            out += forEachProb(v, labels[i++]);
//        }
        return out;
    }
}
