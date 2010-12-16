package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
 public class PCEventsNode extends Node{
    int t0, pc;
    public PCEventsNode(int i, int j, int t0, int pc){
        super(i, j);
        this.t0 = t0;
        this.pc = pc;
    }
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, t0, pc);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof PCEventsNode;
        if (!(obj instanceof PCEventsNode))
            return false;
        PCEventsNode node = (PCEventsNode) obj;
        return i == node.i && j == node.j && t0 == node.t0 && pc == node.pc;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.i;
        hash = 79 * hash + this.j;
        hash = 79 * hash + this.t0;
        hash = 79 * hash + this.pc;
        return hash;
    }
}