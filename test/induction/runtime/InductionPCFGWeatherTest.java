package induction.runtime;

import induction.problem.event3.params.Params;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.AParams.ParamsType;
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
public class InductionPCFGWeatherTest
{

    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public InductionPCFGWeatherTest()
    {
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
                "-modelType event3pcfg "
                + "-Options.stage1.numIters 5 "
                + "-examplesInSingleFile "
                + "-inputLists "
                //                 + "data/weatherGov/weatherGovTrainGaborRecordTreebankUnaryRules.gz "
                + "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules.gz "
                + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesGenDevRightBinarizeUnaryRules "
                + "-fixRecordSelection "
                + "-wordsPerRootRule "
                + "-inputFileExt events "
                + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 "
                + "-disallowConsecutiveRepeatFields "
//                 + "-indepWords 0,-1 "
                + "-dontCrossPunctuation "
                + "-Options.stage1.smoothing 0.1 "
                + "-allowNoneEvent "
//                 + "-maxExamples 5 "
                + "-conditionNoneEvent ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[]
                {
                    opts
                }); // parse input params
        model = new GenerativeEvent3Model(opts);
        model.readExamples();
        model.logStats();
        lopts = opts.stage1;
        opts.outputIterFreq = opts.stage1.numIters;
//        model.init(InitType.random, opts.initRandom, "");
        model.init(InitType.artificial, opts.initRandom, ""); // fixed record selection
//        System.out.println("BEFORE\n" +((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));        
        name = "stage1";
    }

    @After
    public void tearDown() throws Throwable
    {
    }

    /**
     * Test of run method, of class Induction.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");
        String targetOutput = "3 35 3 3 3 3 3 35 3 3 3 3 3 3 3 35 3 3 2 3 2 3 3 3 35 3 3 3 4 3 3 35 3 3 3 4 3 3";
        System.out.println(model.testInitLearn(name, lopts).trim());
//        model.learn(name, lopts);
//        assertEquals(model.testInitLearn(name, lopts).trim(), targetOutput);
        System.out.println("\n\nAFTER\n" +((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));
    }
}