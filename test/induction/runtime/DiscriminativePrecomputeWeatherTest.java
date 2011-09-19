package induction.runtime;

import induction.problem.event3.Event3Model;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
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
public class DiscriminativePrecomputeWeatherTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public DiscriminativePrecomputeWeatherTest() {
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
         String args = "-modelType precompute -testInputLists test/testWeatherGovEvents "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
                    + "-ngramWrapper srilm -allowConsecutiveEvents -reorderType "
                    + "eventType -allowNoneEvent -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 "
//                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 4 ";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new DiscriminativeEvent3Model(opts);
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        model.init(InitType.random, opts.initRandom, "");
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
        String targetOutput = "3 35 3 3 3 3 3 35 3 3 3 3 3 3 3 35 3 3 2 3 2 3 3 3 35 3 3 3 4 3 3 35 3 3 3 4 3 3";
        assertEquals(model.testInitLearn(name, lopts).trim(), targetOutput);
    }
}