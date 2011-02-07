package induction.problem.event3;

import induction.problem.AParams;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public abstract class Field implements Serializable
{
    static final long serialVersionUID = -6112633552247057265L;
    protected String name;
    
    protected int maxLength;

    public abstract int getV(); // number of possible values
    public abstract String valueToString(int v);
    protected abstract int parseValue(int role, String str);
    public abstract AParams newParams(String prefix);
}
