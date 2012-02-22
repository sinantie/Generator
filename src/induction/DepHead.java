package induction;

/**
 *
 * @author konstas
 */
public class DepHead
{
    private final int head, pos;
    private final BigDouble weight;    
    
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

    public DepHead(DepHead headIn)
    {
        this(headIn.head, headIn.pos, new BigDouble(headIn.weight));
    }
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
    
    @Override
    public String toString()
    {        
        return String.format("%s, %s, (%s)", head, pos, weight);
    }
    
    
}
