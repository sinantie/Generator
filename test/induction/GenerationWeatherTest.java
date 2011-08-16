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
public class GenerationWeatherTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;

    public GenerationWeatherTest() {
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
         String args = "-modelType generate -testInputLists test/testWeatherGovEvents "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_control_cond_null/0.exec/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
                    + "-ngramWrapper kylm -allowConsecutiveEvents -reorderType "
                    + "eventType -allowNoneEvent -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 4 "
                    + "-oracleReranker";
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
//        String targetOutput = "<doc docid=\"data/weather-data-full/data/virginia/"
//                            + "falls_church/2009-02-07-0.text\" genre=\"nw\"><p>"
//                            + "<seg id=\"1\" bleu=\"0.8039183415894011\" "
//                            + "bleu_modified=\"0.8039183415894011\" "
//                            + "meteor=\"0.9390967447612161\" ter=\"0.058823529411764705\">"
//                            + "mostly cloudy , with a low around 53 . southwest "
//                            + "wind between 9 and 14 mph .</seg></p></doc>";
        String targetOutput = "<doc docid=\"data/weather-data-full/data/virginia/"
                            + "glen_allen/2009-02-08-1.text\" genre=\"nw\"><p>"
                            + "<seg id=\"1\" bleu=\"0.8039183415894011\" "
                            + "bleu_modified=\"0.8039183415894011\" "
                            + "meteor=\"0.9390967447612161\" ter=\"0.058823529411764705\">"
                            + "mostly cloudy , with a low around 56 . south "
                            + "wind between 3 and 6 mph .</seg></p></doc>";
        String in = model.testGenerate(name, lopts).trim().replaceAll("\\n", "");
        assertEquals(in, targetOutput);
    }
}