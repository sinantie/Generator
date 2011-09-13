package induction;

import fig.basic.LogInfo;
import fig.exec.Execution;
import fig.record.Record;
import induction.Options.InitType;
import induction.problem.ModelInterface;
import induction.problem.event3.Event3Model;

/**
 *
 * @author konstas
 */
public class DiscriminativeGeneration implements Runnable
{
    Options opts = new Options();

    public void run()
    {
        ModelInterface model = null;
        switch(opts.modelType)
        {
            case train:
            case generate:
            default:
                model = new Event3Model(opts);
                break;
        }
        model.init(InitType.staged, opts.initRandom, "");        
        model.readExamples();
        
//model.init(InitType.staged, opts.initRandom, "");
        Record.begin("stats");
        LogInfo.track("Stats", true);
        model.logStats();
        LogInfo.end_track();
        Record.end();
                
        opts.outputIterFreq = opts.stage1.numIters;
        model.generate("stage1", opts.stage1);
    }

    public static void main(String[] args)
    {
        DiscriminativeGeneration x = new DiscriminativeGeneration();        
        Execution.run(args, x, x.opts);
    }
}
