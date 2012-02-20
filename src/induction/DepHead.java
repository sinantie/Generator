package induction;

/**
 *
 * @author konstas
 */
public class DepHead
{
    int head, pos;
    BigDouble weight;

    public DepHead(int head, int headPos, double headWeight)
    {
        this.head = head;
        this.pos = headPos;
        this.weight = BigDouble.fromDouble(headWeight);
    }                
}
