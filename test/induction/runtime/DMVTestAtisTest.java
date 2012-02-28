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
public class DMVTestAtisTest
{
    LearnOptions lopts;
    String name;
    GenerativeDMVModel model;
    
    @Before
    public void setUp() 
    {        
         String args = "-modelType dmv "
                    + "-numThreads 2 "                    
                    + "-examplesInSingleFile "                    
                    + "-testInputLists "
//                    + "../wsj/3.0/parsed/mrg/atis/atis3_clean_pos_cut.mrg "
                    + "data/atis/test/atis-test.txt "
                    + "-stagedParamsFile results/output/atis/dmv/train/"
                    + "atis_raw5000_LEX_10/stage1.dmv.params.obj.gz "
//                    + "../wsj/3.0/parsed/mrg/atis/atis3_one.mrg "
                    + "-inputFileExt events "
                    + "-inputFormat raw "
                    + "-dontOutputParams ";
//                    + "-usePosTagger "            
//                    + "-useTagsAsWords";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new GenerativeDMVModel(opts);
        model.init(InitType.staged, null, "stage1");
        model.readExamples();
        model.logStats();
//        model.preInit();        
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
        System.out.println(model.testGenerate(name, lopts));
    }
}
