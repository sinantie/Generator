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
                out.print(example.exportToEdusFormat(
                        clean(recordAlignments[i++].split(" "), example.getTextInOneLine())));
            }
            
            out.close();
        } catch (Exception ioe)
        {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
    /**
     * Record alignments cleansing. 
     * 1. Remove records aligning to one word.
     * 2. 
     * @param alignments
     * @return 
     */
    private String[] clean(String[] alignments, String[] words)
    {
        return fixSplitSentenceAlignments(removeRecordsWithOneWord(alignments), words);                
    }
    
    /**
     * We assume that a record alignment should span up to the end of a sentence.
     * If a record alignment spans phrases from two adjacent sentences (separated with full-stop)
     * then keep the alignment up to the full-stop and assign the rest words of the phrase
     * to the next record alignment.
     * For examples, if we have the record alignment, 11 11 11 11 11 5 5, and the
     * third word is a full-stop then change the alignments to, 11 11 11 5 5 5 5.
     * 
     * @param alignments
     * @param words
     * @return 
     */
    private String[] fixSplitSentenceAlignments(String[] alignments, String[] words)
    {
        String[] out = new String[alignments.length];
        for(int i = 0; i < words.length; i++)
        {
            if(Utils.isSentencePunctuation(words[i])) // found a sentence delimiter
            {
                if(i < words.length - 1 && (alignments[i].equals(alignments[i + 1]))) // the alignment span crosses the delimiter
                {
                    out[i] = alignments[i]; // copy the delimiter record alignment
                    // find the next record alignment
                    int j;
                    for(j = i + 1; j < words.length; j++)
                    {
                        if(!alignments[j].equals(alignments[i]))
                            break;
                    }
                    for(int k = i + 1; k < j; k++) // replace the crossing alignments with the next record
                    {
                        out[k] = alignments[j];
                    }
                    i = j; // continue with the rest of the string
                } // if
            } // if
            out[i] = alignments[i];
        }                
        return out;
    }
    
    /**
     * Remove records aligning to one word. 
     * TODO: If the single word is punctuation, then always get the record alignment
     * of the previous word
     * @param alignments
     * @return 
     */
    private String[] removeRecordsWithOneWord(String[] alignments)
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
//        String inputPath = "data/weatherGov/weatherGovTrainGabor.gz";
        String inputPath = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2";
//        String inputPathRecordAlignments = "results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.train.pred.14.sorted";
        String inputPathRecordAlignments = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRulesPredAlign_modified2";
//        String outputFile = "data/weatherGov/weatherGovTrainGaborEdusAlignedRemovedSingleRecord.gz";
        String outputFile = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2_EdusAligned";
        System.out.println("Creating " + outputFile);
        new ExportExamplesToEdusFile(inputPath, inputPathRecordAlignments, outputFile).execute();        
    }
}
