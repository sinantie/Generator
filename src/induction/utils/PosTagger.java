package induction.utils;

import uk.co.flamingpenguin.jewel.cli.Option;
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
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import static uk.co.flamingpenguin.jewel.cli.CliFactory.parseArguments;

/**
 *
 * @author konstas
 */
public class PosTagger
{
    private final String path;
    private FileOutputStream fileOutputStream;
    private String extension;
    private MaxentTagger tagger;
    private Set<String> taggedVocabulary, syncVocabulary;
    // option 'list' means that 'path' is a list of filenames that contain
    // a single example (either raw or event by Percy's format)
    // option file means that 'path' is a file that contains a list 
    // of all examples (either raw sentences or events by Percy's format)
    public enum TypeOfPath {list, file}
    public enum TypeOfInput {events, raw}
    private final TypeOfPath typeOfPath;
    private final TypeOfInput typeOfInput;
    private boolean useUniversalTags = false;
    private Map<String, String> universalMaps;       
    private Map<String, List<String>> posDictionary;
    
    public PosTagger(String path, TypeOfPath typeOfPath, TypeOfInput typeOfInput, 
                     String posDictionaryPath, boolean useUniversalTags, String extension)
    {
        this.path = path;
        try
        {
            this.fileOutputStream = new FileOutputStream(path + ".tagged");
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
        this.typeOfPath = typeOfPath;
        this.typeOfInput = typeOfInput;        
        this.useUniversalTags = useUniversalTags;
        this.extension = extension;
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
        if(!posDictionaryPath.equals(""))
        {
            posDictionary = readPosDictionary(posDictionaryPath);
        }
        else
        {
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
        taggedVocabulary = new HashSet<String>();
        syncVocabulary = Collections.synchronizedSet(taggedVocabulary);        
    }

    public void execute()
    {
        try
        {
            List<String[]> examples = null; 
            if(typeOfPath == TypeOfPath.list)            
                examples = readFromList();
            else if(typeOfPath == TypeOfPath.file)                                    
                examples = readExamplesFromSingleFile();
            else
            {
                System.err.println("Invalid argument");
                return;
            }
            Collection<Worker> list = new ArrayList<Worker>(examples.size());
            for(int i = 0; i < examples.size(); i++)
                list.add(new Worker(i, examples.get(i)));
//            Utils.parallelForeach(Runtime.getRuntime().availableProcessors(), list);           
            Utils.parallelForeach(1, list);
            fileOutputStream.close();
            // save vocabulary to disk
            if(posDictionary != null)
            {
                FileOutputStream fos = new FileOutputStream(path + ".vocabulary");
                System.out.println("Writing vocabulary to disk...");
                for(String word : taggedVocabulary)
                {
                    fos.write((word + "\n").getBytes());
                }
                fos.close();
            }            
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Read a list of files that contain a single example each
     * @return
     * @throws IOException 
     */
    private List<String[]> readFromList() throws IOException
    {
        List<String[]> out = new ArrayList<String[]>();
        BufferedReader fin = new BufferedReader(new FileReader(path));
        String line = "";            
        while((line = fin.readLine()) != null)
        {
            if(!extension.equals("") && !line.endsWith(extension))
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
    
    /**
     * Read examples from  a single file
     * @return
     * @throws IOException 
     */
    private List<String[]> readExamplesFromSingleFile() throws IOException
    {
        List<String[]> out = new ArrayList<String[]>();        
        if(new File(path).exists())
        {
            if(typeOfInput == TypeOfInput.events)
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
//                out.addAll(Arrays.asList(Utils.readLines(path)));
                String[] sentences = Utils.readLines(path);
                for(String sentence : sentences)
                {
                    String[] ar = {sentence};
                    out.add(ar);
                }
            }
        } // if        
        return out;
    }
    
    /**
     * Read a single example from the input string. Handle both raw and event format
     * @param input
     * @return 
     */
    private String[] readExample(String input)
    {
//        String[] res = Event3Model.extractExampleFromString(input); // res[0] = name, res[1] = text
//        return res[1];
        if(typeOfInput == TypeOfInput.events)
            return Event3Model.extractExampleFromString(input);
        else
        {
            String[] out = {input};
            return out;
        }
    }
    
    /**
     * Open file and read the example. The file should only contain a single example
     * @param path
     * @return
     * @throws IOException 
     */
    private String[] readFile(String path) throws IOException
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

        return readExample(out.toString().toLowerCase().trim());
    }

    private Map<String, List<String>> readPosDictionary(String path)
    {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for(String line : Utils.readLines(path))
        {
            int indexOfDelimeter = line.lastIndexOf("/");
            String word = line.substring(0, indexOfDelimeter);
            String tag = line.substring(indexOfDelimeter + 1);
            if(!map.containsKey(word))
            {
                ArrayList tags = new ArrayList<String>(1);
                tags.add(tag);
                map.put(word, tags);
            }
            else
            {
                map.get(word).add(tag);
            }
        }
        return map;
    }
    
    private void parse(String[] example)
    {
        String taggedText = tag(example);
        try // TO-DO: needs a seperate thread, as it will eventually slow down seperate workers...
        {
            saveToFile(example, taggedText);
        }
        catch(IOException ioe){
            System.err.println(ioe.getMessage());
        }
        if(posDictionary == null) // update vocabulary, unless we use one to pos tag
        {
            if(!useUniversalTags)
                syncVocabulary.addAll(Arrays.asList(taggedText.split("\\p{Space}")));        
            else
            {
                String[] tokens = taggedText.split("\\p{Space}");
                for(String token : tokens)
                {
                    int index = token.lastIndexOf("/");
                    syncVocabulary.add(token.substring(0, index + 1) + 
                                       universalMaps.get(token.substring(index + 1)));
                }
            } // else
        } // if
        
    }

    private void saveToFile(String[] example, String taggedText) throws IOException
    {        
        synchronized(fileOutputStream)
        {
            if(typeOfInput == TypeOfInput.raw)
                fileOutputStream.write((taggedText + "\n").getBytes());
            else
            {
                StringBuilder str = new StringBuilder();
                for(int i = 0; i < example.length; i++)
                {
                    if(i == 1)
                        str.append(taggedText).append("\n");
                    else
                        str.append(example[i]).append("\n");
                }
                fileOutputStream.write(str.toString().getBytes());
            } // else
        }
    }
    
    public String tag(String[] example)
    {
        String input; 
        if(typeOfInput == TypeOfInput.raw)
            input = example[0];
        else
            input = example[1]; // res[0] = name, res[1] = text
        String taggedText;
        if(posDictionary == null) // use trained pos tagger
        {
            taggedText = tagger.tagString(input);            
            if(!useUniversalTags) // only update word/tag vocabulary
                syncVocabulary.addAll(Arrays.asList(taggedText.split("\\p{Space}")));        
            else
            {
                String[] tokens = taggedText.split("\\p{Space}");
                StringBuilder taggedTextBuilder = new StringBuilder();
                for(String token : tokens)
                {                    
                    String ar[] = splitWordTagToken(token);
                    String wordTagUniv = String.format("%s/%s", ar[0], universalMaps.get(ar[1]));                            
                    taggedTextBuilder.append(wordTagUniv).append(" ");
                    syncVocabulary.add(wordTagUniv); // update word/tag vocabulary
                }
                taggedText = taggedTextBuilder.substring(0, taggedTextBuilder.length() - 1);
            } // else
        } // if
        else // use pos dictionary. Warn in case of ambiguity without crashing.
        {
            String[] tokens = input.split("\\p{Space}");
            StringBuilder taggedTextBuilder = new StringBuilder();
            for(String token : tokens)
            {                
                List<String> tags = posDictionary.get(token);
                if(tags.size() > 1) // found ambiguity, report it!
                {
                    taggedTextBuilder.append(token).append(" ");
                    if(typeOfInput == TypeOfInput.raw)
                        System.err.println("Ambiguity in word '" + token + "' of sentence '" + input + "'");
                    else
                        System.err.println("Ambiguity in word '" + token + 
                                "' in example " + example[0]); 
//                                +" in sentence '" + input + "'");
                }
                else                    
                    taggedTextBuilder.append(String.format("%s/%s", token, tags.get(0))).append(" ");
                    
            }
            taggedText = taggedTextBuilder.substring(0, taggedTextBuilder.length() - 1); 
        }
        return taggedText;
    }

    private String[] splitWordTagToken(String token)
    {
        String[] ar = new String[2];
        int index = token.lastIndexOf("/");
        ar[0] = token.substring(0, index);
        ar[1] = token.substring(index + 1);
        return ar;
    }
    
    public static void main(String[] args)
    {
        try
        {
            TaggerOptions opts = parseArguments(TaggerOptions.class, args);
            PosTagger pos = new PosTagger(opts.getPath(), 
                                          opts.getTypeOfPath(),
                                          opts.getTypeOfInput(),
                                          opts.getPosDictionary(),
                                          opts.isUseUniversalTags(), 
                                          opts.getExtension());
            pos.execute();
        }
        catch(ArgumentValidationException e) 
        {
            System.err.println("Usage: -path path -typeOfPath {dir,file_events,file_raw} "
                             + "[-useUniversalTags -extension ext -posDictionary path]");
            System.err.println(e.getMessage());            
        }        
    }
      
    interface TaggerOptions 
    {        
        @Option String getPath();
        @Option TypeOfPath getTypeOfPath();
        @Option TypeOfInput getTypeOfInput();
        @Option(defaultValue="") String getExtension();
        @Option(defaultValue="") String getPosDictionary();
        @Option boolean isUseUniversalTags();        
    }
    
    protected class Worker extends MyCallable
    {
        int counter;
        String[] example;

        public Worker(int counter, String[] example)
        {
            this.example = example;
            this.counter = counter;
        }
                
        @Override
        public Object call() throws Exception
        {
            parse(example);               
            if(counter++ % 10000 == 0)
                System.out.println("Processed " + counter + " examples");              
            return null;
        }
    }        
}
