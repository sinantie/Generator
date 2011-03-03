package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class FieldsNode extends Node{
    int c, event, f0, efs;
    public FieldsNode(int i, int end, int c, int event, int f0, int efs){
        super(i, end);
        this.c = c;
        this.event = event;
        this.f0 = f0;
        this.efs = efs;
    }
    public int getEvent()
    {
        return event;
    }
    public int getField()
    {
        return f0;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c, event, f0, efs);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof FieldsNode : obj.toString();
        if (!(obj instanceof FieldsNode))
            return false;
        FieldsNode node = (FieldsNode) obj;
        return i == node.i && j == node.j && c == node.c && f0 == node.f0 &&
                event == node.event && efs == node.efs;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.i;
        hash = 83 * hash + this.j;
        hash = 83 * hash + this.c;
        hash = 83 * hash + this.event;
        hash = 83 * hash + this.f0;
        hash = 83 * hash + this.efs;
        return hash;
    }
}