package induction.problem;

import fig.prob.DirichletUtils;
import induction.Utils;
import induction.problem.event3.Constants;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * Augment a regular array with a sum and old sum
 * Used for storing probability vectors allowing for an O(1) update by storing normalization constant
 * The whole motivation for storing the sum is that we can update counts and get probability vectors for online EM
 * The only reason for storing the oldSum is that for the aggressive online EM update, we need to scale a series
 * of updates by the same sum, but updating it affects the sum
 **/
public class ProbVec implements Serializable
{
    static final long serialVersionUID = -8235238691651895455L;
    private double[] counts;
    private int[] sortedIndices;
    private double sum, oldSum;
    private String[] labels;

    public ProbVec(double[] counts, double sum, double oldSum)
    {
        this.counts = counts;
        this.sum = sum;
        this.oldSum = oldSum;
    }

    private ProbVec(double[] counts, double sum, double oldSum, String[] labels)
    {
        this(counts, sum, oldSum);
        this.labels = labels;
    }

    public double[] getCounts() // for serialisation use only
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

    public void setData(double[] counts, double sum, double oldSum, String[] labels) // for serialisation use only
    {
        this.counts = counts;
        this.sum = sum;
        this.oldSum = oldSum;
        this.labels = labels;
    }

    public double getProb(int i)
    {
        return counts[i] / sum;
    }

    public double getCount(int i)
    {
        return counts[i];
    }

    public ProbVec addCount(double x)
    {
        Utils.add(counts, x);
        sum += counts.length * x;
        return this;
    }

    public ProbVec addCount(int i, double x)
    {
        counts[i] += x;
        sum += x;
        return this;
    }

    public ProbVec addCountKeepNonNegative(int i, double x)
    {
        // If adding would make it < 0, just set it to 0
        // This is mostly for numerical precision errors (it shouldn't go too much below 0)
        if(counts[i] + x < 0)
        {
            sum -= counts[i];
            counts[i] = 0;
        }
        else
        {
            counts[i] += x;
            sum += x;
        }
        return this;
    }

    // Add a feature vector phi (usually, phi is indicator at some i
    public ProbVec addCount(double[] phi, double x)
    {
        Utils.add(counts, x, phi);
        sum += x;
        return this;
    }

    public ProbVec addCount(ProbVec vec, double x)
    {
        Utils.add(counts, x, vec.counts);
        sum += x * vec.sum;
        return this;
    }

    // For the special aggressive online EM update
    public ProbVec addProb(int i, double x)
    {
        return addCount(i, x * oldSum);
    }

    public ProbVec addProbKeepNonNegative(int i, double x)
    {
        return addCountKeepNonNegative(i, x * oldSum);
    }

    public ProbVec addProb(double[] phi, double x)
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
        final double x = (sum-counts[i]) * p / (1-p) - counts[i];
        counts[i] += x;
        sum += x;
    }

    public double[] getProbs()
    {
        // in the discriminative model we save weights not probabilities, so no need to normalise
        return sum == 0 ? counts : Utils.div(Arrays.copyOf(counts, counts.length), sum);
    }

    public Set<Pair<Integer>> getProbsSorted()
    {
        int length = counts.length;
        double[] probVec = getProbs();

        TreeSet<Pair<Integer>> pairs = new TreeSet<Pair<Integer>>();
        // sort automatically by probability (pair.value)
        for(int i = 0; i < length; i++)
        {
            pairs.add(new Pair(probVec[i], new Integer(i)));
        }
        return pairs.descendingSet();
    }

    public void setSortedIndices()
    {
        sortedIndices = new int[counts.length];
        int i = 0;
        for(Pair p: getProbsSorted())
        {
            sortedIndices[i++] = (Integer)p.label;
        }
    }

    public int getMax()
    {
        int index = -1;
        double maxCount = -1.0d;
        for(int i = 0; i < counts.length; i++)
        {
            if(counts[i] > maxCount)
            {
                index = i;
                maxCount = counts[i];
            }
        }
        return index;
    }

    public Pair getAtRank(int rank)
    {
        return new Pair(counts[sortedIndices[rank]], sortedIndices[rank]);
    }
    /**
     * Usage: call saveSum, normalize, getOldSum
     * Useful for printing out posteriors - get an idea of posterior mass on these rules
     **/
    public double getOldSum()
    {
        return oldSum;
    }

    public ProbVec expDigamma()
    {
        if(sum > 0)
        {
            DirichletUtils.fastExpExpectedLogMut(counts);
            computeSum();
        }
        return this;
    }

    public ProbVec normalise()
    {
        if (sum == 0)
        {
            Utils.set(counts, 1.0/counts.length);
        }
        else
        {
            Utils.div(counts, sum);
        }
        sum = 1;
        return this;
    }

    public ProbVec normalizeIfTooBig()
    {
        if (sum > 1e20)
        {
            normalise();
        }
        return this;
    }

    public ProbVec set(Random random, double noise, Constants.TypeAdd type)
    {

        Utils.set(counts, random, noise, type);
        return computeSum();
    }

    public ProbVec set(double x)
    {
        Utils.set(counts, x);
        return computeSum();
    }
    public void set(int pos, double x)
    {
        assert pos < counts.length;
        counts[pos] = x;
    }
    
    public ProbVec div(double x)
    {
        Utils.div(counts, x);
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
            accum += counts[i];
        }
        return i;
    }

    public ProbVec computeSum()
    {
        sum = Utils.sum(counts);
        return this;
    }

    public static ProbVec zeros(int n)
    {
        return new ProbVec(new double[n], 0, 0);
    }

    public static ProbVec zeros(int n, String[] labels)
    {
        return new ProbVec(new double[n], 0, 0, labels);
    }

    public static ProbVec[] zeros2(int n1, int n2)
    {
        ProbVec[] result = new ProbVec[n1];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros(n2);
        }
        return result;
    }

    public static ProbVec[] zeros2(int n1, int n2, String[][] labels)
    {
        ProbVec[] result = new ProbVec[n1];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros(n2, labels[i]);
        }
        return result;
    }

    public static ProbVec[][] zeros3(int n1, int n2, int n3)
    {
        ProbVec[][] result = new ProbVec[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros2(n2, n3);
        }
        return result;
    }

    public static void main(String[] args)
    {
        double[] a1 = new double[3]; a1[0] = 0; a1[1] = 1; a1[2] = 2;
        ProbVec v1 = new ProbVec(a1, 3, 3);
        String[] labels = {"test1", "test2", "test3"};
        v1.labels = labels;
        //double[] a2 = v1.getProbsSorted(labels);
        System.out.println("");
    }
}
