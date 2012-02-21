package induction.problem.wordproblem;

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
    protected WordModel model;
    
    public Example(WordModel model, int[] words, Widget trueWidget)
    {        
        this(model, words, trueWidget, "");
    }
    
    public Example(WordModel model, int[] text, Widget trueWidget, String name)
    {
        this.model = model;
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
