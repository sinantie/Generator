package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.dmv.generative.GenerativeDMVModel;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class DMVTrainAtisTest
{
    LearnOptions lopts;
    String name;
    GenerativeDMVModel model;
    
    @Before
    public void setUp() 
    {        
         String args = "-modelType discriminativeTrain -inputLists test/trainAtisExamples "
//         String args = "-modelType discriminativeTrain -inputLists data/atis/train/atis5000.sents.full "
                    + "-Options.stage1.numIters 15 -numThreads 1 "                    
                    + "-inputPaths "
                    + "../wsj/3.0/parsed/mrg/atis/atis3_clean_pos_cut.mrg "
//                    + "../wsj/3.0/parsed/mrg/atis/atis3_one.mrg "
                    + "-inputFileExt mrg "
                    + "-inputFormat mrg "
                    + "-dontOutputParams "            
                    + "-useTagsAsWords";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new GenerativeDMVModel(opts);
        model.readExamples();
        model.logStats();
        model.preInit();
        model.init(InitType.bait, null, "stage1");
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
        System.out.println(model.testGenerativeLearn(name, lopts));
    }
}
