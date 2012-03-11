package induction.utils;

import fig.basic.IOUtils;
import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.Event3Model;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * Parses an input list or path of text files and outputs each to a given output file,
 * each text entry per line, with <code>ngramSize</code> <s> at the beginning,
 * and </s> at the end. All instances of numbers may be replaced with the keyword
 * <num>
 * @author konstas
 */
public class LMPreprocessor
{
    String target, source, HEADER = "", fileExtension;
    int ngramSize;
    BufferedOutputStream bos;
    SimpleTokenizer tokenizer;
    
    public enum SourceType {PATH, LIST, FILE};
    SourceType type;
    boolean replaceNumbers, toLowerCase, stripWords;

    public LMPreprocessor(String targetFile, String sourceDir, int ngramSize, 
                          SourceType type, String fileExtension, 
                          boolean replaceNumbers, boolean toLowerCase,
                          boolean stripWords)
    {
        this.target = targetFile;
        this.source = sourceDir;
        this.ngramSize = ngramSize;
        this.tokenizer = new SimpleTokenizer();
        this.type = type;
        this.fileExtension = fileExtension;
        this.replaceNumbers = replaceNumbers;
        this.toLowerCase = toLowerCase;
        this.stripWords = stripWords;
    }

    public void execute(boolean tokeniseOnly)
    {
        try
        {
            for(int i = 0; i < ngramSize - 1; i++)
            {
                HEADER += "<s> ";
            }
            bos = new BufferedOutputStream(new FileOutputStream(target));
            if(tokeniseOnly)
                tokeniseSource();
            else
            {
                if(type == SourceType.PATH)
                    addPath(source);
                else if(type == SourceType.LIST)
                    addList(source);
                else
                    processExamplesInSingleFile(source);
            }
            bos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parses a file that contains a list of the files to be processed
     * @param path
     */
    private void addList(String path)
    {
        for(String line : Utils.readLines(path))
        {
            addPath(line);
        } // for
    }

    /**
     * Recursively processes files under the <code>path</code> directory
     * @param path
     */
    private void addPath(String path)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                addPath(path + "/" + fileStr);
            } // for
        } // if
        else
        {
            if(!file.getName().endsWith(fileExtension))
            {
                path = IOUtils.stripFileExt(path) + "." + fileExtension;
            }
            processFile(path);
        }
    }

    private void processExamplesInSingleFile(String source)
    {
        if(new File(source).exists())
        {
            String key = null;
            StringBuilder str = new StringBuilder();
            for(String line : Utils.readLines(source))
            {
                if(line.startsWith("Example_"))
                {
                    if(key != null) // only for the first example
                    {                      
                        processEventExample(str.toString());
                        str = new StringBuilder();
                    }
                    key = line;
                } // if                   
                str.append(line).append("\n");
            }  // for           
            processEventExample(str.toString()); // don't forget last example
        }
    }
    
    protected void processFile(String path)
    {       
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = "", textInOneLine = "";
            while((line = br.readLine()) != null)
            {
                textInOneLine += line + " ";
            }
            br.close();
            writeToFile(processExample(textInOneLine));
        }
        catch(IOException ioe)
        {
            LogInfo.error("Error reading file " + path);
        }
    }
    
    protected void processEventExample(String input)
    {
        try
        {
            writeToFile(processExample(Event3Model.extractExampleFromString(input)[1]));
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.getMessage());
        }        
    }
    
    protected String processExample(String input)
    {
        StringBuilder textOut = new StringBuilder(HEADER);
        for(String s : input.trim().split(" "))
        {
            String word = stripWords ? Utils.stripWord(s, true) : s;            
            textOut.append( (replaceNumbers && (word.matches("-\\p{Digit}+|" + // negative numbers
                         "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                         (word.matches("\\p{Digit}+") && // numbers
                          !(word.contains("am") || word.contains("pm")))) // but not hours!
                   ) ?  "<num> " : word).append(" ");            
        }
        return textOut.toString();
    }
    
    protected void writeToFile(String input) throws IOException
    {
        bos.write(( (toLowerCase ? input.trim().toLowerCase() : input.trim())
                  + " </s>\n").getBytes());
    }
    
    /**
     * Tokenises source file. Assumes <s> and </s> are already placed in source
     */
    public void tokeniseSource()
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(source));
            String line = "", out = "";
            while((line = br.readLine()) != null)
            {
                out = "";
                for(String s : tokenizer.tokenize(line.substring(HEADER.length(), line.length() - 5))) // ignore <s>'s and </s>
                {
                    // tokenisation might give numbers not found previously
                    out += (replaceNumbers && (s.matches("-\\p{Digit}+|" + // negative numbers
                                 "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                                 (s.matches("\\p{Digit}+") && // numbers
                                  !(s.contains("am") || s.contains("pm")))) // but not hours!
                           ) ?  "<num> " : s + " ";
                }
                bos.write((HEADER + out + " </s>\n").getBytes());
            }
            br.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args)
    {
//        String source = "/home/konstas/EDI/candc/candc-1.00/atis5000.sents.full.tagged.CDnumbers";
//        String target = "/home/konstas/EDI/candc/candc-1.00/atis5000.sents.full.tagged.CDnumbers.tags_only.sentences";
        String source = "gaborLists/trainListPathsGabor";
        String target = "weatherGovLM/gabor-srilm-abs-3-gram.model.tagged.sentences";
        String fileExtension = "text.tagged";
        boolean tokeniseOnly = false, replaceNumbers = true, toLowerCase = false, stripWords = false;
        int ngramSize = 3;
        LMPreprocessor lmp = new LMPreprocessor(target, source, ngramSize, 
                                                SourceType.LIST, fileExtension, 
                                                replaceNumbers, toLowerCase, stripWords);
        lmp.execute(tokeniseOnly);
    }
}
