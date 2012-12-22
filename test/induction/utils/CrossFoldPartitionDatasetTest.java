package induction.utils;

import fig.exec.Execution;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class CrossFoldPartitionDatasetTest
{          
    public CrossFoldPartitionDatasetTest()
    {
    }
    
    @Before
    public void setUp()
    {
    }
   
   @Test
    public void testWinHelp()
    {     
        String args = 
                   "-modelType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/docs.newAnnotation "
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
}
