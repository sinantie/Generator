/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.runtime;

import fig.exec.Execution;
import induction.Options;
import induction.Options.InitType;
import induction.ngrams.NgramModel;
import induction.problem.event3.Event3Model;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class NgramIndicesTest
{
    DiscriminativeEvent3Model model;
    
    public NgramIndicesTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Before
    public void setUp()
    {
        String args = "-modelType discriminativeTrain -inputLists test/testAtisExamples "
//         String args = "-modelType discriminativeTrain -inputLists data/atis/train/atis5000.sents.full "
                    + "-Options.stage1.numIters 15 "
                    + "-Options.stage1.learningScheme incremental "
                    + "-examplesInSingleFile "
                    + "-inputFileExt events "                    
                    + "-dontOutputParams "            
                    + "-generativeModelParamsFile results/output/atis/alignments/"
                    + "model_3/prior_0.01/stage1.params.obj "
//                    + "-generativeModelParamsFile results/output/atis/alignments/"
//                    + "model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -dontCrossPunctuation "
//                    + "-indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 -indepWords 0,5 "                 
                    + "-kBest 40 "
                    + "-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa "
                    + "-ngramWrapper srilm -allowConsecutiveEvents -reorderType "
                    + "eventType -maxPhraseLength 5 -binariseAtWordLevel "
                    + "-ngramSize 3 ";
         
        /*initialisation procedure from Induction class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params        
        model = new DiscriminativeEvent3Model(opts);
        model.init(InitType.supervised, null, "stage1");
    }
    
    @After
    public void tearDown()
    {
    }
    @Test
    public void testGetNgramIndices()
    {
        DiscriminativeEvent3Model discModel = (DiscriminativeEvent3Model)model;
        String textStr[] = "what flights go from dallas to phoenix".split(" ");
        List<Integer> text = new ArrayList();
        for(int i = 0; i < textStr.length; i++)
            text.add(Event3Model.getWordIndex(textStr[i]));
        List list = NgramModel.getNgramIndices(discModel.getWordNgramMap(), 3, text);
        System.out.println(list);
        
    }
}
