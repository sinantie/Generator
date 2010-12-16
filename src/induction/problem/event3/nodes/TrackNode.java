package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class TrackNode extends EventsNode{
    int c; boolean allowNone, allowReal;
    public TrackNode(int i, int j, int t0, int c, boolean allowNone, boolean allowReal) {
        super(i, j, t0);
        this.c = c;
        this.allowNone = allowNone;
        this.allowReal = allowReal;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, t0, c, allowNone, allowReal);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof TrackNode;
        if(!(obj instanceof TrackNode))
            return false;
        TrackNode node = (TrackNode) obj;
        return i == node.i && j == node.j && t0 == node.t0 && c == node.c &&
                allowNone == node.allowNone && allowReal == node.allowReal;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.i;
        hash = 29 * hash + this.j;
        hash = 29 * hash + this.t0;
        hash = 29 * hash + this.c;
        hash = 29 * hash + (this.allowNone ? 1 : 0);
        hash = 29 * hash + (this.allowReal ? 1 : 0);
        return hash;
    }
}
