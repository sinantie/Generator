package induction.problem;

/**
 *
 * @author konstas
 */
public abstract class AInferState
                         <Widget extends AWidget,
                         Example extends AExample<Widget>,
                         Params extends AParams>
{
    protected Example ex;
    protected Params params, counts;
    protected InferSpec ispec;
    protected final boolean softInfer, hardInfer, trueInfer;
    // When an infer state is created, the following quantities should be available
    protected double temperature, logZ, logVZ, elogZ, logCZ, entropy, objective;
    public Widget bestWidget;

    public AInferState(Example ex, Params params, Params counts, InferSpec ispec)
    {
        this.ex = ex;
        this.params = params;
        this.counts = counts;
        this.ispec = ispec;
        this.temperature = ispec.getTemperature();
        this.softInfer = ispec.isSoftInfer();
        this.hardInfer = ispec.isHardInfer();
        this.trueInfer = ispec.isTrueUpdate();
    }

    public double getCount(Vec v, int i)
    {
        return v.getCount(i);
    }
    
    public double get(Vec v, int i)
    {
        if (ispec.isUseVarUpdates())
        {
            return v.getCount(i); // Explicitly use weights which are not normalized
        }
        else if (temperature == 1)
        {
            return v.getProb(i);
        }
        else
        {
            return Math.pow(v.getProb(i), 1.0/temperature);
        }
    }

    public double getLogProb(Vec v, int i)
    {
//        return Math.abs(Math.log(get(v, i)));
        return Math.log(get(v, i));
    }
    
    public double getLogProb(double prob)
    {
//        System.out.print(prob + " ");
//        return Math.abs(Math.log(prob));
        return Math.log(prob);
    }
    
    public int getMax(Vec v)
    {
        return v.getMax();
    }

    public Pair getAtRank(Vec v, int rank)
    {
        return v.getAtRank(rank);
    }
    protected void initialiseValues()
    {
        // q(z^*|x) = p(z^*, x)/p(x) only when temperature = 1
        logCZ = (temperature == 1) ? logVZ - logZ : Double.NaN;
        objective = elogZ + temperature * entropy;
    }

    public ProbStats stats()
    {
        return new ProbStats(ex.N(), logZ, logVZ, logCZ, elogZ, entropy, objective);
    }
    public Vec update(Vec v, int i, double x)
    {
        //dbg("update " + fmt1(v.getProbs) + " " + i + " " + fmt(x))
        if (ispec.isMixParamsCounts())
        {
            return v.addProb(i, x * ispec.getUpdateScale());
        }
        else
        {
            return v.addCount(i, x * ispec.getUpdateScale());
        }
    }

    public Vec update(Vec v, double[] phi, double x)
    {
        //dbg("update " + fmt1(v.getProbs) + " " + i + " " + fmt(x))
        if (ispec.isMixParamsCounts())
        {
            return v.addProb(phi, x * ispec.getUpdateScale());
        }
        else
        {
            return v.addCount(phi, x * ispec.getUpdateScale());
        }
    }

    public Vec updateKeepNonNegative(Vec v, int i, double x)
    {
        //dbg("update " + fmt1(v.getProbs) + " " + i + " " + fmt(x))
        if (ispec.isMixParamsCounts())
        {
            return v.addProbKeepNonNegative(i, x * ispec.getUpdateScale());
        }
        else
        {
            return v.addCountKeepNonNegative(i, x * ispec.getUpdateScale());
        }
    }
    
    public AWidget getBestWidget()
    {
        return bestWidget;
    }

    public abstract void createHypergraph(int textLength);
    public abstract void doInference();
    public abstract int getComplexity();
    public abstract void updateCounts();
}
