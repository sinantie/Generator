/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.runtime;

import fig.basic.Indexer;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.Options.JsonFormat;
import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
                    + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
//                    + "pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields"
                    + " -kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
//                    + "-ngramModelFile weatherGovLM/dev/gabor-srilm-abs-weather-dev-3-gram.model.arpa "
                    + "-ngramWrapper srilm "
                    + "-allowConsecutiveEvents "
                    + "-reorderType eventType "
                    + "-allowNoneEvent "
//                    + "-conditionNoneEvent "
                    + "-maxPhraseLength 5 "
                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-numAsSymbol "
                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 4 "
                    + "-numAsSymbol "
//                    + "-useDependencies "
                    + "-interpolationFactor 0.5 "
//                    + "-posAtSurfaceLevel "                    
                    + "-dmvModelParamsFile results/output/weatherGov/dmv/train/"
                    + "weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz"; 
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
        try
        {
            String example = Utils.readFileAsString("../../Dropbox/Documents/EDI/Reports/Generator/wunderground/hourly_california.json");
            String in = model.processSingleExampleJson(JsonFormat.wunderground, example, lopts);
            System.out.println(in);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}