package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class CatFieldValueNode extends Node{
    int c, event, field;
    public CatFieldValueNode(int i, int c, int event, int field) {
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
//        assert obj instanceof CatFieldValueNode;
        if (!(obj instanceof CatFieldValueNode))
            return false;
        CatFieldValueNode node = (CatFieldValueNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.i;
        hash = 29 * hash + this.c;
        hash = 29 * hash + this.event;
        hash = 29 * hash + this.field;
        return hash;
    }
}
