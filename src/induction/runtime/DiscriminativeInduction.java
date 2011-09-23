package induction.runtime;

import fig.basic.LogInfo;
import fig.exec.Execution;
import fig.record.Record;
import induction.Options;
import induction.Options.InitType;
import induction.problem.ModelInterface;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;

/**
 *
 * @author konstas
 */
public class DiscriminativeInduction implements Runnable
{
    Options opts = new Options();

    public void run()
    {
        ModelInterface model = new DiscriminativeEvent3Model(opts);
        
        model.init(InitType.staged, opts.initRandom, "");        
        model.readExamples();        
        Record.begin("stats");
        LogInfo.track("Stats", true);
        model.logStats();
        LogInfo.end_track();
        Record.end();
                
        opts.outputIterFreq = opts.stage1.numIters;
        if(opts.modelType == Options.ModelType.precompute)
        {
            model.init(InitType.supervised, null, "stage1");
        }
        else if(opts.modelType == Options.ModelType.discriminativeTrain)
        {
            model.init(InitType.random, opts.initRandom, "stage1");
            model.learn("stage1", opts.stage1);
        }
        else if(opts.modelType == Options.ModelType.generate)
        {
            model.init(InitType.staged, null, "stage1");
            model.generate("stage1", opts.stage1);
        }                
    }

    public static void main(String[] args)
    {
        DiscriminativeInduction x = new DiscriminativeInduction();        
        Execution.run(args, x, x.opts);
    }
}
