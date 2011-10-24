package induction.runtime;

import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import fig.exec.Execution;
import induction.BigDouble;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class DiscriminativeTrainAtisTest
{
    LearnOptions lopts;
    String name;
    DiscriminativeEvent3Model model;

    public DiscriminativeTrainAtisTest() {
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
//        double[] ds = {
//            0.00750711406931216,
//            3.814224416714681E-6,
//            2.5277453964121733E-6,
//            2.525145033878648E-6,
//            2.5259994115003247E-6,
//            2.525145228975861E-6,
//            2.525295174902077E-6,
//            2.5252147223873492E-6,
//            2.528298464298493E-6,
//            0.400084988972228
//                      };
//        double prod = 1.0, logSum = 0.0;
//        for(Double d : ds)
//        {
//            prod *= d;
//            logSum += Math.log(d);
//        }
//        System.out.println("product = " + prod);
//        System.out.println("logSum = " + logSum);
//        System.out.println("logProduct = " + Math.log(prod));
//        
//        BigDouble bd = BigDouble.fromDouble(7.519856925725315E-48);
//        System.out.println(bd.toLogDouble());
//        System.exit(0);
         String args = "-modelType discriminativeTrain -inputLists test/testAtisExamples "
//         String args = "-modelType discriminativeTrain -inputLists data/atis/train/atis5000.sents.full "
                    + "-Options.stage1.numIters 15 "
                    + "-Options.stage1.learningScheme incremental "
                    + "-examplesInSingleFile "
                    + "-inputFileExt events "                    
                    + "-dontOutputParams "            
                    + "-generativeModelParamsFile results/output/atis/alignments/"
                    + "model_3/prior_0.01/stage1.params.obj "
//                    + "-generativeModelParamsFile results/output/atis/alignments/"
//                    + "model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -dontCrossPunctuation "
//                    + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 -indepWords 0,5 "
                 
                 
                    + "-kBest 40 "
                    + "-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa "
                    + "-ngramWrapper srilm -allowConsecutiveEvents -reorderType "
                    + "eventType -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 "
//                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 4";
         
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