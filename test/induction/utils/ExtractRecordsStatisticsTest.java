package induction.utils;

import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class ExtractRecordsStatisticsTest
{
    
    GenerativeEvent3Model model;
    
    public ExtractRecordsStatisticsTest()
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
    public void testBlocksWorld()
    {     
        String args = 
                   "-exportType recordType "
                 +  "-examplesInSingleFile "
                 +  "-initType staged "
                 + "-extractRecordTrees "
                 + "-countNonTerminals "
                 + "-binarize right "
                 + "-modifiedBinarization "
//                 + "-extractNoneEvent "
                 + "-useEventTypeNames "
                 + "-countSentenceNgrams "
                 + "-countDocumentNgrams "
//                 + "-writePermutations "
//                 + "-delimitSentences "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "datasets/GoldSplitLogo/Records.train "
                 + "-execDir "
                 + "results/GoldSplitLogo/treebanks/ "
                 + "-stagedParamsFile "
                    + "results/GoldSplitLogo/alignments/0.exec/"
                    + "stage1.params.obj.gz "
                 + "-predInput "
                 + "results/GoldSplitLogo/treebanks/Records.train.alignedType.align "
                 + "-suffix Aligned "
//                 + "-externalTreesInput data/weatherGov/weatherGovTrainGaborEdusGold.tree "
//                 + "-externalTreesInputType rst " 
//                 + "-overrideCleaningHeuristics " 
                 + "-inputFileExt events ";
        ExtractRecordsStatisticsOptions opts = new ExtractRecordsStatisticsOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ExtractRecordsStatistics ers = new ExtractRecordsStatistics(opts);
        ers.testExecute();
    }
    
    /**
     * Test of main method, of class ExtractRecordsStatistics.
     */
//    @Test
    public void testWeather()
    {     
        String args = 
                   "-exportType recordType "
                 +  "-examplesInSingleFile "
                 +  "-initType staged "
//                 + "-countRepeatedRecords "
                 + "-extractRecordTrees "
                 + "-countNonTerminals "
//                 + "-ruleCountThreshold 5 "
                 + "-binarize right "
                 + "-modifiedBinarization "
//                 + "-extractNoneEvent "
                 + "-useEventTypeNames "
//                 + "-countSentenceNgrams "
//                 + "-countDocumentNgrams "
//                 + "-writePermutations "
//                 + "-delimitSentences "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovTrainGabor.gz "
//                 + "data/weatherGov/treebanks/torontoRST/weatherGovTrainGabor_single_test "
//                 + "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_test "
//                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir "
                 + "data/weatherGov/treebanks/torontoRST "
                 + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
//                    + "model_3_gabor_no_sleet_windChill_15iter/stage1.params.obj.gz "
                    + "model_3_gabor_staged/stage1.params.obj.gz "
                 + "-predInput "
//                 + "results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.train.pred.14.sorted "
//                 + "data/weatherGov/treebanks/torontoRST/weatherGovTrainGabor_single_test.align "
                 + "data/weatherGov/weatherGovTrainGaborEdusGold.align "
//                 + "results/output/weatherGov/alignments/model_3_gabor_staged/stage1.train.pred.1.sorted "
//                 + "-suffix Ccm "                
//                 + "-externalTreesInput data/weatherGov/treebanks/ccm/permutations.kleinParsing.20 "
//                 + "-externalTreesInput data/weatherGov/treebanks/ccm/permutationsAligns.kleinParsing.20.test "
//                + "-externalTreesInputType unlabelled " 
                 + "-suffix GoldRst "
//                 + "-externalTreesInput data/weatherGov/treebanks/torontoRST/weatherGovTrainGaborEdusAlignedRemoved.tree "
//                 + "-externalTreesInput data/weatherGov/treebanks/torontoRST/weatherGovTrainGabor_single_test.tree "
                 + "-externalTreesInput data/weatherGov/weatherGovTrainGaborEdusGold.tree "
                 + "-externalTreesInputType rst " 
                 + "-overrideCleaningHeuristics " 
//                 + "-extractNoneEvent " 
                
//                 + "-excludedEventTypes sleetChance windChill "
                 + "-inputFileExt events ";
//                 + "-ngramWrapper kylm "
//                 + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ExtractRecordsStatisticsOptions opts = new ExtractRecordsStatisticsOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ExtractRecordsStatistics ers = new ExtractRecordsStatistics(opts);
        ers.testExecute();
    }
    
//    @Test
    public void testWinHelp()
    {     
        String args = 
                   "-exportType recordType "
                 +  "-examplesInSingleFile "
//                 + "-countRepeatedRecords "
                 + "-extractRecordTrees "
                 + "-countNonTerminals "
//                 + "-ruleCountThreshold 5 "
                 + "-binarize right "
//                 + "-modifiedBinarization "
                 + "-markovOrder 0 "
//                 + "-extractNoneEvent "
                 + "-useEventTypeNames "
//                 + "-countSentenceNgrams "
//                 + "-countDocumentNgrams "
//                 + "-writePermutations "
                 + "-delimitSentences "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
//                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.single.newAnnotation "
                 + "-execDir data/branavan/winHelpHLA "
                 + "-initType staged "
                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.params.obj.gz "
                 + "-predInput "
                 + "results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.train.pred.1.sorted "
//                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.single.newAnnotation.align "
                 + "-suffix AlignedRstParent "                 
                 + "-externalTreesInput data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.aligned.edus.tree "
                 + "-externalTreesInputType rst "                
                 + "-parentAnnotation "                
                 + "-inputFileExt events ";
//                 + "-posAtSurfaceLevel "
//                 + "-inputPosTagged"; // IMPORTANT        
        ExtractRecordsStatisticsOptions opts = new ExtractRecordsStatisticsOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ExtractRecordsStatistics ers = new ExtractRecordsStatistics(opts);
        ers.testExecute();
    }
}
