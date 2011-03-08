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
//         String args = "-modelType semParse -testInputLists test/testRobocupEvents "
         String args = "-modelType semParse -testInputLists robocupLists/robocupFold1PathsEval "
                    + "-excludeLists robocupLists/robocupAllUnreachable "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField"
                    + "/fold1/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 15 -ngramSize 2 "
                    + "-ngramModelFile robocupLM/srilm-abs-robocup-semantic-fold1-noisy-3-gram.model.arpa "
                    + "-ngramWrapper srilm -reorderType eventTypeAndField "
                    + "-maxPhraseLength 5 -useGoldStandardOnly -newFieldPerWord 0,-1 "
                    + "-modelUnkWord -allowConsecutiveEvents";
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
//        assertEquals(model.testSemParse(name, lopts), 1.0, 0);
        System.out.println(model.testSemParse(name, lopts));

        
    }
}