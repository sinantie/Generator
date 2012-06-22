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
public class ServerGenerationWeatherTest
{
    LearnOptions lopts;
    String name;
    Event3Model model;
    
    public ServerGenerationWeatherTest() {
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
                    + "-inputFileExt events "
//                    + "-initType staged "
                    + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
//                    + "pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields"
                    + " -kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
//                    + "-ngramModelFile weatherGovLM/dev/gabor-srilm-abs-weather-dev-3-gram.model.arpa "
                    + "-ngramWrapper srilm "
//                    + "-allowConsecutiveEvents "
//                    + "-excludedEventTypes gust "
                    + "-reorderType eventType "
                    + "-allowNoneEvent "
//                    + "-conditionNoneEvent "
                    + "-maxPhraseLength 5 "
                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-numAsSymbol "
                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 2 " // IMPORTANT!!!
                    + "-numAsSymbol ";
//                    + "-useDependencies "
//                    + "-interpolationFactor 0.5 "
//                    + "-posAtSurfaceLevel "                    
//                    + "-dmvModelParamsFile results/output/weatherGov/dmv/train/"
//                    + "weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz"; 
//                 + "-oracleReranker";
//                    + "-omitEmptyEvents";
//                    + "-useGoldStandardOnly";
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
        String exampleUrl = "../../Dropbox/Documents/EDI/Reports/Generator/wunderground/heidelberg.json";
//        String exampleUrl = "../../Dropbox/Documents/EDI/Reports/Generator/wunderground/hourly_california_2.json";
        String in = model.processExamplesJson(JsonFormat.wunderground, exampleUrl, lopts, "", "english");        
        System.out.println(in);        
        assertEquals(pred, in);
    }
}