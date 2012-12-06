package induction.utils;

import fig.exec.Execution;

/**
 *
 * @author konstas
 */
public class MergeParamsWithExternalTreebankExecutor implements Runnable
{
    MergeParamsWithExternalTreebankOptions opts = new MergeParamsWithExternalTreebankOptions();
    
    @Override
    public void run()
    {
        MergeParamsWithExternalTreebank m = new MergeParamsWithExternalTreebank(opts);
        m.execute();
    }
    
    public static void main(String[] args)
    {
        MergeParamsWithExternalTreebankExecutor x = new MergeParamsWithExternalTreebankExecutor();
        Execution.run(args, x, x.opts);
    }
}
