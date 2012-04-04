package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.dmv.generative.GenerativeDMVModel;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class DMVTrainWSJTest
{
    LearnOptions lopts;
    String name;
    GenerativeDMVModel model;
    
    @Before
    public void setUp() 
    {        
         String args = "-modelType dmv "
                    + "-Options.stage1.numIters 60 "
                    + "-numThreads 1 "                    
                    + "-inputLists "
//                    + "../wsj/train/ "
//                    + "wsjLists/unsupervised-train-wsj "
                    + "../wsj/3.0/conll/wsj-10-noP.deps "
//                    + "-inputFileExt mrg "
//                    + "-inputFormat mrg "
                    + "-examplesInSingleFile "
                    + "-inputFormat conll "
                    + "-dontOutputParams "
//                    + "-maxExampleLength 10 "
//                    + "-removePunctuation "
                    + "-connlTagPos 3 "
                    + "-connlHeadPos 6 "
                    + "-useTagsAsWords";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new GenerativeDMVModel(opts);
        model.readExamples();
        model.logStats();
        model.preInit();
        model.init(InitType.uniformz, null, "stage1");
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        name = "stage1";
    }
   
    /**
     * Test of run method, of class Induction.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");        
        System.out.println(model.testInitLearn(name, lopts));
    }
}
