/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction;

import fig.exec.Execution;
import induction.Options.InitType;
import induction.problem.event3.Event3Model;
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
public class DiscriminativeTrainGenerationWeatherTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public DiscriminativeTrainGenerationWeatherTest() {
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
         String args = "-modelType event3 -Options.stage1.numIters 15 -testInputLists "
                + "gaborLists/weatherEvalScenariosRandomBest12Events -inputFileExt events "
                + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 "
                + "-disallowConsecutiveRepeatFields -indepWords 0,5 "
                + "-dontCrossPunctuation -Options.stage1.smoothing 0.1";
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new Event3Model(opts);
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