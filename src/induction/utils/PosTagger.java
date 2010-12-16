package induction.utils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fig.basic.IOUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author konstas
 */
public class PosTagger
{
    private final String pathFiles;
    private String extension = null;
    private MaxentTagger tagger;
    private Set<String> taggedVocabulary;

    public PosTagger()
    {
        this("");
    }

    public PosTagger(String pathFiles)
    {
        this.pathFiles = pathFiles;
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

    public PosTagger(String pathFiles, String extension)
    {
        this(pathFiles);
        this.extension = extension;
    }

    public void execute()
    {
        try
        {
            BufferedReader fin = new BufferedReader(new FileReader(pathFiles));
            String line = "";
            int counter = 0;
            while((line = fin.readLine()) != null)
            {
                if(extension != null && !line.endsWith(extension))
                {
                    parse(readFile(IOUtils.stripFileExt(line) + "." + extension));
                }              
                else
                {
                    parse(readFile(line));
                }

                if(counter++ % 1000 == 0)
                    System.out.println("Processed " + counter + " files");
                
            }
            fin.close();

            FileOutputStream fos = new FileOutputStream(pathFiles + "_vocabulary");
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
        if(args.length > 2)
        {
            System.err.println("Usage: file_with_paths [extension]");
            System.exit(1);
        }
        PosTagger pos;
        if(args.length > 1)
            pos = new PosTagger(args[0], args[1]);
        else
            pos = new PosTagger(args[0]);

        pos.execute();
    }
}
