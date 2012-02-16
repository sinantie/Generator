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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Set<String> taggedVocabulary, syncVocabulary;
    private enum Type {DIRECTORY, FILE_EVENTS_FORMAT, FILE_RAW};
    private final Type typeOfFile;
    private boolean useUniversalTags = false;
    private Map<String, String> universalMaps;
    
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
        syncVocabulary = Collections.synchronizedSet(taggedVocabulary);
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

    public PosTagger(String path, String pathsOrFile, String useUniversalTagsStr)
    {
        this(path, pathsOrFile);
        this.useUniversalTags = useUniversalTagsStr.equals("useUniversalTags");
        if(this.useUniversalTags)
        {
            universalMaps = new HashMap<String, String>();
            String[] lines = Utils.readLines("lib/universal_pos_tags/en-ptb.map");
            for(String line : lines)
            {
                String[] ar = line.split("\t");
                universalMaps.put(ar[0], ar[1]);
            }
        }
    }
    
    public PosTagger(String path, String pathsOrFile, String useUniversalTagsStr, String extension)
    {
        this(path, pathsOrFile, useUniversalTagsStr);
        this.extension = extension;        
    }

    public void execute()
    {
        try
        {
            List<String> examples; 
            if(typeOfFile == Type.DIRECTORY)
            {
                examples = readFromPath();
            }
            else if(typeOfFile == Type.FILE_EVENTS_FORMAT)
            {
                examples = readFromSingleFile(true);
            }
            else
                examples = readFromSingleFile(false);
            Collection<Worker> list = new ArrayList<Worker>(examples.size());
            for(int i = 0; i < examples.size(); i++)
                list.add(new Worker(i, examples.get(i)));
            Utils.parallelForeach(Runtime.getRuntime().availableProcessors(), list);
//            Utils.parallelForeach(1, list);
//            int counter = 0;
//            for(String example : examples)
//            {                
//                parse(example);               
//                if(counter++ % 1000 == 0)
//                    System.out.println("Processed " + counter + " examples");                
//            }
           

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
        if(!useUniversalTags)
            syncVocabulary.addAll(Arrays.asList(taggedInput.split("\\p{Space}")));        
        else
        {
            String[] tokens = taggedInput.split("\\p{Space}");
            for(String token : tokens)
            {
                int index = token.lastIndexOf("/");
                syncVocabulary.add(token.substring(0, index + 1) + 
                                   universalMaps.get(token.substring(index + 1)));
            }
        }
    }

    public String tag(String input)
    {
        return tagger.tagString(input);
    }

    public static void main(String[] args)
    {
        if(args.length > 3)
        {
            System.err.println("Usage: file_with_paths use_paths{directory,file_events,file_raw} "
                             + "[useUniversalTags extension]");
            System.exit(1);
        }
        PosTagger pos;
        if(args.length == 3)
            pos = new PosTagger(args[0], args[1], args[2]);
        else if(args.length == 4)
            pos = new PosTagger(args[0], args[1], args[2], args[3]);
        else
            pos = new PosTagger(args[0], args[1]);

        pos.execute();
    }
    
    protected class Worker extends MyCallable
    {
        int counter;
        String example;

        public Worker(int counter, String example)
        {
            this.example = example;
            this.counter = counter;
        }
                
        @Override
        public Object call() throws Exception
        {
            parse(example);               
            if(counter++ % 1000 == 0)
                System.out.println("Processed " + counter + " examples");              
            return null;
        }
    }
}
