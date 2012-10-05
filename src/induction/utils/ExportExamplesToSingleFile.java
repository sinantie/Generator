package induction.utils;

import fig.basic.IOUtils;
import induction.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Compile corpus from many files (.events, .text, .align) to a single file.
 * We assume that the corpus consists of nested folders, and that the leaf 
 * folders contain at least two files, containing the events, text and optionally alignment.
 * 
 * Write examples following the event3 ver.2 format, which contains headers 
 * denoting the start of text, events, alignments.
 * @author konstas
 */
public class ExportExamplesToSingleFile
{

    private static final String EVENTS_EXT = ".events",
            TEXT_EXT = ".text",
            ALIGN_EXT = ".align",
            TAGGED_TEXT = ".text.tagged";
    private String inputPath, outputFile;
    private boolean isDirectory, inputPosTagged;

    public ExportExamplesToSingleFile(String path, String outputFile, boolean isDirectory, boolean inputPosTagged)
    {
        this.inputPath = path;
        this.outputFile = outputFile;
        this.isDirectory = isDirectory;
        this.inputPosTagged = inputPosTagged;
    }

    public void execute()
    {
        try
        {
            PrintWriter out = IOUtils.openOutEasy(outputFile);
            if (isDirectory)
            {
                addPath(inputPath, out);
            }
            else // input is a list of files
            {
                for (String line : Utils.readLines(inputPath))
                {
                    addPath(line, out);
                }
            }

            out.close();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private void addPath(String path, PrintWriter out) throws IOException
    {
        File file = new File(path);
        if (file.isDirectory())
        {
            for (String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                addPath(path + "/" + fileStr, out);
            } // for
        } // if
        else if (validName(path))
        {
            writeFile(path, out);
        }
    }

    private boolean validName(String path)
    {
        return (path.endsWith(EVENTS_EXT))
                && !path.startsWith("#");
    }
    
    private void writeFile(String path, PrintWriter out) throws IOException
    {
        String stripped = Utils.stripExtension(path);
        out.println("$NAME");
        out.println(stripped); // write name        
        out.println("$TEXT");
        out.print(Utils.readFileAsString(stripped + (inputPosTagged ? TAGGED_TEXT : TEXT_EXT) )); // write text
        out.println("$EVENTS");
        out.print(Utils.readFileAsString(path)); // write events
        if(new File(stripped + ALIGN_EXT).exists()) // write alignments
        {
            out.println("$ALIGN");
            out.print(Utils.readFileAsString(stripped + ALIGN_EXT));
        } // write text
            
    }

    public static void main(String[] args)
    {
        // trainListPathsGabor, genDevListPathsGabor, genEvalListPathsGabor
        String inputPath = "gaborLists/trainListPathsGabor";
        // weatherGovTrainGabor.gz, weatherGovGenDevGabor.gz, weatherGovGenEvalGabor.gz
        String outputFile = "data/weatherGov/weatherGovTrainGabor.gz";
        boolean isDirectory = false;
        boolean inputPosTagged = false;
        ExportExamplesToSingleFile e = new ExportExamplesToSingleFile(
                inputPath, outputFile, isDirectory, inputPosTagged);
        e.execute();
    }
}
