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
public class PlanningEvaluationPCFGWeatherTest
{
    LearnOptions lopts;
    String name;
    PlanningEvent3Model model;

    public PlanningEvaluationPCFGWeatherTest() {
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
                "-modelType generatePcfg "
                + "-examplesInSingleFile "
                + "-testInputLists "
//                 + "test/testWeatherGovEvents "
//                + "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2 "
                + "data/weatherGov/weatherGovGenEvalGaborRecordTreebankUnaryRules.gz "
                + "-treebankRules data/weatherGov/treebanks/final/recordTreebankRulesTrainRightBinarizeAlignmentsTreebank "
//                + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeNoSleetWindChillFilteredAligments "
//                + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeNoNoneAlignmentsMarkov1 "
//                + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesGenDevRightBinarizeUnaryRules_test "
                + "-stagedParamsFile "
//                + "results/output/weatherGov/alignments/pcfg/"
//                + "model_3_gabor_record_pcfg_treebank_unaryRules_wordsPerRootRule_30iter/stage1.params.obj.gz "
                + "results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_treebank_20iter/stage1.params.obj.gz "
//                + "model_3_gabor_record_pcfg_treebank_alignments_no_windChill_sleet_externalTreebank/stage1.extTreebank.params.obj.gz "
//                + "model_3_gabor_record_pcfg_treebank_noNone_alignments_markov1_externalTreebank/stage1.extTreebank.params.obj.gz "
//                 + "-fixRecordSelection "
                + "-wordsPerRootRule "
                + "-Options.stage1.cfgThreshold 0.04 "
                + "-inputFileExt events "
                + "-maxPhraseLength 10 "
                + "-maxDocLength 90 "
                + "-docLengthBinSize 5 "
                + "-kBest 60 "
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