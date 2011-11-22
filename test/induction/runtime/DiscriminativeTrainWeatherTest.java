package induction.runtime;

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

/**
 *
 * @author konstas
 */
public class DiscriminativeTrainWeatherTest
{
    LearnOptions lopts;
    String name;
    DiscriminativeEvent3Model model;

    public DiscriminativeTrainWeatherTest() {
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
         String args = "-modelType discriminativeTrain -inputLists test/testWeatherGovEvents "
                    + "-Options.stage1.numIters 15 -numThreads 1 "
                    + "-Options.stage1.learningScheme incremental "
                    + "-inputFileExt events "
                    + "-dontOutputParams "            
                    + "-generativeModelParamsFile results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -dontCrossPunctuation -allowNoneEvent "
//                    + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 -indepWords 0,5 "
                 
//                    + "-kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
                    + "-ngramWrapper srilm -allowConsecutiveEvents -reorderType "
                    + "eventType -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 "
//                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 4 ";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new DiscriminativeEvent3Model(opts);
        model.init(InitType.supervised, null, "stage1");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
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
        System.out.println(model.testDiscriminativeLearn(name, lopts));
    }
}