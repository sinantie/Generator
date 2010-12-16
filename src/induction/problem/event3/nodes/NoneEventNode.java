package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class NoneEventNode extends Node{
    int c;
    public NoneEventNode(int i, int j, int c) {
        super(i, j);
        this.c = c;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof NoneEventNode;
        if(!(obj instanceof NoneEventNode))
            return false;
        NoneEventNode node = (NoneEventNode) obj;
        return i == node.i && j == node.j && c == node.c;
    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.i;
        hash = 43 * hash + this.j;
        hash = 43 * hash + this.c;
        return hash;
    }
}