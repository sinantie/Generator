package induction.utils;

import induction.utils.PosTaggerOptions.TypeOfInput;
import induction.utils.PosTaggerOptions.TypeOfPath;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
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
    PosTaggerOptions opts;
    
    private final String path;
    private FileOutputStream fileOutputStream;
    private String extension;
    private MaxentTagger tagger;
    private Set<String> taggedVocabulary, syncVocabulary; 
    // option 'list' means that 'inputPath' is a list of filenames that contain
    // a single example (either raw or event by Percy's format)
    // option file means that 'inputPath' is a file that contains a list 
    // of all examples (either raw sentences or events by Percy's format)
    
    private final TypeOfPath typeOfPath;
    private final TypeOfInput typeOfInput;
    private boolean useUniversalTags, replaceNumbers, verbose;
    private Map<String, String> universalMaps;       
    private Map<String, List<String>> posDictionary;
    private ExecutorService writerService;
    private final int NUM_OF_THREADS = Runtime.getRuntime().availableProcessors();
//    private final int NUM_OF_THREADS = 1;
    
//    public PosTagger(String inputPath, TypeOfPath typeOfPath, TypeOfInput typeOfInput, 
//                     String posDictionaryPath, boolean useUniversalTags, boolean replaceNumbers, 
//                     String extension, boolean verbose)
    public PosTagger(PosTaggerOptions opts)
    {
        this.opts = opts;
        this.path = opts.inputPath;
        if(opts.typeOfPath == TypeOfPath.file)
        {
            try
            {
                this.fileOutputStream = new FileOutputStream(path + ".tagged");
            }
            catch(IOException ioe)
            {
                System.err.println(ioe.getMessage());
                System.exit(1);
            }
        }
        writerService = Executors.newFixedThreadPool(NUM_OF_THREADS);
        this.typeOfPath = opts.typeOfPath;
        this.typeOfInput = opts.typeOfInput;        
        this.useUniversalTags = opts.useUniversalTags;
        this.replaceNumbers = opts.replaceNumbers;
        this.extension = opts.extension;
        this.verbose = opts.verbose;
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
        if(!opts.posDictionaryPath.equals(""))
        {
            posDictionary = readPosDictionary(opts.posDictionaryPath);
            if(opts.forceTagger)
                loadPosTagger();
        }
        else
            loadPosTagger();
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
            Utils.parallelForeach(NUM_OF_THREADS, list);
            if(fileOutputStream != null)
                fileOutputStream.close();
            shutDownWriterService();
            // save vocabulary to disk
            if(posDictionary == null)
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

    private void loadPosTagger()
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
//                out.addAll(Arrays.asList(Utils.readLines(inputPath)));
                String[] sentences = Utils.readLines(path);
                int i = 0;
                for(String sentence : sentences)
                {
                    String[] ar = {("Example_" + ++i), sentence};
                    out.add(ar);
                }
            }
        } // if        
        return out;
    }
    
    /**
     * Read a single example from the input string. Handle event format
     * @param input
     * @return 
     */
    private String[] readExample(String input)
    {
        if(typeOfInput == TypeOfInput.events)
        {
            String[] out = Event3Model.extractExampleFromString(input);
            if(replaceNumbers)
                out[1] = Utils.replaceNumbers(out[1]);
            return out;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Read a single example from the input string. Handle raw format only
     * @param input
     * @param inputPath
     * @return 
     */
    private String[] readExample(String path, String input)
    {
        if(typeOfInput == TypeOfInput.raw)
        {
            String[] out = {path, replaceNumbers ? Utils.replaceNumbers(input) : input};
            return out;
        }
        return null;
    }
    
    /**
     * Open file and read the example. The file should only contain a single example
     * @param inputPath
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

        return readExample(path, out.toString().toLowerCase().trim());
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
        if(typeOfPath == TypeOfPath.file)
        {
            StringBuilder str = new StringBuilder();
            if(typeOfInput == TypeOfInput.raw)
                str.append(taggedText).append("\n");
            else
            {
                for(int i = 0; i < example.length; i++)
                {
                    if(i == 1)
                        str.append(taggedText).append("\n");
                    else
                        str.append(example[i]).append("\n");
                } // for
            } // else
            writerService.submit(new Writer(fileOutputStream, str.toString()));
        } // if
        else        
            writerService.submit(new Writer(example[0], taggedText));
        
        
//        synchronized(fileOutputStream)
//        {
//            if(typeOfInput == TypeOfInput.raw)
//                fileOutputStream.write((taggedText + "\n").getBytes());
//            else
//            {              
//                StringBuilder str = new StringBuilder();
//                for(int i = 0; i < example.length; i++)
//                {
//                    if(i == 1)
//                        str.append(taggedText).append("\n");
//                    else
//                        str.append(example[i]).append("\n");
//                }
//                fileOutputStream.write(str.toString().getBytes());
//            } // else
//        }
    }
    
    public String tag(String[] example)
    {
        String input; 
//        if(typeOfInput == TypeOfInput.raw)
//            input = example[0];
//        else
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
            int countEmpty=0;
            for(int i = 0; i < tokens.length; i++)
            {  
                String token = tokens[i];
                if(!token.isEmpty())
                {
                    List<String> tags = posDictionary.get(token);
                    if(tags.size() > 1) // found ambiguity, report it or use pos tagger to resolve it (costly might have to call the tagger several times)
                    {
                        if(opts.forceTagger)
                        {
                            taggedTextBuilder.append(tagger.tagString(input).split(" ")[i - countEmpty]).append(" ");
                        }
                        else
                        {
                            taggedTextBuilder.append(token).append(" ");
                            if(typeOfInput == TypeOfInput.raw && verbose)
                                System.err.println("Ambiguity in word '" + token + "' of sentence '" + input + "'");
                            else
                                System.err.println("Ambiguity in word '" + token + 
                                        "' in example " + example[0]); 
                        }
                            
                    } // if
                    else                    
                        taggedTextBuilder.append(String.format("%s/%s", token, tags.get(0))).append(" ");
                } // if
                else
                    countEmpty++;
            } // for
            taggedText = taggedTextBuilder.substring(0, taggedTextBuilder.length() - 1); 
        }
        return retainFormatAndNormalise(input, taggedText);
    }

    private String[] splitWordTagToken(String token)
    {
        String[] ar = new String[2];
        int index = token.lastIndexOf("/");
        ar[0] = token.substring(0, index);
        ar[1] = token.substring(index + 1);
        return ar;
    }
    
    /**
     * Retain the format of the original text, i.e. newlines that have been 
     * wiped out from the tagging process. In addition, strip <num> tag assignments
     * as they are most likely noisy.
     * @param original
     * @param tagged
     * @return 
     */
    private String retainFormatAndNormalise(String original, String tagged)
    {
        StringBuilder str = new StringBuilder();
        String[] originalAr = original.split(" ");
        String[] taggedAr = tagged.split(" ");
        for(int i = 0; i < taggedAr.length; i++)
        {            
            String temp = taggedAr[i].contains("<num>") ? "<num>" : taggedAr[i];                
            str.append(originalAr[i].endsWith("\n") ? (temp + "\n") : (temp + " "));
        }
        return str.toString().trim();
    }
    
    private void shutDownWriterService()
    {
        writerService.shutdown();
        try
        {
            while (!writerService.awaitTermination(1, TimeUnit.SECONDS)) { }
        }
        catch(InterruptedException ie)
        {
            System.err.println("Interrupted");
        }
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
    
    protected class Writer extends MyCallable
    {
        String text;
        FileOutputStream fos;
        
        public Writer(String path, String text)
        {
            this.text = text;
            try
            {
                fos = new FileOutputStream(path + ".tagged");
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }        
        
        public Writer(FileOutputStream fos, String text)
        {
            this.text = text;
            this.fos = fos;
        }
        
        @Override
        public Object call() throws Exception
        {
            try
            {                 
                fos.write(text.getBytes());
                fos.close();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            return null;
        }       
    }        
}
