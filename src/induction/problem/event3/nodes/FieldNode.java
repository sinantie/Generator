package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class FieldNode extends Node{
    int c, event, field;
    public FieldNode(int begin, int end, int c, int event, int field){
        super(begin, end);
        this.c = c;
        this.event = event;
        this.field = field;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c, event, field);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof FieldNode : obj.toString();
        if (!(obj instanceof FieldNode))
            return false;
        FieldNode node = (FieldNode) obj;
        return i == node.i && j == node.j && c == node.c &&
                event == node.event && field == node.field;
    }
    @Override
    public int hashCode() {
        int hash = 101;
        hash = 29 * hash + this.i;
        hash = 29 * hash + this.j;
        hash = 29 * hash + this.c;
        hash = 29 * hash + this.event;
        hash = 29 * hash + this.field;
        return hash;
    }
}