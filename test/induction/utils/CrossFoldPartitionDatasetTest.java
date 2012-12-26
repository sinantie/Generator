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
    public void testWinHelpSentences()
    {     
        String args = 
                   "-modelType event3 "
                 + "-inputType raw "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "winHelpLM/winHelpRL-docs-newAnnotation-3-gram.sentences "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/winHelpLM/new.annotation "
                 + "-prefix winHelp "
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
   
//   @Test
    public void testWinHelpDocs()
    {     
        String args = 
                   "-modelType event3 "
                 + "-inputType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/docs.newAnnotation "
                 + "-prefix winHelp "
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
   
//    @Test
    public void testWinHelpSents()
    {     
        String args = 
                   "-modelType event3 "
                 + "-inputType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.sents.all.newAnnotation "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/sents.newAnnotation "
                 + "-prefix winHelp "
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
}
