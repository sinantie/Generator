package induction.utils;

import fig.exec.Execution;

/**
 *
 * @author konstas
 */
public class ComputeAmrEstimatesExecutor implements Runnable
{
    ComputeAmrEstimatesOptions opts = new ComputeAmrEstimatesOptions();
    
    @Override
    public void run()
    {
        ComputeAmrEstimates c = new ComputeAmrEstimates(opts);
        c.execute();
    }
    
    public static void main(String[] args)
    {
        ComputeAmrEstimatesExecutor x = new ComputeAmrEstimatesExecutor();
        Execution.run(args, x, x.opts);
    }
}
