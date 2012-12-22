package induction.utils;

import fig.exec.Execution;

/**
 *
 * @author konstas
 */
public class CrossFoldPartitionDatasetExecutor implements Runnable
{
    CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
    
    @Override
    public void run()
    {
        CrossFoldPartitionDataset cpd = new CrossFoldPartitionDataset(opts);
        cpd.execute();
    }
    
    public static void main(String[] args)
    {
        CrossFoldPartitionDatasetExecutor x = new CrossFoldPartitionDatasetExecutor();
        Execution.run(args, x, x.opts);
    }
}
