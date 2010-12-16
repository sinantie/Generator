package induction.problem;

/**
 *
 * @author konstas
 */
public class ProbStats
{
    private int n; // |x|: just for normalization
           // q(z|x) \propto p(x,z)^{1/temperature}
           // z^* = argmax_z q(z|x)
    private double logZ; // \sum_z p(x, z)^{1/temperature}
    private double logVZ; // p(x, z^*)
    private double logCZ; // q(z^* | x)
    private double elogZ; // \E_q \log p(x, z)
    private double entropy; // H(q)
    private double objective; // objective = \E_q p(z | x) + T H(q)

    public ProbStats(int n, double logZ, double logVZ, double logCZ,
                     double elogZ, double entropy, double objective)
    {
        this.n = n;
        this.logZ = logZ;
        this.elogZ = elogZ;
        this.entropy = entropy;
        this.logCZ = logCZ;
        this.logVZ = logVZ;
        this.objective = objective;
    }

    private double assertValid(double x)
    {
        return x; // Don't be picky
    }

    public void add(ProbStats that)
    {
        n += that.n;

        logZ += assertValid(that.logZ);
        logVZ += assertValid(that.logVZ);
        logCZ += assertValid(that.logCZ);
        elogZ += assertValid(that.elogZ);
        entropy += assertValid(that.entropy);
        objective += assertValid(that.objective);
    }

    public int getN()
    {
        return n;
    }

    public double getAvg_logZ()
    {
        return logZ / (double)n;
    }

    public double getAvg_logVZ()
    {
        return logVZ / (double)n;
    }

    public double getAvg_logCZ()
    {
        return logCZ / (double)n;
    }

    public double getAvg_elogZ()
    {
        return elogZ / (double)n;
    }

    public double getAvg_entropy()
    {
        return entropy / (double)n;
    }

    public double getAvg_objective()
    {
        return objective / (double)n;
    }
}
