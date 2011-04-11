package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public abstract class FieldParams extends AParams implements Serializable
{
    static final long serialVersionUID = -4262178904641568053L;
    public ProbVec[] wordBigramChoices;
    private int W;
    protected String prefix;

    public FieldParams(String prefix)
    {
        super();
        this.W = Event3Model.W();
        this.prefix = prefix;
        wordBigramChoices = ProbVec.zeros2(W, W);
        addVec(getLabels(W, "wordBi "  + prefix + " ",
                    Event3Model.wordsToStringArray()), wordBigramChoices);
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
                    Event3Model.wordsToStringArray(), Event3Model.wordsToStringArray());
        int i = 0;
        for(ProbVec v: wordBigramChoices)
        {
            out += forEachProb(v, labels[i++]);
        }
        return out;
    }
}
