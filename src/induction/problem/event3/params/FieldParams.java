package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
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
    public Vec[] wordBigramChoices;
    private int W;
    protected String prefix;

    public FieldParams(Event3Model model, VecFactory.Type vectorType, String prefix)
    {
        this(model, model.W(), vectorType, prefix);
    }
    
    public FieldParams(Event3Model model, int numOfWords, VecFactory.Type vectorType, String prefix)
    {
        super(model);
        this.W = numOfWords;
        this.prefix = prefix;
        // don't compute in cases where W is prohibitively large
        if(model.isIndepWords())
            wordBigramChoices = VecFactory.zeros2(vectorType, 0, 0);
        else
        {
            wordBigramChoices = VecFactory.zeros2(vectorType, W, W);
            addVec(getLabels(W, "wordBi "  + prefix + " ",
                        ((Event3Model)model).wordsToStringArray()), wordBigramChoices);
        }
    }

    // Provide potentials for salience
    protected double getFilter(Example ex, int e, int f)
    {
        return 1.0;
    }
    protected void updateFilter(Example ex, int e, int f, double prob)
    { }

    @Override
    public String output(ParamsType paramsType)
    {
        String[] words = ((Event3Model)model).wordsToStringArray();
        StringBuilder out = new StringBuilder();
        String[][] labels = getLabels(W, W, "wordBiC " + prefix + " ", words, words);
        int i = 0;
        // if too huge parameter set, comment
        for(Vec v: wordBigramChoices)
        {
            out.append(forEachProb(v, labels[i++]));
        }
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        String[] words = ((Event3Model)model).wordsToStringArray();
        StringBuilder out = new StringBuilder();
        String[][] labels = getLabels(W, W, "wordBiC " + prefix + " ", words, words);
        int i = 0;
        // if too huge parameter set, comment
        for(Vec v: wordBigramChoices)
        {
            out.append(forEachProbNonZero(v, labels[i++]));
        }
        return out.toString();
    }
}
