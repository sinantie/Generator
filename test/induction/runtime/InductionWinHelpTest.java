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
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpHLA.sents.all "
//                 + "test/testWinHelpExamples  "
                 + "-examplesInSingleFile "
                 + "-indepEventTypes 0,10 "
                 + "-indepFields 0,5 "
                 + "-newEventTypeFieldPerWord 0,5 "
                 + "-newFieldPerWord 0,5 "
                 + "-disallowConsecutiveRepeatFields "
                 + "-indepWords 0,5 "
                 + "-initNoise 0 "
                 + "-dontCrossPunctuation "
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged "                // IMPORTANT!
                 + "-Options.stage1.smoothing 0.01 ";
//                 + "-modelUnkWord "
//                 + "-Options.stage1.useVarUpdates";
//                + "-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport";
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
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
        String targetOutput = "8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8";
        assertEquals(model.testInitLearn(name, lopts).trim(), targetOutput);
    }
}