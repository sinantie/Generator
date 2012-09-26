package induction.utils;

import induction.problem.event3.generative.GenerativeEvent3Model;
import fig.exec.Execution;
import induction.Utils;
import java.io.FileOutputStream;
import java.io.IOException;
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
        
        try
        {
            FileOutputStream fos = new FileOutputStream("/home/konstas/EDI/wsj/3.0/conll/wsj-10_percy");
            StringBuilder str = new StringBuilder();
            for(String line : Utils.readLines("/home/konstas/EDI/wsj/3.0/conll/wsj-10_words_tags"))
            {
                if(line.equals(""))
                {
                    str.append("\n");
                    fos.write(str.toString().getBytes());
                    str = new StringBuilder();
                }
                else
                {
                    String[] ar = line.split("\t");
                    str.append(ar[1]).append(" ").append(ar[0]).append(" ");
                }
            }
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        System.exit(0);
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
