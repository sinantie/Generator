package induction.problem;

/**
 *
 * @author konstas
 */
public interface AExample<Widget extends AWidget>
{    
    public int N(); // might need to declare as field member
    public Widget getTrueWidget();
    public int[] getText();
    public String getName();
}
