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
public class SemParseRobocupTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public SemParseRobocupTest() {
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
         String args = "-modelType semParse -testInputLists test/testRobocupEvents "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/robocup/model_3_percy_NO_NULL_semPar_values/fold1/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 5 "
                    + "-ngramModelFile robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa "
                    + "-ngramWrapper kylm -reorderType "
                    + "eventType -maxPhraseLength 5 -useGoldStandardOnly -newFieldPerWord 0,-1";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new Event3Model(opts);
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
        String targetOutput = "<doc docid=\"data/robocup-data/2001final-percy/2001final-train-106.text\""
                            + " genre=\"nw\"><p>"
                            + "<seg id=\"1\" bleu=\"0.9375\" "
                            + "bleu_modified=\"0.9375\" "
                            + "meteor=\"1.0\" ter=\"0.0\">"
                            + "purple10 passes to purple11</seg></p></doc>";
//        assertEquals(model.testGenerate(name, lopts).trim().replaceAll("\\n", ""), targetOutput);
        System.out.println(model.testSemParse(name, lopts));
    }
}