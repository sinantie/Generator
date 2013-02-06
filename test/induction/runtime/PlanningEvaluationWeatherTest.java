package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.event3.generative.planning.GenerativePlanningEvent3Model;
import induction.problem.event3.planning.PlanningEvent3Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class PlanningEvaluationWeatherTest
{
    LearnOptions lopts;
    String name;
    PlanningEvent3Model model;

    public PlanningEvaluationWeatherTest() {
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
                "-modelType evalPlanning "
                + "-examplesInSingleFile "
                + "-testInputLists "
//                 + "test/testWeatherGovEvents "
//                + "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2 "
                + "data/weatherGov/weatherGovGenEvalGaborRecordTreebankUnaryRules.gz "
                + "-stagedParamsFile "
                + "results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.params.obj "
                + "-inputFileExt events "
                + "-allowConsecutiveEvents "
                + "-reorderType ignore "
//                + "-allowNoneEvent "
                + "-numAsSymbol ";
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativePlanningEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;        
//        System.out.println("BEFORE\n" +((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));
        lopts = opts.stage1;
        name = "stage1";
//        model.restoreArtificialInitParams();
//        model.saveParams(name, "stage2.params.obj.gz");
//        System.exit(0);
    }

    @After
    public void tearDown() throws Throwable {
    }

    /**
     * Test of run method, of class Induction.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
        System.out.println(in);
//        assertEquals(in, targetOutput);
//        System.out.println("\n\nAFTER\n" +((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));
    }
}