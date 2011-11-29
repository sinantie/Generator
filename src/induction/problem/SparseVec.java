package induction.problem;

import fig.prob.DirichletUtils;
import induction.Utils;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.SparseRealVector;

/**
 *
 * @author konstas
 */
public class SparseVec implements Vec
{
    static final long serialVersionUID = -2L;    
    private SparseRealVector counts;
    private int[] sortedIndices;
    private double sum, oldSum;
    private String[] labels;
    
    public SparseVec(SparseRealVector counts, double sum, double oldSum)
    {
        this.counts = counts;
        this.sum = sum;
        this.oldSum = oldSum;
    }
    
    public SparseVec(SparseRealVector counts, double sum, double oldSum, String[] labels)
    {
        this(counts, sum, oldSum);
        this.labels = labels;
    }
    
    public SparseRealVector getCounts()
    {
        return counts;
    }
    
    public double getSum() // for serialisation use only
    {
        return sum;
    }

    public String[] getLabels() // for serialisation use only
    {
        return labels;
    }
    
    public void setData(SparseRealVector counts, double sum, double oldSum, String[] labels) // for serialisation use only
    {
        this.counts = counts;
        this.sum = sum;
        this.oldSum = oldSum;
        this.labels = labels;
    }
    
    public double getProb(int i)
    {
        return counts.getEntry(i) / sum;
    }
    
    @Override
    public double getCount(int i)
    {
        return counts.getEntry(i);
    }

    @Override
    public Vec addCount(double x)
    {
        counts.mapAddToSelf(x);
        sum += counts.getDimension() * x;
        return this;
    }

    public SparseVec addCountKeepNonNegative(int i, double x)
    {
        // If adding would make it < 0, just set it to 0
        // This is mostly for numerical precision errors (it shouldn't go too much below 0)
        double entry = counts.getEntry(i);
        if(entry + x < 0)
        {
            sum -= entry;
            counts.setEntry(i, 0);
        }
        else
        {
            counts.setEntry(i, entry + x);
            sum += x;
        }
        return this;
    }
    
    // Add a feature vector phi (usually, phi is indicator at some i
    public Vec addCount(double[] phi, double x)
    {
        Utils.add(phi, x, phi);
        counts.add(phi);
        sum += x;
        return this;
    }
    
    @Override
    public Vec addCount(int i, double x)
    {
        counts.setEntry(i, counts.getEntry(i));
        sum += x;
        return this;
    }

    @Override
    public Vec addCount(Vec vec, double x)
    {
        SparseVec sv = (SparseVec)vec;
        counts.add(sv.counts.ebeMultiply(new ArrayRealVector(Utils.set(new double[counts.getDimension()], x))));
        return this;
    }

    @Override
    public Vec addCount(Vec vec)
    {
        SparseVec sv = (SparseVec)vec;
        counts.add(sv.counts);
        sum += sv.sum;
        return this;
    }
    
    // For the special aggressive online EM update
    public Vec addProb(int i, double x)
    {
        return addCount(i, x * oldSum);
    }
    
    public Vec addProbKeepNonNegative(int i, double x)
    {
        return addCountKeepNonNegative(i, x * oldSum);
    }
    
    public Vec addProb(double[] phi, double x)
    {
        return addCount(phi, x * oldSum);
    }
    
    public void saveSum()
    {
        oldSum = sum;
    }
    
    public void setCountToObtainProb(int i, double p)
    {
        assert(p < 1);
        final double x = (sum-counts.getEntry(i)) * p / (1-p) - counts.getEntry(i);
        counts.setEntry(i, counts.getEntry(i) + x);
        sum += x;
    }
    
    public double[] getProbs()
    {
        // in the discriminative model we save weights not probabilities, so no need to normalise
        return sum == 0 ? counts.getData() : Utils.div(Arrays.copyOf(counts.getData(), counts.getDimension()), sum);
    }
    
    @Override
    public Set<Pair<Integer>> getProbsSorted()
    {
        
        TreeSet<Pair<Integer>> pairs = new TreeSet<Pair<Integer>>();
        // sort automatically by probability (pair.value)
        for(int i = 0; i < counts.getDimension(); i++)
        {
            // in the discriminative model we save weights not probabilities, so no need to normalise
            pairs.add(new Pair(sum == 0 ? counts.getEntry(i) : counts.getEntry(i) / sum, new Integer(i)));
        }
        return pairs.descendingSet();
    }

    public int getMax()
    {
        int index = -1;
        double maxCount = -1.0d;
        for(int i = 0; i < counts.getDimension(); i++)
        {
            double entry = counts.getEntry(i);
            if(entry > maxCount)
            {
                index = i;
                maxCount = entry;
            }
        }
        return index;
    }
    
    @Override
    public Vec set(double x)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set(int pos, double x)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSortedIndices()
    {
        sortedIndices = new int[counts.getDimension()];
        int i = 0;
        for(Pair p: getProbsSorted())
        {
            sortedIndices[i++] = (Integer)p.label;
        }
    }

    @Override
    public Pair getAtRank(int rank)
    {
        return new Pair(counts.getEntry(sortedIndices[rank]), sortedIndices[rank]);
    }
    
    public double getOldSum()
    {
        return oldSum;
    }
    
    public Vec expDigamma()
    {
        if(sum > 0)
        {
            DirichletUtils.fastExpExpectedLogMut(counts.getData());
            computeSum();
        }
        return this;
    }
    
    public Vec normalise()
    {
        if (sum == 0)
        {
            Utils.set(counts, 1.0/counts.getDimension());
        }
        else
        {
            Utils.div(counts, sum);
        }
        sum = 1;
        return this;
    }
}
