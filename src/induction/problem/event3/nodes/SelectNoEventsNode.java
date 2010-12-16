package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
 public class SelectNoEventsNode extends Node{
    int c;
    public SelectNoEventsNode(int i, int c) {
        super(i);
        this.c = c;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, c);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof SelectNoEventsNode;
        if(!(obj instanceof SelectNoEventsNode))
            return false;
        SelectNoEventsNode node = (SelectNoEventsNode) obj;
        return i == node.i && c == node.c;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.i;
        hash = 43 * hash + this.c;
        return hash;
    }
}