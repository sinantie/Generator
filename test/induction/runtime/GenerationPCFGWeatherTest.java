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
public class GenerationPCFGWeatherTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public GenerationPCFGWeatherTest() {
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
                + "results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_treebank_20iter_posTagged/stage1.params.obj.gz "
//                + "model_3_gabor_record_pcfg_treebank_alignments_no_windChill_sleet_externalTreebank/stage1.extTreebank.params.obj.gz "
//                + "model_3_gabor_record_pcfg_treebank_noNone_alignments_markov1_externalTreebank/stage1.extTreebank.params.obj.gz "
//                 + "-fixRecordSelection "
                + "-outputPcfgTrees "
                + "-wordsPerRootRule "
                + "-Options.stage1.cfgThreshold 0.04 "
//                + "-oracleReranker "
                + "-outputFullPred "
                + "-inputFileExt events "
                + "-disallowConsecutiveRepeatFields "
                + "-maxPhraseLength 10 "
                + "-maxDocLength 90 "
                + "-docLengthBinSize 5 "
                + "-kBest 60 "
                + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
                + "-ngramWrapper kylm "
                + "-allowConsecutiveEvents "
                + "-reorderType ignore "
//                + "-allowNoneEvent "
                + "-binariseAtWordLevel "
                + "-ngramSize 3 "
//                + "-lengthPredictionMode gold "
//                + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                + "-lengthPredictionMode gold "
                + "-lengthPredictionModelFile gaborLists/genEvalGaborScaledPredLength_c6_g1.1.svr_round.length "
                + "-lengthPredictionFeatureType values "
                + "-lengthPredictionStartIndex 4 "
//                + "-excludedEventTypes windChill sleetChance "
                + "-numAsSymbol "
                + "-useDependencies "
                + "-interpolationFactor 0.3 "
                + "-posAtSurfaceLevel "
                + "-dmvModelParamsFile results/output/weatherGov/dmv/train/"
                + "weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
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
        String targetOutput = "<doc docid=\"data/weather-data-full/data/virginia/"
                            + "glen_allen/2009-02-08-1.text\" genre=\"nw\"><p>"
                            + "<seg id=\"1\" bleu=\"0.8039183415894011\" "
                            + "bleu_modified=\"0.8039183415894011\" "
                            + "meteor=\"0.9390967447612161\" ter=\"0.058823529411764705\">"
                            + "mostly cloudy , with a low around 56 . south "
                            + "wind between 3 and 6 mph .</seg></p></doc>";
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
        System.out.println(in);
//        assertEquals(in, targetOutput);
//        System.out.println("\n\nAFTER\n" +((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));
    }
}