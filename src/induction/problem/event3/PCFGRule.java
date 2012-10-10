package induction.problem.event3;

import fig.basic.Indexer;
import java.util.List;
/**
 *
 * @author konstas
 */
public class PCFGRule
{
    private int lhs; // non-terminal left hand-side symbol
    private List<Integer> rhs; // right hand-side symbol(s)
    private Indexer<String> vocabulary;
    
    /**
     * Parse PCFG rule from String. We assume that the input is of the type
     * lhs -> rhs_1 rhs_2 ... rhs_n
     * @param rule 
     */
    public PCFGRule(String rule, Indexer<String> vocabulary)
    {
        this.vocabulary = vocabulary;
        String[] ar = rule.split("->");
        assert ar.length > 2; // make sure the rule has at least one right hand-side symbol
        lhs = vocabulary.getIndex(ar[0]);
        for(int i = 1; i < ar.length; i++)
        {           
            rhs.add(vocabulary.getIndex(ar[i]));
        }        
    }

    public int getLhs()
    {
        return lhs;
    }

    public List<Integer> getRhs()
    {
        return rhs;
    }
    
    public int getRhs1()
    {
        return rhs.get(0);
    }
    
    public int getRhs2()
    {
        assert rhs.size() > 1;
        return rhs.get(1);
    }
    
    public String getRhsToString()
    {
        StringBuilder str = new StringBuilder();
        for(int r : rhs)
            str.append(" ").append(vocabulary.getObject(r));
        return str.toString().trim();
    }
    
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(vocabulary.getObject(lhs) + " ->");
        for(int r : rhs)
            str.append(" ").append(vocabulary.getObject(r));
        return str.toString();
    }
    
    
}
