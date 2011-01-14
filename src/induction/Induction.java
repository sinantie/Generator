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
public class Induction implements Runnable
{
    Options opts = new Options();

    public void run()
    {
        ModelInterface model = null;
        switch(opts.modelType)
        {
            case event3 : model = new Event3Model(opts); break;
            default : LogInfo.fail("Unknown model type: " + opts.modelType);
        }
        /*in staged initialisation we need to read parameters before examples*/
        if(opts.initType == InitType.staged)
        {
            model.init(InitType.staged, opts.initRandom, "");
        }
        model.readExamples();
        Record.begin("stats");
        LogInfo.track("Stats", true);
        model.logStats();
        LogInfo.end_track();
        Record.end();
          
        if(opts.stage1.numIters > 0)
        {
            opts.outputIterFreq = opts.stage1.numIters;
            if(opts.initType != InitType.staged)
                model.init(opts.initType, opts.initRandom, "");
            model.learn("stage1", opts.stage1);
        }       
    }

    public static void main(String[] args)
    {
        Induction x = new Induction();        
        Execution.run(args, x, x.opts);
    }
}
