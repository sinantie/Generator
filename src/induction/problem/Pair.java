package induction.problem;

import induction.problem.event3.generative.Constants;

/**
 *
 * @author konstas
 */
public class Pair<E> implements Comparable
{
    public double value;
    public E label;
    private Constants.Compare choice; // default is value

    public Pair(double value, E label)
    {
        this.value = value;
        this.label = label;
        this.choice = Constants.Compare.VALUE;
    }

    public Pair(double value, E label, Constants.Compare choice)
    {
        this.value = value;
        this.label = label;
        this.choice = choice;
    }

    public int compareTo(Object o)
    {
        assert(o instanceof Pair);
        Pair p = (Pair)o;
        if(choice == Constants.Compare.LABEL)
        {            
            return ((String)label).compareTo((String)p.label);
        }
        else
        {
//            return value > p.value ? 1 : (value < p.value) ? -1 : 0;
            return value >= p.value ? 1 : -1;
        }
    }

}
