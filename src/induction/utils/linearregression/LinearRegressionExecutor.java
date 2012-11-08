package induction.utils.linearregression;

import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.utils.linearregression.LinearRegressionOptions.Mode;



/**
 *
 * @author konstas
 */
public class LinearRegressionExecutor implements Runnable
{
    LinearRegressionOptions opts = new LinearRegressionOptions();

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
            try
            {
                LogInfo.logs(lrw.predict(opts.inputFile));        
            }
            catch(Exception e)
            {
                LogInfo.error(e);
            }
        }
    }
    
    public static void main(String[] args)
    {
        LinearRegressionExecutor x = new LinearRegressionExecutor();
        Execution.run(args, x, x.opts);
    }
}
