package induction.problem;

import fig.basic.LogInfo;
import fig.prob.DirichletUtils;
import induction.Utils;
import induction.problem.event3.Constants;
import induction.problem.event3.Constants.TypeAdd;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.OpenMapRealVector;
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
    
    public double[] getCounts()
    {
        return counts.getData();
    }
    
    public double getSum() // for serialisation use only
    {
        return sum;
    }

    public String[] getLabels() // for serialisation use only
    {
        return labels;
    }
    
    @Override
    public void copyDataFrom(Vec v)
    {
        assert v instanceof SparseVec;
        SparseVec sv = (SparseVec)v;
        this.counts = sv.counts;
        this.sum = sv.sum;
        this.oldSum = sv.oldSum;
        this.labels = sv.labels;
    }
        
//    public void setData(SparseRealVector counts, double sum, double oldSum, String[] labels) // for serialisation use only
//    {
//        this.counts = counts;
//        this.sum = sum;
//        this.oldSum = oldSum;
//        this.labels = labels;
//    }
    
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
        sum += size() * x;
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
        counts.add(new ArrayRealVector(phi).mapMultiplyToSelf(x));
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
        counts.add(sv.counts.mapMultiply(x));
        sum += x * sv.sum;
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
        return sum == 0 ? counts.getData() : Utils.div(Arrays.copyOf(counts.getData(), size()), sum);
    }
    
    @Override
    public Set<Pair<Integer>> getProbsSorted()
    {
        
        TreeSet<Pair<Integer>> pairs = new TreeSet<Pair<Integer>>();
        // sort automatically by probability (pair.value)
        for(int i = 0; i < size(); i++)
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
        for(int i = 0; i < size(); i++)
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
    public void setSortedIndices()
    {
        sortedIndices = new int[size()];
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
            counts.set(1.0/size());
        }
        else
        {
            counts.mapDivideToSelf(sum);
        }
        sum = 1;
        return this;
    }
    
    public Vec normalizeIfTooBig()
    {
        if (sum > 1e20)
        {
            normalise();
        }
        return this;
    }
    
    public Vec set(final Random random, final double noise, final Constants.TypeAdd type)
    {
        try {
            counts.mapToSelf(new UnivariateRealFunction() {
                @Override
                public double value(double d) throws FunctionEvaluationException
                {
                    return (type == TypeAdd.RANDOM) ? Math.pow(1 + random.nextDouble(), noise) :
                                             random.nextDouble() * noise;
                }
            });
        }
        catch (FunctionEvaluationException ex) {
            LogInfo.error(ex);
        }
        return computeSum();
    }
    
    @Override
    public Vec set(double x)
    {
        counts.set(x);
        return computeSum();
    }

    @Override
    public void set(int pos, double x)
    {
        assert pos < size();
        counts.setEntry(pos, x);
        computeSum();
    }
    
    public Vec div(double x)
    {
        counts.mapDivideToSelf(x);
        return computeSum();
    }

    public int sample(Random random)
    {
        final double target = random.nextDouble() * sum;
        int i = -1;
        double accum = 0.0;
        while (accum < target)
        {
            i += 1;
            accum += counts.getEntry(i);
        }
        return i;
    }

    public Vec computeSum()
    {
        sum = counts.getL1Norm();        
        return this;
    }

    public int size()
    {
        return counts.getDimension();
    }
    
    public static Vec zeros(int n)
    {
        return new SparseVec(new OpenMapRealVector(n), 0, 0);
    }   

    public static Vec[] zeros2(int n1, int n2)
    {
        Vec[] result = new SparseVec[n1];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros(n2);
        }
        return result;
    }    

    public static Vec[][] zeros3(int n1, int n2, int n3)
    {
        Vec[][] result = new SparseVec[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros2(n2, n3);
        }
        return result;
    }
}
