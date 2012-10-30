package induction.utils.postprocess;

import fig.exec.Execution;

/**
 *
 * @author sinantie
 */
public class ComputeAveragesExecutor implements Runnable
{

    ComputeAveragesOptions opts = new ComputeAveragesOptions();

    @Override
    public void run()
    {
        ComputeAverages ca = new ComputeAverages(opts);
        ca.execute();
    }

    public static void main(String[] args)
    {
        ComputeAveragesExecutor x = new ComputeAveragesExecutor();
        Execution.run(args, x, x.opts);
    }
}
