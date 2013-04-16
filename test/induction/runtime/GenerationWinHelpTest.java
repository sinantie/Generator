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
public class GenerationWinHelpTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public GenerationWinHelpTest() {
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
//                    + "-testInputLists data/branavan/winHelpHLA/folds/docs.cleaned/winHelpFold3Eval "
                    + "-testInputLists data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.single.newAnnotation "
//                    + "-testInputLists test/winHelpFold1EvalTest "
                    + "-inputFileExt events "
                    + "-examplesInSingleFile "
                    + "-stagedParamsFile "
                    + "results/output/winHelp/alignments/pos/"
//                    + "model_3_no_null_pos_auto/fold1/stage1.params.obj.gz "
                    + "model_3_docs_no_null_newAnnotation/fold2/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields "
                    + "-kBest 80 "
//                    + "-ngramModelFile winHelpLM/srilm-abs-winHelpRL-docs-fold3-3-gram.model.arpa "
                    + "-ngramModelFile winHelpLM/kylm-abs-winHelpRL-docs-newAnnotation-3-gram.model.arpa "
                    + "-ngramWrapper srilm "
                    + "-allowConsecutiveEvents "
                    + "-reorderType eventType "
                    + "-maxPhraseLength 12 "
//                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-lengthPredictionMode gold "
//                    + "-lengthPredictionModelFile data/branavan/winHelpHLA/folds/winHelpFold1Train.lengthPrediction.counts.linear-reg.model "
                    + "-lengthPredictionFeatureType counts "
                    + "-lengthPredictionStartIndex 2 "
                    + "-lengthCompensation 0 "
//                    + "-useDependencies "
                    + "-interpolationFactor 1 "
//                    + "-posAtSurfaceLevel "
//                    + "-tagDelimiter _ "
                    + "-useStopNode "                    
                    + "-outputFullPred "
                    + "-dmvModelParamsFile results/output/winHelp/dmv/train/"
                    + "winHelp_uniformZ_initNoise_POS_auto_100/fold1/stage1.dmv.params.obj.gz ";
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
        String targetOutput = "<doc docid=\"Example_22188_sent_0\" genre=\"nw\">"
                + "<p><seg id=\"1\" bleu=\"0.619945900436246\" bleu_modified=\"0.619945900436246\" "
                + "meteor=\"0.8376686317842197\" ter=\"0.3333333333333333\">"
                + "click_NNS to_TO settings_NNS ,_, and_CC then_RB click_NNS start_VBP ,_, "
                + "and_CC then_RB click_VB control_NN panel_NN</seg></p></doc>";
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
        
//        assertEquals(in, targetOutput);
    }
}