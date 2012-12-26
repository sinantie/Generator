package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class WordNode extends Node{
    int c, event, field, posInFieldValue;
    public WordNode(int i, int c, int event, int field, int posInFieldValue) {
        super(i);
        this.c = c;
        this.event = event;
        this.field = field;
        this.posInFieldValue = posInFieldValue;
    }
    
    public WordNode(int i, int c, int event, int field) {
        this(i, c, event, field, -1);
    }

    public int getField()
    {
        return field;
    }

    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, c, event, field, posInFieldValue);
    }
    @Override
    public boolean equals(Object obj) {
//        assert obj instanceof WordNode;
        if (!(obj instanceof WordNode))
            return false;
        WordNode node = (WordNode) obj;
        return i == node.i && c == node.c && event == node.event &&
                field == node.field && posInFieldValue == node.posInFieldValue;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.i;
        hash = 67 * hash + this.c;
        hash = 67 * hash + this.event;
        hash = 67 * hash + this.field;
        hash = 67 * hash + this.posInFieldValue;
        return hash;
    }
}
