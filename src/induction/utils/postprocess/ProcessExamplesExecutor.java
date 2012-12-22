package induction.utils.postprocess;

import fig.exec.Execution;

/**
 *
 * @author sinantie
 */
public class ProcessExamplesExecutor implements Runnable
{

    ProcessExamplesOptions opts = new ProcessExamplesOptions();

    @Override
    public void run()
    {
        ProcessExamples ca = new ProcessExamples(opts);
        ca.execute();
    }

    public static void main(String[] args)
    {
        ProcessExamplesExecutor x = new ProcessExamplesExecutor();
        Execution.run(args, x, x.opts);
    }
}
