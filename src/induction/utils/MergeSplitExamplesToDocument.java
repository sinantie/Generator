package induction.utils;

import induction.Utils;
import induction.problem.event3.Event3Example;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Take a list of examples that are split in sentences and merge back to a document.
 * Examples of the same document have an id of the format id_sent_lineNumber
 * 
 * @author sinantie
 */
public class MergeSplitExamplesToDocument
{
    String filename, splitDir, docDir;
    Set<String> excludedExamples;
    
    private MergeSplitExamplesToDocument(String filename, String splitDir, String docDir, String excludedExamplesFile)
    {
        this.filename = filename;
        this.splitDir = splitDir;
        this.docDir = docDir;
        excludedExamples = new HashSet<String>();
        Collections.addAll(excludedExamples, Utils.readLines(excludedExamplesFile));        
    }
    
    public void execute() 
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(docDir + filename);
            List<Event3Example> splitExamples = Utils.readEvent3Examples(splitDir + filename, true);
            // remove excludedExamples
            if(!excludedExamples.isEmpty())
            {
                Iterator<Event3Example> it = splitExamples.listIterator();
                while(it.hasNext())
                {
                    if(excludedExamples.contains(stripName(it.next().getName())))
                        it.remove();
                }
            }
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
            // don't forget last example
            fos.write(docExample.toString().getBytes());    
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
        String docDir = "data/branavan/winHelpHLA/folds/docs.newAnnotation.removedOutliers/";
        String excludeExamplesFile = "data/branavan/winHelpHLA/folds/removedOutliers";
        for(int i = 1; i <= folds; i++)
        {
            String filename = "winHelpFold"+i+"Train.tagged";
            MergeSplitExamplesToDocument m = new MergeSplitExamplesToDocument(filename, splitDir, 
                                                                              docDir, excludeExamplesFile);
            m.execute();
        }        
    }
 
}
