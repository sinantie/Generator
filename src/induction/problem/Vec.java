package induction.problem;

import induction.problem.event3.Constants;
import java.util.Random;
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
    public Vec addCount(double[] phi, double x);
    public Vec addCountKeepNonNegative(int i, double x);
    public Vec addProb(int i, double x);
    public Vec addProb(double[] phi, double x);
    public Vec addProbKeepNonNegative(int i, double x);
    public void copyDataFrom(Vec v);
    public Vec div(double x);
    public Vec expDigamma();
    public Pair getAtRank(int rank);
    public double[] getCounts(); // for serialisation use only
    public String[] getLabels(); // for serialisation use only
    public double getOldSum();
    public double[] getProbs();
    public Set<Pair<Integer>> getProbsSorted();
    public double getProb(int i);
    public double getSum(); // for serialisation use only
    public Vec normalise();
    public Vec normalizeIfTooBig();
    public int sample(Random random);
    public void saveSum();
    public Vec set(double x);
    public void set(int pos, double x);
    public Vec set(final Random random, final double noise, final Constants.TypeAdd type);    
    public void setSortedIndices();
    public int size();
}
