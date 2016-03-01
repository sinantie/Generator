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
public class GenerationBlocksWorldTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public GenerationBlocksWorldTest() {
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
         String args = "-modelType generate "
                    + "-testInputLists datasets/A0/Dev.data "
                    + "-inputFileExt events "
                    + "-examplesInSingleFile "
                    + "-outputFullPred "
                    + "-stagedParamsFile "
                    + "results/A0/alignments/"
                    + "model_3/1.exec/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields "
                    + "-kBest 120 "
                    + "-ngramModelFile results/A0/lang_file.arpa "
//                    + "-secondaryNgramModelFile atisLM/atis-all-train-3-gram-tagged.CDnumbers.tags_only.model.arpa "
                    + "-ngramWrapper kylm "
                    + "-allowConsecutiveEvents "
//                    + "-reorderType eventTypeAndField "
                    + "-maxPhraseLength 5 "
                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-lengthPredictionMode gold "
//                    + "-lengthPredictionMode linearRegression "
//                    + "-lengthPredictionModelFile ../datasets/atis/train/lengthPrediction.counts.linear-reg.model "
//                    + "-lengthPredictionFeatureType counts "
//                    + "-lengthPredictionStartIndex 2 "
//                    + "-lengthCompensation 0 "
//                    + "-useDependencies "
                    + "-interpolationFactor 1 "
//                    + "-posAtSurfaceLevel "
                    + "-allowNoneEvent "
                    + "-useStopNode ";                    
//                    + "-forceOutputOrder";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);        
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();        
        model.logStats();        
//        opts.outputIterFreq = opts.stage1.numIters;
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
//        String targetOutput = "<doc docid=\"Example_1\" genre=\"nw\"><p>"
//                + "<seg id=\"1\" bleu=\"0.4673945708424301\" bleu_modified=\"0.4673945708424301\" "
//                + "meteor=\"0.6853734613927462\" ter=\"0.42857142857142855\">"
//                + "show me flights from dallas to baltimore</seg></p></doc>";
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
//        assertEquals(in, targetOutput);
    }
}