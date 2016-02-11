package induction.utils;

import fig.exec.Execution;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class ComputeAmrEstimatesTest
{          
    public ComputeAmrEstimatesTest()
    {
    }
    
    @Before
    public void setUp()
    {
    }
   
    /**
     * Test of main method, of class ComputeAmrEstimates.
     */
    @Test
    public void testEstimates()
    {     
        String args = 
                   "-modelType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "../hackathon/data/ldc/split/training/training-thres-5.event3 "
                 + "-execDir "
                 + "results/output/amr/ldc/alignments/bootstrap_word_estimates/ "                 
                 + "-sentencesFile ../hackathon/data/ldc/split/training/training-sentences.s.tok.lc "
                 + "-GHKMTreeFile ../hackathon/data/ldc/split/training/training-sentences.t-tree-clean "
                 + "-alignmentsFile ../hackathon/data/ldc/split/training/training-sentences.align.t-s "                 
                 + "-useStringLabels false "                 
                 + "-stripConceptSense "                 
                 + "-indepWords 0,-1 "                 
//                 + "-artNumWords 100 "                 
                 + "-inputFileExt events ";
        ComputeAmrEstimatesOptions opts = new ComputeAmrEstimatesOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        ComputeAmrEstimates m = new ComputeAmrEstimates(opts);
        m.testExecute();
    }
    
}
