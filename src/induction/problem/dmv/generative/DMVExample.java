package induction.problem.dmv.generative;

import induction.DepTree;
import induction.problem.wordproblem.Example;
import induction.problem.wordproblem.WordModel;

/**
 *
 * @author konstas
 */
public class DMVExample extends Example<DepTree>
{
    
    
    public DMVExample(WordModel model, int[] text, DepTree trueWidget)
    {
        super(model, text, trueWidget);        
    }
    
    public DMVExample(WordModel model, int[] text, DepTree trueWidget, String name)
    {
        super(model, text, trueWidget, name);        
    }
    
    public String widgetToNiceFullString(DepTree widget)
    {
        StringBuilder out = new StringBuilder(name);
        out.append("\n").append("Pred: ").append(renderWidget(widget));
        if(trueWidget != null)
        {
            out.append("\n").append("True: ").append(renderWidget(trueWidget)).
                append("\n").append(trueWidget.performance).append("\n");
        }        
        return out.toString();
    }
    
    /**
     * Output argument-head dependency pairs
     * @param widget
     * @return 
     */
    protected String renderWidget(DepTree widget)
    {        
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < numTokens; i++)
            out.append(i).
                append("[").append(model.wordToString(text[i])).append("]").
                append("<-").
                append(widget.getParent()[i]).
                append("[").append(model.wordToString(text[widget.getParent()[i]])).append("]").
                append(" ");        
        return out.toString();
    }
}
