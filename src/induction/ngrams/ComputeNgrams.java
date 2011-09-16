package induction.ngrams;

import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options;
import induction.Utils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import kylm.model.ngram.NgramLM;
import kylm.model.ngram.smoother.MKNSmoother;
import kylm.model.ngram.writer.ArpaNgramWriter;
import kylm.model.ngram.writer.NgramWriter;
import kylm.reader.SentenceReader;

/**
 *
 * @author konstas
 */
public class ComputeNgrams implements Runnable
{
    Options opts = new Options();
    NgramLM lm;
    BufferedOutputStream bos;
    boolean writeSentences = true, write = true, splitOnNewLine = true;
    public void run()
    {

        // create the n-gram model
        lm = new NgramLM(opts.ngramSize, new MKNSmoother());
        lm.getSmoother().setCutoffs(null);
        lm.setDebug(5);
        lm.setName(null);
        lm.setUnknownSymbol("<unk>");
        lm.setVocabFrequency(0);
        lm.setStartSymbol("<s>");
        lm.setTerminalSymbol("</s>");

        
        // create the input sentence loader

        CorpusReader corpusReader = new CorpusReader(readFilenames(opts.inputPaths, opts.inputLists));
        try
        {
            if(writeSentences)
                bos = new BufferedOutputStream(
                    new FileOutputStream(opts.ngramModelFile + ".sentences"));
            // train the model        
            lm.trainModel(corpusReader);
            if(writeSentences)
                bos.close();
        }
        catch (IOException ex)
        {
            Execution.finish();
        }

        LogInfo.logs("CountNgrams, Started writing");
        long time = System.currentTimeMillis();
        //set up writer
        NgramWriter writer = new ArpaNgramWriter();
        // print the model
        try
        {
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(opts.ngramModelFile));

        writer.write(lm, os);
        os.close();
        }
        catch(IOException e)
        {
            LogInfo.error("Error writing file");
        }

        LogInfo.logs("CountNgrams, done writing - " +
                (System.currentTimeMillis()-time)+" ms");

        
    }

    private ArrayList<String> addPath(String path)
    {
        ArrayList<String> pathList = new ArrayList();
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                pathList.addAll(addPath(path + "/" + fileStr));
            } // for
        } // if
        else if(validName(path))
        {
            if(!path.endsWith(".text"))
                path = path.replaceAll("\\."+ opts.inputFileExt,
                                                ".text");
            pathList.add(path);
        }
        return pathList;
    }

    private ArrayList<String> readFilenames(ArrayList<String> inputPaths,
            ArrayList<String> inputLists)
    {
        ArrayList<String> pathList = new ArrayList();
        for(String path : inputPaths)
        {
            pathList.addAll(addPath(path));
        }
        for(String path : inputLists) // file that contains paths
        {
            for(String line : Utils.readLines(path))
            {
                pathList.addAll(addPath(line));
            } // for
        } // for
        return pathList;
    }

    private boolean validName(String path)
    {
        return Utils.isEmpty(opts.inputFileExt) ||
               path.endsWith(opts.inputFileExt);
    }
    private String pathName(String path, String f)
    {
        if(f.charAt(0) == '/')
        {
            return f;
        }
        return new File(path).getParent() + "/" + f;
    }

    public static void main(String[] args)
    {       
        ComputeNgrams x = new ComputeNgrams();
        Execution.run(args, x, x.opts);
    }

    private class CorpusReader implements SentenceReader
    {
        private final ArrayList<String> pathList;

        public CorpusReader(ArrayList<String> pathList)
        {
            this.pathList = pathList;
        }

        public Iterator<String[]> iterator()
        {
            return new CorpusIterator(pathList);
        }

        public boolean supportsReset()
        {
            return true;
        }

        // iterator implementation
	private class CorpusIterator implements Iterator<String[]>
        {
            private final ArrayList<String> pathList;
            
            private int curPos = 0;
            public CorpusIterator(ArrayList<String> pathList)
            {
                this.pathList = pathList;
            }

            @Override
            public boolean hasNext()
            {
                if(curPos >= pathList.size())
                {
                    write = false;
                }
                return curPos < pathList.size();
            }

            @Override
            public String[] next()
            {
                return processFile(pathList.get(curPos++));
            }

            private String[] processFile(String path)
            {
                String text = "";
                try
                {
                    BufferedReader br = new BufferedReader(new FileReader(path));
                    String line = "";
                    for(int i = 0; i < opts.ngramSize - 1; i++)
                    {
                        text += "<s> ";
                    }
                    while((line = br.readLine()) != null)
                    {
                        String out = "";
                        for(String s : line.split(" "))
                        {
                            out += (s.matches("-\\p{Digit}+|" + // negative numbers
                                         "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                                         (s.matches("\\p{Digit}+") && // numbers
                                          !(s.contains("am") || s.contains("pm"))) // but not hours!
                                   ) ?  "<num> " : s + " ";
                        }
                        text += out.trim().toLowerCase() + " ";
                    }
                    br.close();
                    text += "</s>";
                    if(writeSentences && write)
                        bos.write((text + "\n").getBytes());
                }
                catch(IOException ioe)
                {
                    LogInfo.error("Error reading file " + path);
                }                
                return text.split(" ");
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("Remove is not implemented");
            }
	}
    }
}
