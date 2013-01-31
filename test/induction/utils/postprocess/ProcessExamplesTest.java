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
//    @Test
    public void testWeather()
    {        
        String args = 
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovGenEvalGabor.gz "
//                 + "data/weatherGov/weatherGovGenDevGabor.gz "
//                 + "data/weatherGov/weatherGovGenEvalGabor.gz "
//                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir data/weatherGov "
                 + "-stagedParamsFile results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.params.obj "
                 + "-predFileType generation " // 0.42509076
                 + "-fullPredOutput "
                 + "/home/sinantie/EDI/Generator/results/output/weatherGov/generation/pcfg/model_3_60-best_0.01_treebank_unaryRules_no_null_markov1_wordsPerRootRule_0.04_svrPredLength/stage1.test.full-pred-gen "
//                 + "-predFileType alignment " // 0.42520976
//                 + "-fullPredOutput "
//                 + "/home/sinantie/EDI/Generator/results/output/weatherGov/alignments/model_3_gabor_bigrams_again/stage1.train.full-pred.14 "
//                 + "-actionType computePermMetrics";
//                 + "-actionType splitDocToSentences";
                 + "-actionType recordTypeStatistics "
                 + "-excludeField time";
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
        int fold = 10;
        String args = 
                   "-examplesInSingleFile "
                 + "-inputFileExt events "
                 + "-modelType event3 "
                 + "-inputLists "
//                 + "data/branavan/winHelpHLA/folds/docs.newAnnotation.removedOutliers/winHelpFold"+fold+"Eval "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "statistics/winHelp "
                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.params.obj.gz "
//                 + "-predFileType generation " // 0.42509076
//                 + "-fullPredOutput "
//                 + "results/output/winHelp/generation/generative/no_pos/no_null/model_3_docs_newAnnotation_150-best_iter1_max18_gold/stage1.test.all.full-pred-gen "
                 + "-predFileType alignment " // 0.23474468
                 + "-fullPredOutput "
                 + "results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.train.full-pred.1 "
                 + "-actionType averageAlignmentsPerExample "; // 9.11
//                 + "-actionType averageWordsPerDocument "; // 51.92
//                 + "-actionType averageWordsPerSentence"; // 11.91
//                 + "-actionType maxDocLength"; // 153
//                 + "-actionType maxValueLength"; // 18
//                 + "-actionType averageRecordsPerExample"; // 9.2 (Max 25)
//                 + "-actionType computePermMetrics";
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
