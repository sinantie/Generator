package induction.problem.event3.json;

/**
 *
 * @author sinantie
 */
public class JsonResult implements Comparable<JsonResult>
{

    int index;
    String title, text, semanticAlignment, performance;

    public JsonResult(int index, String title, String text, String semanticAlignment, String performance)
    {
        this.index = index;
        this.title = title;
        this.text = text;
        this.semanticAlignment = semanticAlignment;
        this.performance = performance;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getTitle()
    {
        return title;
    }
        
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getSemanticAlignment()
    {
        return semanticAlignment;
    }

    public void setSemanticAlignment(String semanticAlignment)
    {
        this.semanticAlignment = semanticAlignment;
    }

    public String getPerformance()
    {
        return performance;
    }

    public void setPerformance(String performance)
    {
        this.performance = performance;
    }
           
    @Override
    public int compareTo(JsonResult o)
    {
        return this.index - o.index;
    }
}
