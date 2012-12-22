package induction.utils;

import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class MergeParamsWithExternalTreebankTest
{          
    public MergeParamsWithExternalTreebankTest()
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
                   "-modelType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/weatherGov/weatherGovTrainGabor.gz "
                 + "-execDir "
                 + "weatherGovLM/recordStatistics "
                 + "-stagedParamsFile results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
                 + "-externalTreebankFile data/weatherGov/treebanks/recordTreebankTrainRightBinarizeUnaryRulesFilteredAlignments "
                 + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRulesFilteredAlignments "
                 + "-inputFileExt events ";
        MergeParamsWithExternalTreebankOptions opts = new MergeParamsWithExternalTreebankOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        MergeParamsWithExternalTreebank m = new MergeParamsWithExternalTreebank(opts);
        m.testExecute();
    }
    
    @Test
    public void testWinHelp()
    {     
        String args = 
                   "-modelType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/folds/docs.cleaned/winHelpFold1Train "
                 + "-execDir "
                 + "results/output/winHelp/alignments/model_3_docs_staged_no_null_cleaned_objType_markov0_externalTreebank/fold1 "
                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_docs_staged_no_null_cleaned_objType/fold1/stage1.params.obj.gz "
                 + "-externalTreebankFile data/branavan/winHelpHLA/folds/treebanks/recordTreebankRightBinarizeCleanedObjTypeMarkov0Fold1 "
                 + "-treebankRules data/branavan/winHelpHLA/folds/treebanks/recordTreebankRulesRightBinarizeCleanedObjTypeMarkov0Fold1 "
                 + "-maxDocLength 150 "
                 + "-docLengthBinSize 10 "
                 + "-inputFileExt events ";
        MergeParamsWithExternalTreebankOptions opts = new MergeParamsWithExternalTreebankOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        MergeParamsWithExternalTreebank m = new MergeParamsWithExternalTreebank(opts);
        m.testExecute();
    }
}