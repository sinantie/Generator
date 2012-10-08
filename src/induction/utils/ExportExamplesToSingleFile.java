package induction.utils;

import fig.basic.IOUtils;
import induction.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> treebankMap;
    private boolean isDirectory, inputPosTagged;

    public ExportExamplesToSingleFile(String path, String treebankInputFile, 
            String outputFile, boolean isDirectory, boolean inputPosTagged)
    {
        this.inputPath = path;        
        if(treebankInputFile != null)
        {
            this.treebankMap = new HashMap<String, String>();
            readTreebankFile(treebankInputFile, treebankMap);
        }
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

    /**
     * Read treebank data from file. We assume each example starts with the name
     * in the first line, then follows the tree and ends with an empty line
     * @param treebankInputFile
     * @param map 
     */
    private void readTreebankFile(String treebankInputFile, Map<String, String> map)
    {                
        String lines[] = Utils.readLines(treebankInputFile);        
        StringBuilder str = new StringBuilder();
        String key = Utils.stripExtension(lines[0]); // key could be a filename
        for(int i = 1; i < lines.length; i++)
        {
            if(lines[i].equals(""))
            {
                map.put(key, str.toString());
                str = new StringBuilder();
                if(i < lines.length - 1)
                    key = Utils.stripExtension(lines[++i]);
            }
            else
            {
                str.append(lines[i]).append("\n");
            }            
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
        if(treebankMap != null) // write record tree
        {
            out.println("$RECORD_TREE");
            out.print(treebankMap.get(stripped));
        }
        if(new File(stripped + ALIGN_EXT).exists()) // write alignments
        {
            out.println("$ALIGN");
            out.print(Utils.readFileAsString(stripped + ALIGN_EXT));
        } // write text                    
    }

    public static void main(String[] args)
    {
        // trainListPathsGabor, genDevListPathsGabor, genEvalListPathsGabor
        String inputPath[] = {
                              "gaborLists/trainListPathsGabor", 
                              "gaborLists/genDevListPathsGabor", 
                              "gaborLists/genEvalListPathsGabor"
                             };
        // recordTreebankTrainRightBinarize, recordTreebankGenDevRightBinarize, recordTreebankGenEvalRightBinarize
        String treebankInputFile[] = {
                                      "data/weatherGov/treebanks/recordTreebankTrainRightBinarize",
                                      "data/weatherGov/treebanks/recordTreebankGenDevRightBinarize",
                                      "data/weatherGov/treebanks/recordTreebankGenEvalRightBinarize"
                                     };
        // weatherGovTrainGabor.gz, weatherGovGenDevGabor.gz, weatherGovGenEvalGabor.gz
        String outputFile[] = {
                               "data/weatherGov/weatherGovTrainGaborRecordTreebank.gz",
                               "data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz",
                               "data/weatherGov/weatherGovEvalGaborRecordTreebank.gz"
                              };        
        boolean isDirectory = false;
        boolean inputPosTagged = false;
        for(int i = 0; i < 3; i++)
            new ExportExamplesToSingleFile(inputPath[i], treebankInputFile[i], outputFile[i], isDirectory, inputPosTagged).execute();
    }
}
