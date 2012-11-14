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
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovTrainGabor.gz "
//                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir "
                 + "statistics/weatherGov "
                 + "-actionType averageWordsPerDocument";
//                 + "-actionType averageWordsPerSentence";
//                 + "-actionType averageSentencesPerDocument";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ComputeAveragesOptions opts = new ComputeAveragesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ComputeAverages ca = new ComputeAverages(opts);
        ca.testExecute();
    }
    
    //@Test
    public void testAtis()
    {
        String args = 
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/atis/train/atis5000.sents.full.prototype "
//                 + "data/atis/test/atis-test.txt "
                 + "-execDir "
                 + "statistics/atis "
                 + "-actionType averageFieldsWithNoValuePerRecord"
//                 + "-actionType averageAlignmentsPerExample"
                 + "-record flight "
                 + "-totalNumberOfFields 13";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ComputeAveragesOptions opts = new ComputeAveragesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ComputeAverages ca = new ComputeAverages(opts);
        ca.testExecute();
    }
}
