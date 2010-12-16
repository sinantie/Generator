package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
 public class EventsNode extends Node{
    protected int t0;     
    public EventsNode(int i, int j, int t0){
        super(i, j);
        this.t0 = t0;
    }
    public EventsNode(int i, int t0){
        this(i, i, t0);
    }
    public int getEventType()
    {
        return t0;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, t0);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof EventsNode;
        if (!(obj instanceof EventsNode))
            return false;
        EventsNode node = (EventsNode) obj;
        return i == node.i && t0 == node.t0;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.i;
        hash = 97 * hash + this.t0;
        return hash;
    }
}