package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class StopNode extends Node{
    int t0;
    public StopNode(int i, int t0) {
        super(i);
        this.t0 = t0;
    }
 
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, t0);
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StopNode))
            return false;
        StopNode node = (StopNode) obj;
        return i == node.i && t0 == node.t0;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.i;
        hash = 67 * hash + this.t0;
        return hash;
    }
}
