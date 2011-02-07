package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.event3.Example;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public abstract class FieldParams extends AParams implements Serializable
{
    static final long serialVersionUID = -4262178904641568053L;
    
    public FieldParams()
    {
        super();
    }

    // Provide potentials for salience
    protected double getFilter(Example ex, int e, int f)
    {
        return 1.0;
    }
    protected void updateFilter(Example ex, int e, int f, double prob)
    { }
}
