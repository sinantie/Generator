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
                 + "-externalTreebankFile data/weatherGov/treebanks/recordTreebankTrainRightBinarizeAlignmentsMarkov1 "
                 + "-treebankRules data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeAlignmentsMarkov1 "
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
                 + "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold1Train.tagged "
//                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "results/output/winHelp/alignments/pos/model_3_docs_no_null_newAnnotation_markov1_externalTreebank/fold1 "
//                 + "results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_markov1_externalTreebank/all "
                 + "-stagedParamsFile results/output/winHelp/alignments/pos/model_3_docs_no_null_newAnnotation/fold1/stage1.params.obj.gz "
//                 + "-stagedParamsFile results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.params.obj.gz "
                 + "-externalTreebankFile data/branavan/winHelpHLA/folds/treebanks/recordTreebankRightBinarizeNewAnnotationAlignmentsMarkov1Fold1 "
//                 + "-externalTreebankFile data/branavan/winHelpHLA/recordTreebankRightBinarizeNewAnnotationAlignmentsMarkov1 "
                 + "-treebankRules data/branavan/winHelpHLA/folds/treebanks/recordTreebankRulesRightBinarizeNewAnnotationAlignmentsMarkov1Fold1 "
//                 + "-treebankRules data/branavan/winHelpHLA/recordTreebankRulesRightBinarizeNewAnnotationAlignmentsMarkov1 "
                 + "-maxDocLength 100 "
                 + "-docLengthBinSize 15 "
                 + "-initSmoothing 0.0001 "
                 + "-inputPosTagged "
                 + "-tagDelimiter _ "
                 + "-inputFileExt events ";
        MergeParamsWithExternalTreebankOptions opts = new MergeParamsWithExternalTreebankOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        MergeParamsWithExternalTreebank m = new MergeParamsWithExternalTreebank(opts);
        m.testExecute();
    }
}
