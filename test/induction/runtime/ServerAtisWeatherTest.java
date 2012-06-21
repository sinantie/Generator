/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.runtime;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.Options.JsonFormat;
import induction.problem.event3.Event3Model;
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
public class ServerAtisWeatherTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;
    
    public ServerAtisWeatherTest() {
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
                    + "-inputFileExt events "
                    + "-stagedParamsFile "
                    + "results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj "
                    + "-examplesInSingleFile "
                    + "-disallowConsecutiveRepeatFields"
                    + " -kBest 40 "
                    + "-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa "
                    + "-ngramWrapper srilm "
                    + "-allowConsecutiveEvents "
                    + "-reorderType eventType "                    
//                    + "-conditionNoneEvent "
                    + "-maxPhraseLength 5 "
                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-numAsSymbol "
                    + "-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model "
                    + "-lengthPredictionFeatureType COUNTS "
                    + "-lengthPredictionStartIndex 2 " // IMPORTANT!!!
                    + "-useStopNode ";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");   
        model.getWordIndexer().add("(boundary)"); // from readExamples
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        opts.alignmentModel = lopts.alignmentModel;
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
        String pred="[{\"index\":0,\"title\":\"Monday Night\",\"text\":\"A chance of showers before 1am. Areas of fog. Otherwise, mostly cloudy, with a south wind between 4 and 5 mph.\",\"semanticAlignment\":\"rainChance(18)[(none)[a chance of showers]  time[before_v] ] snowChance(23)[time[1am_v ._v areas_v of_v fog_v] ] freezingRainChance(27)[mode[._v] ] thunderChance(15)[mode[otherwise_v ,_v mostly_v cloudy_v ,_v] ] temperature(0)[(none)[with a] ] windDir(3)[mode[south_v]  (none)[wind between] ] windSpeed(2)[min[4_v1]  max[and 5_v1 mph .] ] \",\"performance\":\"\"},{\"index\":1,\"title\":\"Tuesday\",\"text\":\"A chance of showers. High near 77. West southwest wind between 4 and\",\"semanticAlignment\":\"rainChance(19)[(none)[a chance of showers]  time[._v] ] temperature(0)[time[high_v near_v]  max[77_v1]  mean[.] ] windDir(3)[mode[west_v southwest_v wind_v]  (none)[between] ] windSpeed(2)[min[4_v1]  max[and] ] \",\"performance\":\"\"}]";
//String example = "{\"flight\":{\"class_type\":\"--\",\"direction\":\"round_trip\",\"from\":\"atlanta\",\"stop\":\"--\",\"to\":\"boston\"},\"search\":[{\"of\":\"flight\",\"typed\":\"max\",\"what\":\"fare\"},{\"of\":\"departure_time\",\"typed\":\"argmin\",\"what\":\"flight\"}]}";
        String example = "{\"flight\":{\"class_type\":\"--\",\"direction\":\"round_trip\",\"from\":\"atlanta\",\"stop\":\"--\",\"to\":\"boston\"},\"search\":[{\"of\":\"--\",\"typed\":\"lambda\",\"what\":\"flight\"}]}";
        String in = model.processExamplesJson(JsonFormat.lowjet, example, lopts);
        System.out.println(in);        
//        assertEquals(pred, in);
    }
}