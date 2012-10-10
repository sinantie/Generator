package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class NonTerminalNode  extends Node
{
    int label;
    
    public NonTerminalNode(int i, int j, int label)
    {
        super(i, j);
        this.label = label;
    }

    @Override
    public String toString()
    {
        return debug(this.getClass().getSimpleName(), i, j, label);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NonTerminalNode))
            return false;
        NonTerminalNode node = (NonTerminalNode) obj;
        return i == node.i && j == node.j && label == node.label;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + this.i;
        hash = 79 * hash + this.j;
        hash = 79 * hash + this.label;
        return hash;
    }
    
}
