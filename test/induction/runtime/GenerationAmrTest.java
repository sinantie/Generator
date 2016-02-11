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
public class GenerationAmrTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public GenerationAmrTest() {
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
                    + "-testInputLists ../hackathon/data/ldc/split/training/training-thres-5-test.event3 "
                    + "-inputFileExt events "
                    + "-examplesInSingleFile "
                    + "-stagedParamsFile "
                    + "results/output/amr/ldc/alignments/model_3-thres-5-bootstrap-ignoreFields/5.exec/stage1.params.obj.gz "
//                    + "results/output/amr/ldc/alignments/model_3-thres-5/lemmatise-generateNonEmptyFieldsOnly/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields "
                    + "-kBest 100 "
                    + "-ngramModelFile amrLM/training-3-gram-sentences.arpa "
//                    + "-secondaryNgramModelFile atisLM/atis-all-train-3-gram-tagged.CDnumbers.tags_only.model.arpa "
                    + "-ngramWrapper kylm "
                    + "-allowConsecutiveEvents "
                    + "-reorderType eventTypeAndField "
                    + "-maxPhraseLength 3 "
                    + "-binariseAtWordLevel "
                    + "-indepWords 0,-1 "
                    + "-useFieldSets 0,-1 "
                    + "-omitEmptyEvents "
                    + "-ngramSize 3 "
//                    + "-lengthPredictionMode linearRegression "
                    + "-lengthPredictionMode gold "
//                    + "-lengthPredictionModelFile ../datasets/atis/train/lengthPrediction.counts.linear-reg.model "
//                    + "-lengthPredictionFeatureType counts "
//                    + "-lengthPredictionStartIndex 2 "
                    + "-lengthCompensation 0 "
//                    + "-useDependencies "
                    + "-interpolationFactor 1 "
                    + "-outputFullPred "
//                    + "-posAtSurfaceLevel "
//                    + "-useStopNode "
//                    + "-dmvModelParamsFile results/output/atis/dmv/train/"
//                    + "atis_raw5000_full_indexers_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz "
                    + "-forceOutputOrder";
//                    + "atis_raw5000_full_indexers_prior_01_LEX_100/stage1.dmv.params.obj.gz";
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
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");        
    }
}