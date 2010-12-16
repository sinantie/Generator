package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class StringFieldValueNode extends Node{
    int c, event, field;
    public StringFieldValueNode(int i, int c, int event, int field) {
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
//        assert obj instanceof StringFieldValueNode;
        if(!(obj instanceof StringFieldValueNode))
            return false;
        StringFieldValueNode node = (StringFieldValueNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.i;
        hash = 61 * hash + this.c;
        hash = 61 * hash + this.event;
        hash = 61 * hash + this.field;
        return hash;
    }
}