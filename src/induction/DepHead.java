package induction;

/**
 *
 * @author konstas
 */
public class DepHead
{
    private int head, pos;
    private BigDouble weight;

    public int getHead()
    {
        return head;
    }

    public int getPos()
    {
        return pos;
    }

    public BigDouble getWeight()
    {
        return weight;
    }
    
    public DepHead(int head, int pos, double weight)
    {
        this(head, pos, BigDouble.fromDouble(weight));
    }
    
    public DepHead(int head, int headPos, BigDouble headWeight)
    {
        this.head = head;
        this.pos = headPos;
        this.weight = headWeight;
    }
}
