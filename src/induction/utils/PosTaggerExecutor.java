package induction.utils;

import fig.exec.Execution;

/**
 *
 * @author sinantie
 */
public class PosTaggerExecutor implements Runnable
{

    PosTaggerOptions opts = new PosTaggerOptions();

    @Override
    public void run()
    {
        PosTagger posTagger = new PosTagger(opts);
        posTagger.execute();
    }

    public static void main(String[] args)
    {
        PosTaggerExecutor x = new PosTaggerExecutor();
        Execution.run(args, x, x.opts);
    }
}
