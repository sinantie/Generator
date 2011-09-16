package induction.problem.event3;

import induction.problem.event3.params.NumFieldParams;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.event3.Constants;
import java.io.Serializable;

/**
 * Numeric (#) - only integers allowed; words represent noisy versions of the value
 * Example: temperature in the weather domain
 * @author konstas
 */
public class NumField extends Field implements Serializable
{
    static final long serialVersionUID = 5656113283260370518L;
    
    private int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

    public NumField() {}
    public NumField(String fieldName)
    {
        this.name = fieldName;
        maxLength = 1;
    }

    public int getV()
    {
        return max - min + 1; // Range (inclusive)
    }

    @Override
    public String valueToString(int v)
    {
        return String.valueOf(v);
    }

    @Override
    public int parseValue(int role, String str)
    {
       int x = Constants.str2numOrFail(str);
       if (x < min) min = x;
       if (x > max) max = x;
       return x;
    }

    @Override
    public AParams newParams(String prefix)
    {
        return new NumFieldParams(prefix);
    }

    @Override
    public String toString()
    {
        return Utils.fmts("#%s(%s..%s)", name, min, max);
    }


}
