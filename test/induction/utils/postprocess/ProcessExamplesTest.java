package induction.utils.postprocess;

import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class ProcessExamplesTest
{
    
    GenerativeEvent3Model model;
    
    public ProcessExamplesTest()
    {
    }
    
    @Before
    public void setUp()
    {
        
    }
   
    /**
     * Test of main method, of class ExtractRecordsStatistics.
     */
    //@Test
    public void testWeather()
    {        
        String args = 
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovGenEvalGabor.gz "
//                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir "
                 + "statistics/weatherGov "
                 + "-actionType averageWordsPerDocument";
//                 + "-actionType averageWordsPerSentence";
//                 + "-actionType averageSentencesPerDocument";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ProcessExamplesOptions opts = new ProcessExamplesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ProcessExamples ca = new ProcessExamples(opts);
        ca.testExecute();
    }
    
    /**
     * Test of main method, of class ExtractRecordsStatistics.
     */
    @Test
    public void testWinHelp()
    {        
        String args = 
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "statistics/winHelp "
//                 + "-actionType averageWordsPerDocument "; // 52.07
//                 + "-actionType averageWordsPerSentence"; // 11.97
//                 + "-actionType maxDocLength"; // 153
                 + "-actionType maxValueLength"; // 18
//                 + "-actionType splitDocToSentences";
//                 + "-actionType exportExamplesAsSentences";
//                 + "-actionType averageSentencesPerDocument";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ProcessExamplesOptions opts = new ProcessExamplesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ProcessExamples ca = new ProcessExamples(opts);
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
        ProcessExamplesOptions opts = new ProcessExamplesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ProcessExamples ca = new ProcessExamples(opts);
        ca.testExecute();
    }
}
