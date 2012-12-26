package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class StringFieldValueNode extends Node{
    int c, event, field, posInFieldValue;
    public StringFieldValueNode(int i, int c, int event, int field, int posInFieldValue) {
        super(i);
        this.c = c;
        this.event = event;
        this.field = field;
        this.posInFieldValue = posInFieldValue;
    }
    public StringFieldValueNode(int i, int c, int event, int field) {
        this(i, c, event, field, -1);
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, c, event, field, posInFieldValue);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof StringFieldValueNode;
        if(!(obj instanceof StringFieldValueNode))
            return false;
        StringFieldValueNode node = (StringFieldValueNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field && posInFieldValue == node.posInFieldValue;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.i;
        hash = 61 * hash + this.c;
        hash = 61 * hash + this.event;
        hash = 61 * hash + this.field;
        hash = 61 * hash + this.posInFieldValue;
        return hash;
    }
}