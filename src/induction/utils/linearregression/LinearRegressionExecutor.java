package induction.utils.linearregression;

import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.problem.event3.Event3Model;
import induction.utils.linearregression.LinearRegressionOptions.Mode;



/**
 *
 * @author konstas
 */
public class LinearRegressionExecutor implements Runnable
{
    LinearRegressionOptions opts = new LinearRegressionOptions();
    Event3Model model;
    
    @Override
    public void run()
    {
        LinearRegressionWekaWrapper lrw = new LinearRegressionWekaWrapper(opts);
        if(opts.mode == Mode.train)
        {
            LogInfo.logs("Training linear regression model...");
            lrw.train(opts.outputFeaturesFile, opts.saveModel);
        }
        else if(opts.mode == Mode.test)
        {            
           LogInfo.logs("Testing linear regression model...");
           lrw.predict(opts.inputFile, opts.examplesInSingleFile);
        }
    }
    
    public static void main(String[] args)
    {
        LinearRegressionExecutor x = new LinearRegressionExecutor();
        Execution.run(args, x, x.opts);
    }
}
