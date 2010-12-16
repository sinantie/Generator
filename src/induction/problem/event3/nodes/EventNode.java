package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
 public class EventNode extends Node{
    int c, event;
    public EventNode(int i, int j, int c, int event) {
        super(i, j);
        this.c = c;
        this.event = event;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c, event);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof EventNode;
        if(!(obj instanceof EventNode))
            return false;
        EventNode node = (EventNode) obj;
        return i == node.i && j == node.j && c == node.c && event == node.event;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.i;
        hash = 29 * hash + this.j;
        hash = 29 * hash + this.c;
        hash = 29 * hash + this.event;
        return hash;
    }
}