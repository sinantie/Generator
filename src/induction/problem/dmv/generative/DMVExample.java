package induction.problem.dmv.generative;

import induction.DepTree;
import induction.problem.wordproblem.Example;
import induction.problem.wordproblem.WordModel;
import java.util.List;

/**
 *
 * @author konstas
 */
public class DMVExample extends Example<DepTree>
{
    private String[][] rawText; // contains input example in the CoNLL format
    private int conllHeadPos;
    
    public DMVExample(WordModel model, int[] text, DepTree trueWidget)
    {
        super(model, text, trueWidget);        
    }
    
    public DMVExample(WordModel model, int[] text, DepTree trueWidget, String name)
    {
        super(model, text, trueWidget, name);        
    }
    
    public DMVExample(WordModel model, int[] text, DepTree trueWidget, String name, String[][] rawText, int conllHeadPos)
    {
        super(model, text, trueWidget, name);
        this.rawText = rawText;
        this.conllHeadPos = conllHeadPos;
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
    
    public String widgetToNiceConllString(DepTree widget)
    {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < rawText.length; i++)
        {
            out.append(rawText[i][0]);
            for(int j = 1; j < rawText[i].length; j++)
            {
                out.append("\t").append(j == conllHeadPos ? conllGetParent(widget, i) : rawText[i][j]);
            }
            out.append("\n");
        }
        //out.append("\n");
        return out.toString();
    }
    
    private int conllGetParent(DepTree widget, int pos)
    {
        int head = widget.getParent()[pos];
        return head == pos ? 0 : head + 1;
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
