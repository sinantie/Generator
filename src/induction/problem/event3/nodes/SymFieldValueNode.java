package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
 public class SymFieldValueNode extends Node{
    int c, event, field;
    public SymFieldValueNode(int i, int c, int event, int field) {
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
//        assert obj instanceof SymFieldValueNode;
        if(!(obj instanceof SymFieldValueNode))
            return false;
        SymFieldValueNode node = (SymFieldValueNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.i;
        hash = 47 * hash + this.c;
        hash = 47 * hash + this.event;
        hash = 47 * hash + this.field;
        return hash;
    }
}