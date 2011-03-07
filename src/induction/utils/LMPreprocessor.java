package induction.utils;

import fig.basic.IOUtils;
import fig.basic.LogInfo;
import induction.Utils;
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
 * and </s> at the end. All instances of numbers are replaced with the keyword
 * <num>
 * @author konstas
 */
public class LMPreprocessor
{
    String target, source, HEADER = "", fileExtension;
    int ngramSize;
    BufferedOutputStream bos;
    SimpleTokenizer tokenizer;
    public enum SourceType {PATH, LIST};
    SourceType type;

    public LMPreprocessor(String targetFile, String sourceDir, int ngramSize, 
                          SourceType type, String fileExtension)
    {
        this.target = targetFile;
        this.source = sourceDir;
        this.ngramSize = ngramSize;
        this.tokenizer = new SimpleTokenizer();
        this.type = type;
        this.fileExtension = fileExtension;
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
                else
                    addList(source);
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

    protected void processFile(String path)
    {       
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = "", textInOneLine = "", textOut = HEADER;
            while((line = br.readLine()) != null)
            {
                textInOneLine += line + " ";
            }
//            for(String s : tokenizer.tokenize(textInOneLine.trim()))
            for(String s : textInOneLine.trim().split(" "))
            {
                textOut += (s.matches("-\\p{Digit}+|" + // negative numbers
                             "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                             (s.matches("\\p{Digit}+") && // numbers
                              !(s.contains("am") || s.contains("pm"))) // but not hours!
                       ) ?  "<num> " : s + " ";
            }            
            br.close();
            bos.write((textOut.trim().toLowerCase() + " </s>\n").getBytes());
        }
        catch(IOException ioe)
        {
            LogInfo.error("Error reading file " + path);
        }
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
                    out += (s.matches("-\\p{Digit}+|" + // negative numbers
                                 "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                                 (s.matches("\\p{Digit}+") && // numbers
                                  !(s.contains("am") || s.contains("pm"))) // but not hours!
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
        String source = "robocupLists/robocupAllPathsTrain";
        String target = "robocupLM/robocup-all-3-gram.sentences";
        String fileExtension = "text";
        boolean tokeniseOnly = false;
        int ngramSize = 3;
        LMPreprocessor lmp = new LMPreprocessor(target, source, ngramSize, 
                                              SourceType.LIST, fileExtension);
        lmp.execute(tokeniseOnly);
    }
}
