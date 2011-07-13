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
public class GenerationAtisTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public GenerationAtisTest() {
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
                    + "-inputFileExt events -examplesInSingleFile -stagedParamsFile "
                    + "results/output/atis/alignments/"
                    + "model_3/15_iter_no_null_no_smooth_STOP/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 40 "
                    + "-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa "
                    + "-ngramWrapper kylm -allowConsecutiveEvents -reorderType "
                    + "eventType -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3";
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
        String targetOutput = "<doc docid=\"Example_1\" genre=\"nw\"><p>"
                + "<seg id=\"1\" bleu=\"0.1743883015602825\" bleu_modified=\"0.1743883015602825\" "
                + "meteor=\"0.531022469646483\" ter=\"0.7142857142857143\">"
                + "show me the flights from dallas to</seg></p></doc>";
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
        assertEquals(in, targetOutput);
    }
}