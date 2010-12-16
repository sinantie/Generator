package induction.problem.wordproblem;

import induction.problem.AExample;
import induction.problem.AWidget;

/**
 *
 * @author konstas
 */
public class WordExample<Widget extends AWidget> implements AExample<Widget>
{
    protected int[] words;
    private Widget trueWidget;

    public WordExample() {}
    public WordExample(int[] words, Widget trueWidget)
    {
        this.words = words;
        this.trueWidget = trueWidget;
    }

    public int N()
    {
        return words.length;
    }

    public Widget getTrueWidget()
    {
        return trueWidget;
    }
}
