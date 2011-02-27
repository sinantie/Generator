package induction.utils;

import induction.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author sinantie
 */
public class ExtractSemanticAlignments
{
    private final String INPUT_EXT, REF_EXT, OUTPUT_EXT, PATH;

    private ExtractSemanticAlignments(String inputExt, String referenceExt,
                                      String outputExt, String path)
    {
       this.INPUT_EXT = inputExt;
       this.OUTPUT_EXT = outputExt;
       this.REF_EXT = referenceExt;
       this.PATH = path;
    }

    private void execute(String path)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                execute(path + "/" + fileStr);
            } // for
        } // if
        else if(path.endsWith(INPUT_EXT))
        {
            processFile(path);
        }
    }

    public void processFile(String inputPath)
    {
        try
        {            
            String refPath = inputPath.replaceAll(INPUT_EXT, REF_EXT);
            FileOutputStream outputWriter = new FileOutputStream(
                    new File(inputPath.replaceAll(INPUT_EXT, OUTPUT_EXT)));

            // Copy lines of input file to a list
            String[] inputLines = Utils.readLines(inputPath);

            // Choose the events indicated in the reference file, from the input file,
            // and write them to the output
            int eventId;
            for(String refLine : Utils.readLines(refPath))
            {
                String[] refEvents = refLine.split(" ");
                for(int i = 1; i < refEvents.length; i++) // ignore the first token
                {
                    eventId = Integer.valueOf(refEvents[i]);
                    if(eventId > -1) // if it exists in the input file
                        outputWriter.write((inputLines[eventId] + "\n").getBytes());
                }
            }
            outputWriter.close();
        }
        catch(IOException ioe)
        {
            System.err.println("Error reading file " + inputPath);
        }
    }

    public static void main(String[] args)
    {
        String path = "data/robocup-data/2004final-percy/";
        String inputExt = ".events", referenceExt = ".align", outputExt = ".salign";
        ExtractSemanticAlignments esa = new ExtractSemanticAlignments(inputExt,
                referenceExt, outputExt, path);
        esa.execute(path);
    }
}
