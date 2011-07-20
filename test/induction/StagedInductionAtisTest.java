/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction;

import fig.exec.Execution;
import induction.Options.InitType;
import induction.problem.event3.Event3Model;
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
public class StagedInductionAtisTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public StagedInductionAtisTest() {
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
         String args = "-modelType event3 -Options.stage1.numIters 1 -testInputLists "
                + "test/testAtisExamples -examplesInSingleFile -inputFileExt events "
                + "-initNoise 0 -initType staged -stagedParamsFile "
                + "results/output/atis/alignments/"
                + "model_3/15_iter_no_null_no_smooth_STOP/stage1.params.obj "
                + "-dontCrossPunctuation -modelUnkWord -disallowConsecutiveRepeatFields";
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new Event3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        name = "stage1";
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Induction.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");
        String targetOutput = "1 0 0 0 0 0 0";
        assertEquals(model.testStagedLearn(name, lopts).trim(), targetOutput);
    }
}