package induction.problem;

import java.util.Set;

/**
 *
 * @author konstas
 */
public interface Vec
{
    public double getCount(int i);
    public Vec addCount(double x);
    public Vec addCount(int i, double x);  
    public Vec addCount(Vec vec, double x);
    public Vec addCount(Vec vec);
    public Set<Pair<Integer>> getProbsSorted();
    public Vec set(double x);
    public void set(int pos, double x);
    public void setSortedIndices();
    public Pair getAtRank(int rank);
}
