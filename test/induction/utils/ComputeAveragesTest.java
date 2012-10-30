package induction.utils;

import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import induction.utils.postprocess.ComputeAverages;
import induction.utils.postprocess.ComputeAveragesOptions;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class ComputeAveragesTest
{
    
    GenerativeEvent3Model model;
    
    public ComputeAveragesTest()
    {
    }
    
    @Before
    public void setUp()
    {
        
    }
   
    /**
     * Test of main method, of class ExtractRecordsStatistics.
     */
    @Test
    public void testWeather()
    {     
        String args = 
                   "-exportType recordType "
                 +  "-examplesInSingleFile "
//                 + "-countRepeatedRecords "
                 + "-extractRecordTrees "
                 + "-binarize right "
                 + "-modifiedBinarization "
                 + "-extractNoneEvent "
//                 + "-useEventTypeNames "
//                 + "-countSentenceNgrams "
//                 + "-countDocumentNgrams "
                 + "-writePermutations "
                 + "-delimitSentences "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovGenDevGabor.gz "
//                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir "
                 + "weatherGovLM/recordStatistics "
                 + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
                 + "-inputFileExt events ";
//                 + "-ngramWrapper kylm "
//                 + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ComputeAveragesOptions opts = new ComputeAveragesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ComputeAverages ca = new ComputeAverages(opts);
        ca.testExecute();

    }
}
