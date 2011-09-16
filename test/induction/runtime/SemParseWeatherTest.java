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

/**
 *
 * @author konstas
 */
public class SemParseWeatherTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public SemParseWeatherTest() {
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
         String args = "-modelType semParse -testInputLists test/testWeatherGovEvents "
//         String args = "-modelType semParse -testInputLists gaborLists/semParseWeatherEval200 "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/weatherGov/alignments/model_3_gabor_mapVecs"
                    + "/0.exec/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 10 -ngramSize 3 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-weather-semantic-full-less-times-3-gram.model.arpa "
                    + "-ngramWrapper srilm -reorderType eventTypeAndField "
                    + "-maxPhraseLength 5 -newFieldPerWord 0,-1 "
                    + "-modelUnkWord -allowNoneEvent";
//
//                    + " -excludedFields skyCover.time temperature.time windChill.time "
//                    + "windSpeed.time windDir.time gust.time "
//                    + "precipPotential.time thunderChance.time "
//                    + "snowChance.time freezingRainChance.time sleetChance.time";// rainChance.time";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
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
//        assertEquals(model.testSemParse(name, lopts), 1.0, 0);
        System.out.println(model.testSemParse(name, lopts));
    }
}
