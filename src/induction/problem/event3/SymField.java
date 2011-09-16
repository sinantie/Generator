package induction.problem.event3;

import induction.problem.event3.params.SymFieldParams;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.event3.generative.GenerativeEvent3Model;

/**
 * Symbol (:) - symbol values must match words verbatim
 * (can use symbols to encode numbers where we expect no noise)
 * This is a special case of a string
 * Example: entity, relPoints in the NFL domain
 *
 * @author konstas
 */
public class SymField extends Field
{
    static final long serialVersionUID = -2578578447679981583L;
    
//    int LB, W;

    public SymField() {}
    public SymField(String fieldName)
    {
//        this.W = W;
//        this.LB = LB;
        name = fieldName;
        maxLength = 1;
    }

    @Override
    public int getV()
    {
        return GenerativeEvent3Model.W();
    }
    
    @Override
    public String valueToString(int v)
    {
        return GenerativeEvent3Model.wordToString(v);
    }

    @Override
    public int parseValue(int role, String str)
    {
        return GenerativeEvent3Model.getWordIndex(str);
    }

    @Override
    public AParams newParams(String prefix)
    {
        return new SymFieldParams(GenerativeEvent3Model.LB(), prefix);
    }

    @Override
    public String toString()
    {
        return Utils.fmts(":%s", name);
    }
}
