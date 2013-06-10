package induction.utils;

import fig.basic.IOUtils;
import induction.Utils;
import induction.problem.event3.Event3Example;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create dataset file that contains text and elemantary discourse units (EDUs). 
 * The boundaries for the segmentation of the text are taken from alignment data,
 * extracted either manually or automatically. 
 * 
 * Accepts dataset in single file only.
 * 
 * Write examples following the event3 ver.2 format, which contains headers 
 * denoting the start of text, and EDUs.
 * @author konstas
 */
public class ExportExamplesToEdusFile
{    
    private String inputPath, recordAlignmentsPath, outputFile;
   
    public ExportExamplesToEdusFile(String path, String recordAlignmentsPath, 
            String outputFile)
    {
        this.inputPath = path;               
        this.recordAlignmentsPath = recordAlignmentsPath;
        this.outputFile = outputFile;        
    }

    public void execute()
    {        
        try
        {                        
            PrintWriter out = IOUtils.openOutEasy(outputFile);
            List<Event3Example> examples = Utils.readEvent3Examples(inputPath, true); 
            String[] recordAlignments = Utils.readLines(recordAlignmentsPath);
            int i = 0;
            for(Event3Example example : examples)
            {                
                out.print(example.exportToEdusFormat(recordAlignments[i++]));
            }
            
            out.close();
        } catch (Exception ioe)
        {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {        
        // trainListPathsGabor, genDevListPathsGabor, genEvalListPathsGabor
        String inputPath = "data/weatherGov/weatherGovTrainGabor.gz";
        String inputPathRecordAlignments = "results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.train.pred.14.sorted";
        String outputFile = "data/weatherGov/weatherGovTrainGaborEdusAligned.gz";
        System.out.println("Creating " + outputFile);
        new ExportExamplesToEdusFile(inputPath, inputPathRecordAlignments, outputFile).execute();        
    }
}
