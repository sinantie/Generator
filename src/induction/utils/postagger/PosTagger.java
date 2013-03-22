package induction.utils.postagger;

import induction.utils.postagger.PosTaggerOptions.TypeOfInput;
import induction.utils.postagger.PosTaggerOptions.TypeOfPath;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fig.basic.IOUtils;
import induction.MyCallable;
import induction.Utils;
import induction.problem.event3.Event3Example;
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
    String tagDelimeter;
    private Map<String, String> universalMaps;       
    private Map<String, List<String>> posDictionary;
    private ExecutorService writerService;
    private String[] writerList;
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
        this.tagDelimeter = opts.tagDelimiter;
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
            List<Example> examples = null; 
            if(typeOfPath == TypeOfPath.list)            
                examples = readFromList();
            else if(typeOfPath == TypeOfPath.file)                                    
            {
                examples = readExamplesFromSingleFile();
                writerList = new String[examples.size()];
            }
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
            {
//                Writer w = new Writer(fileOutputStream);
                for(String s : writerList)
                    fileOutputStream.write(s.getBytes());
                fileOutputStream.close();
            }
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
    private List<Example> readFromList() throws IOException
    {
        List<Example> out = new ArrayList<Example>();
        BufferedReader fin = new BufferedReader(new FileReader(path));
        String line = "";
        int counter = 0;
        while((line = fin.readLine()) != null)
        {
            if(!extension.equals("") && !line.endsWith(extension))
            {
                out.add(new Example(readFile(IOUtils.stripFileExt(line) + "." + extension), counter));
            }              
            else
            {
                out.add(new Example(readFile(line), counter));
            }
            counter++;
        }
        fin.close();
        return out;
    }
    
    /**
     * Read examples from  a single file
     * @return
     * @throws IOException 
     */
    private List<Example> readExamplesFromSingleFile() throws IOException
    {
        List<Example> out = new ArrayList<Example>();
        int counter = 0;
        for(Event3Example ex : Utils.readEvent3Examples(path, true))
        {
            if(typeOfInput == TypeOfInput.events && replaceNumbers)
            {                
                ex.setText(Utils.replaceNumbers(ex.getText()));
            }
            out.add(new Example(ex, counter++));
        }
//        if(new File(path).exists())
//        {
//            int counter = 0;
//            if(typeOfInput == TypeOfInput.events)
//            {
//                String key = null;
//                StringBuilder str = new StringBuilder();
//                for(String line : Utils.readLines(path))
//                {
//                    if(line.startsWith("Example_"))
//                    {
//                        if(key != null) // only for the first example
//                        {                      
//                            out.add(new Example(readExample(str.toString()), counter++));
//                            str = new StringBuilder();
//                        }
//                        key = line;
//                    } // if                   
//                    str.append(line).append("\n");                    
//                }  // for           
//                out.add(new Example(readExample(str.toString()), counter)); // don't forget last example
//            } // if
//            else
//            {
////                out.addAll(Arrays.asList(Utils.readLines(inputPath)));
//                String[] sentences = Utils.readLines(path);
//                int i = 0;
//                for(String sentence : sentences)
//                {
//                    String[] ar = {("Example_" + ++i), sentence};
//                    out.add(new Example(ar, counter++));
//                }
//            }
//        } // if        
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
            String[] out = Utils.extractExampleFromString(input);
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
    private Event3Example readExample(String path, String input)
    {
        if(typeOfInput == TypeOfInput.raw)
        {
            return new Event3Example(path, replaceNumbers ? Utils.replaceNumbers(input) : input, null, null);            
        }
        return null;
    }
    
    /**
     * Open file and read the example. The file should only contain a single example
     * @param inputPath
     * @return
     * @throws IOException 
     */
    private Event3Example readFile(String path) throws IOException
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
            int indexOfDelimeter = line.lastIndexOf(tagDelimeter);
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
    
    private void parse(Example example)
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
                    int index = token.lastIndexOf(tagDelimeter);
                    syncVocabulary.add(token.substring(0, index + 1) + 
                                       universalMaps.get(token.substring(index + 1)));
                }
            } // else
        } // if
        
    }

    private void saveToFile(Example example, String taggedText) throws IOException
    {              
        if(typeOfPath == TypeOfPath.file)
        {
            StringBuilder str = new StringBuilder();
            if(typeOfInput == TypeOfInput.raw)
                str.append(taggedText).append("\n");
            else
            {
                example.body.setText(taggedText); // store to the example the new tagged text
                str.append(example.body.toString());
//                for(int i = 0; i < example.body.getNumberOfRecords(); i++)
//                {
//                    if(i == 1)
//                        str.append(taggedText).append("\n");
//                    else
//                        str.append(example.body[i]).append("\n");
//                } // for
            } // else
            //writerService.submit(new Writer(fileOutputStream, str.toString()));
            // delay writing until after all threads are done to preserve order
            synchronized(writerList)
            {
                writerList[example.id] = str.toString();
            } 
        } // if
        else        
            writerService.submit(new Writer(example.body.getName(), taggedText));
        
        
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
    
    public String tag(Example example)
    {
        String input; 
//        if(typeOfInput == TypeOfInput.raw)
//            input = example[0];
//        else
        input = example.body.getText(); // res[0] = name, res[1] = text
        String taggedText;
        if(posDictionary == null) // use trained pos tagger
        {
            taggedText = tagger.tagTokenizedString(input);
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
            input = input.replace("'", " '"); // correctly tokenize words with apostrophe
            String[] tokens = input.split("\\p{Space}");
            StringBuilder taggedTextBuilder = new StringBuilder();
            int countEmpty = 0;
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
                            taggedTextBuilder.append(tagger.tagTokenizedString(input).split(" ")[i - countEmpty]).append(" ");
                        }
                        else
                        {
                            taggedTextBuilder.append(token).append(" ");
                            if(typeOfInput == TypeOfInput.raw && verbose)
                                System.err.println("Ambiguity in word '" + token + "' of sentence '" + input + "'");
                            else
                                System.err.println("Ambiguity in word '" + token + 
                                        "' in example " + example.body.getName()); 
                        }
                            
                    } // if
                    else                    
                        taggedTextBuilder.append(String.format("%s%s%s", token, tagDelimeter, tags.get(0))).append(" ");
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
        int index = token.lastIndexOf(tagDelimeter);
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
        if(!replaceNumbers) // if new lines have not been taken care of already
            original = original.replaceAll("\n", "\n ");
        original = original.replace("'", " '"); // correctly tokenize words with apostrophe
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
        Example example;

        public Worker(int counter, Example example)
        {
            this.example = example;
            this.counter = counter;
        }
                
        @Override
        public Object call() throws Exception
        {
            parse(example);               
            if(counter++ % opts.outputExampleFreq == 0)
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

        public Writer(FileOutputStream fos)
        {        
            this.fos = fos;
        }
        
        public Writer(FileOutputStream fos, String text)
        {
            this.text = text;
            this.fos = fos;
        }
        
        @Override
        public Object call()
        {
            try
            {
                write(text);
                fos.close();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            return null;
        }
        
        public void write(String[] list)
        {
            try
            {
                for(String s : list)
                    write(s);
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        
        private void write(String text) throws IOException
        {
            fos.write(text.getBytes());
        }
    }
    
    protected class Example {
        Event3Example body;
        int id;

        public Example(Event3Example body, int id)
        {
            this.body = body;
            this.id = id;
        }
                
    }
}
