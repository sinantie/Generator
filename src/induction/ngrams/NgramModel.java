package induction.ngrams;

/**
 *
 * @author konstas
 */
public abstract class NgramModel
{
    public abstract double getProb(String[] ngramWords);
 
    /**
     * Converts a 10-base log probability to a normal probability in [0, 1]
     * @param value the log probability
     * @return the probability in [0, 1]
     */
    protected double unLog(float value)
    {
        return Math.pow(10, value);
    }

    protected double unLog(double value)
    {
        return Math.pow(10, value);
    }
    /**
     * Converts a 2-base log probability to a normal probability in [0, 1]
     * @param value the log probability
     * @return the probability in [0, 1]
     */
    private double unLogBase2(double value)
    {
        return Math.pow(2, value);
    }
}
