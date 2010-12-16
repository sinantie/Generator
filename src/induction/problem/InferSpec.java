package induction.problem;

/**
 *
 * @author konstas
 */
public class InferSpec
{
    private double temperature;
    private boolean softInfer, hardInfer, hardUpdate, trueUpdate,
                    mixParamsCounts, useVarUpdates;
    private double updateScale;
    public int iter;

    public InferSpec(double temperature, boolean softInfer, boolean hardInfer,
                     boolean hardUpdate, boolean trueUpdate, boolean mixParamsCounts,
                     boolean useVarUpdates, double updateScale, int iter)
    {
        this.temperature = temperature;
        this.softInfer = softInfer;
        this.hardInfer = hardInfer;
        this.hardUpdate = hardUpdate;
        this.trueUpdate = trueUpdate;
        this.mixParamsCounts = mixParamsCounts;
        this.useVarUpdates = useVarUpdates;
        this.updateScale = updateScale;
        this.iter = iter;
    }

    public double getTemperature()
    {
        return temperature;
    }

    public boolean isSoftInfer()
    {
        return softInfer;
    }

    public boolean isHardInfer()
    {
        return hardInfer;
    }

    public boolean isHardUpdate()
    {
        return hardUpdate;
    }

    public boolean isTrueUpdate()
    {
        return trueUpdate;
    }

    public boolean isMixParamsCounts()
    {
        return mixParamsCounts;
    }

    public boolean isUseVarUpdates()
    {
        return useVarUpdates;
    }

    public double getUpdateScale()
    {
        return updateScale;
    }

    public void setUpdateScale(double updateScale)
    {
        this.updateScale = updateScale;
    }

    public int getIter()
    {
        return iter;
    }

}
