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
    @Test
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
}
