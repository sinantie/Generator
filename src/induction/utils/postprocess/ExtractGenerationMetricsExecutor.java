package induction.utils.postprocess;

import fig.exec.Execution;

/**
 *
 * @author sinantie
 */
public class ExtractGenerationMetricsExecutor implements Runnable
{

    ExtractGenerationMetricsOptions opts = new ExtractGenerationMetricsOptions();

    @Override
    public void run()
    {
        ExtractGenerationMetrics egm = new ExtractGenerationMetrics(opts);
        egm.execute();
    }

    public static void main(String[] args)
    {
        ExtractGenerationMetricsExecutor x = new ExtractGenerationMetricsExecutor();
        Execution.run(args, x, x.opts);
    }
}
