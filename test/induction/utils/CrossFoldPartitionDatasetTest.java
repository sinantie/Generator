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
    
   /**
     * Partition the text of each example into raw sentences prefixed with <s> <s> and suffixed with </s>
     */
//   @Test
    public void testWinHelpSentences()
    {     
        String args = 
                   "-modelType event3 "
                 + "-inputType raw "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "winHelpLM/winHelpRL-docs-newAnnotation-3-gram.sentences "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/winHelpLM/docs.newAnnotation "
                 + "-prefix winHelp "
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
   
    /**
     * Partition event3 examples into whole documents
     */
   @Test
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
   
   /**
    * Partition event3 examples and for each example split into separate sentences.
    */
//    @Test
    public void testWinHelpSents()
    {     
        String args = 
                   "-modelType event3 "
                 + "-inputType event3 "
                 + "-examplesInSingleFile "
                 + "-inputLists "
                 + "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                 + "-execDir "
                 + "data/branavan/winHelpHLA/folds/sents.newAnnotation "
                 + "-splitDocToSentences "
                 + "-prefix winHelp "                 
                 + "-folds 10 ";                 
        CrossFoldPartitionDatasetOptions opts = new CrossFoldPartitionDatasetOptions();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        CrossFoldPartitionDataset c = new CrossFoldPartitionDataset(opts);
        c.testExecute();
    }
}
