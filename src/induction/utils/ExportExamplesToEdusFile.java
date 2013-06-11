package induction.utils;

import fig.basic.IOUtils;
import induction.Utils;
import induction.problem.event3.Event3Example;
import java.io.PrintWriter;
import java.util.List;

/**
 * Create dataset file that contains text and elementary discourse units (EDUs). 
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
                out.print(example.exportToEdusFormat(clean(recordAlignments[i++].split(" "))));
            }
            
            out.close();
        } catch (Exception ioe)
        {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
    private String[] clean(String[] alignments)
    {
        String[] out = new String[alignments.length];
        for(int i = 0; i < alignments.length; i++)
        {
            if(!recordWithOneWord(alignments, alignments[i], i - 1, i + 1))
                out[i] = alignments[i];
            else
                out[i] = i == 0 ? alignments[i + 1] : alignments[i - 1];
        }
        return out;
    }
    
    private boolean recordWithOneWord(String[] records, String current, int from, int to)
    {
        if(records.length == 1)
            return true;
        if(from < 0)
            return !current.equals(records[to]);
        else 
        {
            if(to >= records.length)
                return !current.equals(records[from]);
            else
                return !(current.equals(records[from]) || current.equals(records[to]));
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
