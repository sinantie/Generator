package induction.problem.wordproblem;

import fig.basic.Indexer;
import induction.problem.AExample;

/**
 *
 * @author konstas
 */
public class Example<Widget> implements AExample<Widget>
{
    protected final String name;
    protected final int numTokens;
    protected int[] text;
    protected Widget trueWidget;
    
    public Example(int[] words, Widget trueWidget)
    {
        this(words, trueWidget, "");
    }
    
    public Example(int[] text, Widget trueWidget, String name)
    {
        this.trueWidget = trueWidget;
        this.name = name;
        this.text = text;
        this.numTokens = text.length;
    }        
    
    @Override
    public int N()
    {
        return numTokens;
    }

    @Override
    public Widget getTrueWidget()
    {
        return trueWidget;
    }

    @Override
    public int[] getText()
    {
        return text;
    }

    @Override
    public String getName()
    {
        return name;
    }
    
}
