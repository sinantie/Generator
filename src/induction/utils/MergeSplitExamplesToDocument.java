package induction.utils;

import induction.Utils;
import induction.problem.event3.Event3Example;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Take a list of examples that are split in sentences and merge back to a document.
 * Examples of the same document have an id of the format id_sent_lineNumber
 * 
 * @author sinantie
 */
public class MergeSplitExamplesToDocument
{
    String filename, splitDir, docDir;
    
    private MergeSplitExamplesToDocument(String filename, String splitDir, String docDir)
    {
        this.filename = filename;
        this.splitDir = splitDir;
        this.docDir = docDir;
    }
    
    public void execute() 
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(docDir + filename);
            List<Event3Example> splitExamples = Utils.readEvent3Examples(splitDir + filename, true);
            // first example
            String docName = stripName(splitExamples.get(0).getName());
            Event3Example docExample = new Event3Example(docName, splitExamples.get(0));
            for(int i = 1; i < splitExamples.size(); i++)
            {                
                Event3Example splitExample = splitExamples.get(i);
                String splitName = stripName(splitExample.getName());
                // keep appending sentence-examples
                if(docName.equals(splitName))
                {
                    docExample.add(splitExample);
                }
                // finished concatentating sentence examples
                else
                {
                    // write to file and deal with the new sentence-example
                    fos.write(docExample.toString().getBytes());                    
                    docName = splitName; 
                    docExample = new Event3Example(docName, splitExample);
                }                
            }
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    
    private String stripName(String in)
    {
        return in.substring(0, in.indexOf("_sent"));
    }
    
    public static void main(String[] args)
    {        
        int folds = 10;
        String splitDir = "data/branavan/winHelpHLA/folds/sents.newAnnotation/";
        String docDir = "data/branavan/winHelpHLA/folds/docs.newAnnotation/";
        for(int i = 1; i <= folds; i++)
        {
            String filename = "winHelpFold"+i+"Train.tagged";
            MergeSplitExamplesToDocument m = new MergeSplitExamplesToDocument(filename, splitDir, docDir);
            m.execute();
        }        
    }
 
}
