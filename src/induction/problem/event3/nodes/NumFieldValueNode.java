package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class NumFieldValueNode extends Node {
    int c, event, field;
    public NumFieldValueNode(int i, int c, int event, int field) {
        super(i);
        this.c = c;
        this.event = event;
        this.field = field;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, c, event, field);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof NumFieldValueNode;
        if(!(obj instanceof NumFieldValueNode))
            return false;
        NumFieldValueNode node = (NumFieldValueNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.i;
        hash = 89 * hash + this.c;
        hash = 89 * hash + this.event;
        hash = 89 * hash + this.field;
        return hash;
    }
}
