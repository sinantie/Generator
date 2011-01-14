/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.problem.event3;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class Event3ModelTest extends TestCase{

    public Event3ModelTest() {
    }
   
   /**
     * Test of generate method, of class AModel.
     */
    @Test
    public void testGenerate()
    {
        System.out.println("generate");
        String args = "-modelType generate -testInputLists test/testWeatherGovEvents "
                    + "-inputFileExt events -stagedParamsFile ../ContentSelectionJava/"
                    + "results/output/model_3_gabor/0.exec/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 15 "
                    + "-ngramModelFile data/gabor-srilm-abs-3-gram.model.arpa "
                    + "-ngramWrapper kylm -allowConsecutiveEvents -reorderType "
                    + "eventType -allowNoneEvent";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        Event3Model model = new Event3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        LearnOptions lopts = opts.stage1;
        String name = "stage1";
        
        String targetOutput = "<doc docid=\"data/weather-data-full/data/virginia/"
                            + "falls_church/2009-02-07-0.text\" genre=\"nw\"><p>"
                            + "<seg id=\"1\" bleu=\"0.8039183415894011\" "
                            + "bleu_modified=\"0.8039183415894011\" "
                            + "meteor=\"0.9390967447612161\" ter=\"0.058823529411764705\">"
                            + "mostly cloudy , with a low around 53 . southwest "
                            + "wind between 9 and 14 mph .</seg></p></doc>";
        assertEquals(model.testGenerate(name, lopts).trim().replaceAll("\\n", ""), targetOutput);
    }
}