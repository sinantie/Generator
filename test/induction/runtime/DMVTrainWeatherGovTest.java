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
public class DMVTrainWeatherGovTest
{
    LearnOptions lopts;
    String name;
    GenerativeDMVModel model;
    
    @Before
    public void setUp() 
    {        
         String args = "-modelType dmv "
                    + "-Options.stage1.numIters 30 "
                    + "-numThreads 2 "
                    + "-examplesInSingleFile "
                    + "-inputLists "
                    + "data/weatherGov/induction/weatherGovTrainGaborPermutations_eventTypes_delimited "
//                    + "gaborLists/trainListPathsGabor "
                    + "-inputFileExt events "
                    + "-inputFormat raw "
                    + "-dontOutputParams "
//                    + "-useTagsAsWords "
//                    + "-posAtSurfaceLevel "
                    + "-initSmoothing 0.01 "
                    + "-initNoise 1e-3";                    
//                    + "-usePosTagger "            

         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new GenerativeDMVModel(opts);
        model.readExamples();
        model.logStats();
        model.preInit();
        model.init(InitType.uniformz, opts.initRandom, "stage1");
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
