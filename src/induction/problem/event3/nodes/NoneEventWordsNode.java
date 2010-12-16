package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class NoneEventWordsNode extends Node{
    int  c;
    public NoneEventWordsNode(int i, int j, int c) {
        super(i);
        this.j = j;
        this.c = c;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, c);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof NoneEventWordsNode;
        if(!(obj instanceof NoneEventWordsNode))
            return false;
        NoneEventWordsNode node = (NoneEventWordsNode) obj;
        return i == node.i && j == node.j && c == node.c;
    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.i;
        hash = 23 * hash + this.j;
        hash = 23 * hash + this.c;
        return hash;
    }
}