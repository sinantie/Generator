package induction.problem.event3;

import fig.basic.Indexer;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author konstas
 */
public class CFGRule
{
    private int lhs; // non-terminal left hand-side symbol
    private List<Integer> rhs; // right hand-side symbol(s)
    private Indexer<String> vocabulary;

    public CFGRule(int lhs, int rhs1, int rhs2)
    {
        this.lhs = lhs;
        this.rhs = new ArrayList<Integer>();
        this.rhs.add(rhs1);
        this.rhs.add(rhs2);
    }
    
    
    /**
     * Parse PCFG rule from String. We assume that the input is of the type
     * lhs -> rhs_1 rhs_2 ... rhs_n
     * @param rule 
     */
    public CFGRule(String rule, Indexer<String> vocabulary)
    {
        this.vocabulary = vocabulary;
        String[] ar = rule.split("->");        
        assert ar.length > 1; // make sure the rule has at least one right hand-side symbol
        lhs = vocabulary.getIndex(ar[0]);
        ar = ar[1].trim().split(" ");
        rhs = new ArrayList<Integer>(ar.length);
        for(String r : ar)
        {           
            rhs.add(vocabulary.getIndex(r));
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

    @Override
    public boolean equals(Object obj)
    {
        assert obj instanceof CFGRule;
        CFGRule rule = (CFGRule)obj;
        return this.lhs == rule.lhs && this.rhs.equals(rule.rhs);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 31 * hash + this.lhs;
        hash = 31 * hash + (this.rhs != null ? this.rhs.hashCode() : 0);
        return hash;
    }
    
    
}
