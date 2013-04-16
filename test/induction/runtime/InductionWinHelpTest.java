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
public class InductionWinHelpTest
{
    LearnOptions lopts;
    String name;
    GenerativeEvent3Model model;

    public InductionWinHelpTest() {
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
         String args = "-modelType event3 "
                 + "-Options.stage1.numIters 15 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
//                 + "data/branavan/winHelpHLA/winHelpRL.sents.all.tagged "
//                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.docs.all "
//                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "data/branavan/winHelpHLA/folds/sents.newAnnotation/winHelpFold1Train.tagged "
//                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_sents_no_null_cleaned_objType/all/stage1.params.obj.gz "                 
//                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_sents_no_null_newAnnotation/all/stage1.params.obj.gz "
//                 + "-stagedParamsFile data/branavan/winHelpHLA/stage1.test.params.obj.gz "
                 + "-examplesInSingleFile "
                 + "-indepEventTypes 0,10 "
                 + "-indepFields 0,5 "
                 + "-newEventTypeFieldPerWord 0,5 "
                 + "-newFieldPerWord 0,5 "
                 + "-disallowConsecutiveRepeatFields "
                 + "-indepWords 0,0 "
                 + "-initNoise 0 "
                 + "-dontCrossPunctuation "
//                 + "-posAtSurfaceLevel "
                 + "-inputPosTagged "                // IMPORTANT!
                 + "-Options.stage1.smoothing 0.001 ";
//                 + "-modelUnkWord "
//                 + "-Options.stage1.useVarUpdates";
//                + "-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport";
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
//        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();        
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        model.init(InitType.random, opts.initRandom, "");
        lopts = opts.stage1;
        name = "stage1";
//        model.saveParams(name, "data/branavan/winHelpHLA/stage1.test.params.obj.gz");
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
        String targetOutput = "0 0 0 0 0 1 1 1 1 2 2 2 2 2 2 2 2 2 2 2 2 2 3 3 3 3 3";
        assertEquals(model.testInitLearn(name, lopts).trim(), targetOutput);
    }
}