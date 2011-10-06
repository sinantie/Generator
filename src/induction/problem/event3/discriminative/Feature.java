package induction.problem.event3.discriminative;

import induction.problem.ProbVec;

/**
 *
 * @author konstas
 */
public class Feature
{
    private ProbVec probVec;
    private int index;

    public Feature(ProbVec probVec, int index)
    {
        this.probVec = probVec;
        this.index = index;
    }
    
    public double getValue()
    {
        return probVec.getCount(index);
    }
    
    public void setValue(double value)
    {
        probVec.set(index, value);
    }
    
    public void increment(double value)
    {
        setValue(getValue() + value);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Feature other = (Feature) obj;
        if (this.probVec != other.probVec && (this.probVec == null || !this.probVec.equals(other.probVec)))
        {
            return false;
        }
        if (this.index != other.index)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + (this.probVec != null ? this.probVec.hashCode() : 0);
        hash = 83 * hash + this.index;
        return hash;
    }
    
    
}
