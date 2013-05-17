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
 * Compile corpus from many files (.events, .text, .align) to a single file.
 * We assume that the corpus consists of nested folders, and that the leaf 
 * folders contain at least two files, containing the events, text and optionally alignment.
 * 
 * Write examples following the event3 ver.2 format, which contains headers 
 * denoting the start of text, events, alignments and (optionally) record trees.
 * @author konstas
 */
public class ExportExamplesToSingleFile
{
    public static enum Type {DIR, FILES, SINGLE_FILE};
    private static final String EVENTS_EXT = ".events",
            TEXT_EXT = ".text",
            ALIGN_EXT = ".align",
            TAGGED_TEXT = ".text.tagged";
    private String inputPath, outputFile;
    private Map<String, String> treebankMap;
    private boolean inputPosTagged;
    private Type inputType;

    public ExportExamplesToSingleFile(String path, String treebankInputFile, 
            String outputFile, boolean inputPosTagged, Type inputType)
    {
        this.inputPath = path;        
        if(treebankInputFile != null)
        {
            this.treebankMap = new HashMap<String, String>();
            readTreebankFile(treebankInputFile, treebankMap);
        }
        this.outputFile = outputFile;
        this.inputPosTagged = inputPosTagged;
        this.inputType = inputType;
    }

    public void execute()
    {        
        try
        {                        
            PrintWriter out = IOUtils.openOutEasy(outputFile);
            switch(inputType)
            {
                case DIR : addPath(inputPath, out); break;
                case FILES : addPath(Utils.readLines(inputPath), out); break;
                case SINGLE_FILE : addPath(Utils.readEvent3Examples(inputPath, true), out); break;
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
    public static void readTreebankFile(String treebankInputFile, Map<String, String> map)
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

    private void addPath(String[] lines, PrintWriter out) throws IOException
    {
        for (String line : Utils.readLines(inputPath))
        {
            addPath(line, out);
        }
    }
    
    private void addPath(List<Event3Example> examples, PrintWriter out) throws IOException
    {
        for(Event3Example example : examples)
        {
            writeFile(example, out);
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
            String entry = treebankMap.get(stripped);            
            out.print(entry != null ? entry : "N/A\n");
        }
        if(new File(stripped + ALIGN_EXT).exists()) // write alignments
        {
            out.println("$ALIGN");
            out.print(Utils.readFileAsString(stripped + ALIGN_EXT));
        } // write text                    
    }
    
    private void writeFile(Event3Example example, PrintWriter out) throws IOException
    {
        // set record tree
        String entry = treebankMap.get(example.getName());            
        example.setTree(entry != null ? entry : "N/A\n");
        out.println(example.toString());
    }

    public static void main(String[] args)
    {        
        // trainListPathsGabor, genDevListPathsGabor, genEvalListPathsGabor
        String inputPath[] = {
                              "data/weatherGov/weatherGovTrainGabor.gz", 
//                              "data/weatherGov/weatherGovTrainGaborPosTagged.gz", 
                              "data/weatherGov/weatherGovGenDevGabor.gz", 
                              "data/weatherGov/weatherGovGenEvalGabor.gz"
                             };
        // recordTreebankTrainRightBinarize, recordTreebankGenDevRightBinarize, recordTreebankGenEvalRightBinarize
        String treebankInputFile[] = {
//                                      "data/weatherGov/treebanks/final/recordTreebankTrainRightBinarizeAlignmentsThres5",
//                                      "data/weatherGov/treebanks/final/recordTreebankTrainRightBinarizeAlignmentsTreebank",
                                      "data/weatherGov/treebanks/ccm/recordTreebankTrainCcm",
                                      "data/weatherGov/treebanks/recordTreebankGenDevRightBinarizeUnaryRules",
                                      "data/weatherGov/treebanks/recordTreebankGenEvalRightBinarizeUnaryRules"
                                     };
        // weatherGovTrainGabor.gz, weatherGovGenDevGabor.gz, weatherGovGenEvalGabor.gz
        String outputFile[] = {
//                               "data/weatherGov/weatherGovTrainGaborRecordTreebankTrainRightBinarizeAlignmentsThres5.gz",
//                               "data/weatherGov/weatherGovTrainGaborRecordTreebankTrainRightBinarizeAlignmentsTreebank_PosTagged.gz",
                               "data/weatherGov/weatherGovTrainGaborRecordTreebankCcm.gz",
                               "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules.gz",
                               "data/weatherGov/weatherGovGenEvalGaborRecordTreebankUnaryRules.gz"
                              };                
        Type inputType = Type.SINGLE_FILE;
        boolean inputPosTagged = false;
        for(int i = 0; i < 1; i++)
        {
            System.out.println("Creating " + outputFile[i]);
            new ExportExamplesToSingleFile(inputPath[i], treebankInputFile[i], outputFile[i], inputPosTagged, inputType).execute();
        }
    }
}
