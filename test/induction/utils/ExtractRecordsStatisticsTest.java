package induction.utils;

import induction.Options;
import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import induction.Utils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
//        double[][] ar = new double[36][36];
//        for(String line: Utils.readLines(("/home/sinantie/data")))
//        {
//            String tok[] = line.split("\t");
//            ar[Integer.valueOf(tok[0])][Integer.valueOf(tok[1])] = Double.valueOf(tok[2]);
//        }        
//        StringBuilder str = new StringBuilder();
//        for(double[] line : ar)
//        {
//            for(Double d : line)
//            {
//                str.append(d > 0.0 ? String.format("%.5f", d) : "").append("\t");
//            }
//            str.append("\n");
//        }
//        Utils.write("/home/sinantie/data.out", str.toString());
//        System.exit(0);
    }
   
    /**
     * Test of main method, of class ExtractRecordsStatistics.
     */
    @Test
    public void testWeather()
    {     
        String args = 
                   "-exportType recordType "
//                 + "-countRepeatedRecords "
                 + "-countSentenceNgrams "
                 + "-writePermutations "
                 + "-delimitSentences "
                 + "-modelType event3 "
                 + "-inputLists "
                 + "test/testWeatherGovEvents "
//                 + "gaborLists/trainListPathsGabor "                 
                 + "-execDir "
                 + "weatherGovLM/recordStatistics "
                 + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
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
}
