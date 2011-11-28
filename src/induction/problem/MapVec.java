package induction.problem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author konstas
 */
public class MapVec implements Serializable, Vec
{
    static final long serialVersionUID = -1L;
    private Map<Integer, Double> counts;
    private int[] sortedIndices;
    private String[] labels;
    
    public MapVec()
    {
        counts = new HashMap<Integer, Double>();
    }
    
    public MapVec(String[] labels)
    {
        this();
        this.labels = labels;
    }

    public String[] getLabels()
    {
        return labels;
    }
    
    public void setData(Map<Integer, Double> counts, String[] labels)
    {
        this.counts = counts;
        this.labels = labels;
    }
    
    private Set<Integer> getKeys()
    {
        return counts.keySet();
    }
    
    public Map<Integer, Double> getCounts()
    {
        return counts;
    }
    
    @Override
    public double getCount(int i)
    {
        return !counts.containsKey(i) ? Double.NEGATIVE_INFINITY : counts.get(i);
    }
    
    @Override
    public Vec addCount(double x)
    {
        for(Entry<Integer, Double> entry : counts.entrySet())
            entry.setValue(entry.getValue() + x);
        return this;
    }
    
    @Override
    public Vec addCount(int i, double x)
    {
        counts.put(i, x);
        return this;
    }
    
    @Override
    public Vec addCount(Vec vec, double x)
    {
        MapVec mv = (MapVec)vec;
        for(Integer otherKey: mv.getKeys())
            counts.put(otherKey, x + mv.getCount(otherKey));
        return this;
    }
    
    @Override
    public Set<Pair<Integer>> getProbsSorted()
    {
        int length = counts.size();        

        TreeSet<Pair<Integer>> pairs = new TreeSet<Pair<Integer>>();
        // sort automatically by probability (pair.value)
        for(int i = 0; i < length; i++)
        {
            pairs.add(new Pair(counts.get(i), new Integer(i)));
        }
        return pairs.descendingSet();
    }
    
    @Override
    public void setSortedIndices()
    {
        sortedIndices = new int[counts.size()];
        int i = 0;
        for(Pair p: getProbsSorted())
        {
            sortedIndices[i++] = (Integer)p.label;
        }
    }
    
    public Pair getAtRank(int rank)
    {
        return new Pair(counts.get(sortedIndices[rank]), sortedIndices[rank]);
    }
    
    @Override
    public Vec set(double x)
    {
        for(Entry<Integer, Double> entry : counts.entrySet())
            entry.setValue(entry.getValue() + x);
        return this;        
    }
    
    @Override
    public void set(int pos, double x)
    {
//        assert pos < counts.size();
        counts.put(pos, x);
    }
        
}
