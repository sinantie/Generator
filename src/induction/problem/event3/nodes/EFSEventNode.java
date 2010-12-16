package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class EFSEventNode extends Node{
    int c, event, efs;
    public EFSEventNode(int i, int j, int c, int event, int efs) {
        super(i, j);
        this.c = c;
        this.event = event;
        this.efs = efs;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c, event, efs);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof EFSEventNode;
        if(!(obj instanceof EFSEventNode))
            return false;
        EFSEventNode node = (EFSEventNode) obj;
        return i == node.i && j == node.j && c == node.c &&
                event == node.event && efs == node.efs;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.i;
        hash = 97 * hash + this.j;
        hash = 97 * hash + this.c;
        hash = 97 * hash + this.event;
        hash = 97 * hash + this.efs;
        return hash;
    }
}