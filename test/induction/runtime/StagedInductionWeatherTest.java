/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.event3.generative.GenerativeEvent3Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author konstas
 */
public class StagedInductionWeatherTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public StagedInductionWeatherTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() 
    {
        String args = 
                   "-modelType event3 "
                 + "-Options.stage1.numIters 2 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
//                 + "test/testWeatherGovEvents "
                 + "data/weatherGov/weatherGovTrainGabor.gz "
                 + "-stagedParamsFile "
//                    + "results/output/weatherGov/alignments/"
//                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
                    + "/home/sinantie/EDI/Generator/results/output/weatherGov/alignments/model_3_gabor_split/stage1.params.obj.gz "
                 + "-inputFileExt events "
                 + "-ngramWrapper kylm "
                 + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
//                 + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 "
                 + "-disallowConsecutiveRepeatFields "
//                 + "-indepWords 0,-1 "
                 + "-dontCrossPunctuation "
                 + "-Options.stage1.smoothing 0.01 "
                 + "-initNoise 0 "                 
                 + "-maxExamples 5 "
                 + "-allowNoneEvent "                
                 + "-conditionNoneEvent "
                 + "-useStopNode ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.logStats();
        model.readExamples();
        model.logStats();
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
        String targetOutput = "5 5 5 0 0 0 0 0 0 3 3 3 2 2 2 2 2";
        assertEquals(model.testStagedLearn(name, lopts).trim(), targetOutput);
    }
}