package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class DiscriminativeGenerationAtisTest
{
    LearnOptions lopts;
    String name;
    DiscriminativeEvent3Model model;
    
    public DiscriminativeGenerationAtisTest()
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
        String args = "-modelType generate -testInputLists test/testAtisExamples "
//         String args = "-modelType generate -inputLists data/atis/test/atis-test.txt "                   
                    + "-examplesInSingleFile -numThreads 1 "
                    + "-inputFileExt events "                    
                    + "-dontOutputParams "            
                    + "-generativeModelParamsFile results/output/atis/alignments/"
                    + "model_3/prior_0.01/stage1.params.obj "
                    + "-stagedParamsFile results/output/atis/generation/discriminative/"
                    + "baseline_ignore_ngrams_numWordsField_hasCons_staged_ignore/stage1.discriminative.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields "                 
                    + "-kBest 40 "
                    + "-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa "
                    + "-ngramWrapper srilm -allowConsecutiveEvents -reorderType "
                    + "eventType -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model "
                    + "-lengthPredictionFeatureType COUNTS "
                    + "-lengthPredictionStartIndex 2 "
                    + "-lengthCompensation 0";
//                    + "-oracleReranker";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new DiscriminativeEvent3Model(opts);
        model.init(InitType.staged, null, "stage1");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        name = "stage1";
    }
    
    @After
    public void tearDown()
    {
    }
    
    /**
     * Test of run method, of class Induction.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");        
        System.out.println(model.testGenerate(name, lopts));
    } 
}
