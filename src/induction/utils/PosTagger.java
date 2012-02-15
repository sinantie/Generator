package induction.utils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fig.basic.IOUtils;
import induction.MyCallable;
import induction.Utils;
import induction.problem.event3.Event3Model;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author konstas
 */
public class PosTagger
{
    private final String path;
    private String extension = null;
    private MaxentTagger tagger;
    private Set<String> taggedVocabulary;
    private enum Type {DIRECTORY, FILE_EVENTS_FORMAT, FILE_RAW};
    private final Type typeOfFile;

    public PosTagger(String path, String pathsOrFile)
    {
        this.path = path;
        if(pathsOrFile.equals("directory"))
            typeOfFile = Type.DIRECTORY;
        else if(pathsOrFile.equals("file_events"))
            typeOfFile = Type.FILE_EVENTS_FORMAT;
        else
            typeOfFile = Type.FILE_RAW;
        taggedVocabulary = new HashSet<String>();
        try
        {
            tagger = new MaxentTagger("lib/models/bidirectional-distsim-wsj-0-18.tagger");
        }
        catch(Exception ioe)
        {
            System.out.println("Error loading tagger model");
            System.exit(1);
        }
    }

    public PosTagger(String path, String pathsOrFile, String extension)
    {
        this(path, pathsOrFile);
        this.extension = extension;
    }

    public void execute()
    {
        try
        {
            List<String> corpus; 
            if(typeOfFile == Type.DIRECTORY)
            {
                corpus = readFromPath();
            }
            else if(typeOfFile == Type.FILE_EVENTS_FORMAT)
            {
                corpus = readFromSingleFile(true);
            }
            else
                corpus = readFromSingleFile(false);
            
            int counter = 0;
            for(String example : corpus)
            {                
                parse(example);               
                if(counter++ % 1000 == 0)
                    System.out.println("Processed " + counter + " examples");                
            }
           

            FileOutputStream fos = new FileOutputStream(path + "_vocabulary");
            System.out.println("Writing vocabulary to disk...");
            for(String word : taggedVocabulary)
            {
                fos.write((word + "\n").getBytes());
            }
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private List<String> readFromPath() throws IOException
    {
        List<String> out = new ArrayList<String>();
        BufferedReader fin = new BufferedReader(new FileReader(path));
        String line = "";            
        while((line = fin.readLine()) != null)
        {
            if(extension != null && !line.endsWith(extension))
            {
                out.add(readFile(IOUtils.stripFileExt(line) + "." + extension));
            }              
            else
            {
                out.add(readFile(line));
            }               
        }
        fin.close();
        return out;
    }
    
    private List<String> readFromSingleFile(boolean events) throws IOException
    {
        List<String> out = new ArrayList<String>();        
        if(new File(path).exists())
        {
            if(events)
            {
                String key = null;
                StringBuilder str = new StringBuilder();
                for(String line : Utils.readLines(path))
                {
                    if(line.startsWith("Example_"))
                    {
                        if(key != null) // only for the first example
                        {                      
                            out.add(readExample(str.toString()));
                            str = new StringBuilder();
                        }
                        key = line;
                    } // if                   
                    str.append(line).append("\n");
                }  // for           
                out.add(readExample(str.toString())); // don't forget last example
            } // if
            else
            {
                out.addAll(Arrays.asList(Utils.readLines(path)));
            }
        } // if        
        return out;
    }
    
    private String readExample(String input)
    {
        String[] res = Event3Model.extractExampleFromString(input); // res[0] = name, res[1] = text
        return res[1];
    }
    private String readFile(String path) throws IOException
    {
        InputStream in = new FileInputStream(new File(path));
        OutputStream out = new ByteArrayOutputStream();
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        return out.toString().toLowerCase().trim();
    }

    private void parse(String input)
    {        
        String taggedInput = tag(input);
        taggedVocabulary.addAll(Arrays.asList(taggedInput.split("\\p{Space}")));        
    }

    public String tag(String input)
    {
        return tagger.tagString(input);
    }

    public static void main(String[] args)
    {
        if(args.length > 3)
        {
            System.err.println("Usage: file_with_paths use_paths{directory,file_events,file_raw} [extension]");
            System.exit(1);
        }
        PosTagger pos;
        if(args.length > 1)
            pos = new PosTagger(args[0], args[1], args[2]);
        else
            pos = new PosTagger(args[0], args[1]);

        pos.execute();
    }
    
    protected class Worker extends MyCallable
    {
        String example;

        public Worker(String example)
        {
            this.example = example;
        }
                
        @Override
        public Object call() throws Exception
        {
            parse(example);
            return null;
        }
    }
}
