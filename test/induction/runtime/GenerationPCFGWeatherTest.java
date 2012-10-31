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
                + "-inputLists "
//                 + "test/testWeatherGovEvents "
                + "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules "
                + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRules "
                + "-stagedParamsFile "
                + "results/output/weatherGov/alignments/pcfg/"
                + "model_3_gabor_record_pcfg_treebank_unaryRules_30iter/stage1.params.obj.gz "
//                 + "-fixRecordSelection "
                + "-inputFileExt events "
                + "-disallowConsecutiveRepeatFields "
                + "-maxPhraseLength 10 "
                + "-kBest 15 "
                + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
                + "-ngramWrapper srilm "
                + "-allowConsecutiveEvents "
                + "-reorderType ignore "
                + "-allowNoneEvent "
                + "-binariseAtWordLevel "
                + "-ngramSize 3 "
//                + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                + "-lengthPredictionFeatureType values "
                + "-lengthPredictionStartIndex 4 "
                + "-numAsSymbol ";
//                + "-useDependencies "
//                + "-interpolationFactor 0.3 "
//                + "-posAtSurfaceLevel "                    
//                + "-dmvModelParamsFile results/output/weatherGov/dmv/train/"
//                + "weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz "
//                + "-excludedEventTypes windChill gust snowChance freezingRainChance sleetChance"; 
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