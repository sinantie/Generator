package induction.utils;

import edu.berkeley.nlp.ling.Tree;
import fig.basic.LogInfo;
import induction.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author konstas
 */
public class ExportMrgToString
{
    String inputLists, outputFile;
    boolean removePunctuation;
                    
    private ExportMrgToString(String inputLists, String outputFile, boolean removePunctuation)
    {
        this.inputLists = inputLists;
        this.outputFile = outputFile;
        this.removePunctuation = removePunctuation;
    }
    
    public void execute()
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(outputFile);                    
            for(String folder : Utils.readLines(inputLists))
            {
                File f = new File(folder);
                if(f.isDirectory())            
                    readFromSingleFileMrg(f.listFiles(), fos);
            }
            fos.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    private void readFromSingleFileMrg(File[] inputLists, FileOutputStream fos)
    {
        for(File file : inputLists)
        {            
            try
            {
                List<Tree<String>> trees = Utils.loadTrees(file.getAbsolutePath(), removePunctuation);
                for(Tree tree : trees)
                {
                    fos.write((tree.toSurfaceStringLowerCase() + "\n").getBytes());
//                        readExample(tree, Integer.MAX_VALUE);
                }
            }
            catch(IOException ioe)
            {
                LogInfo.error("Error loading file " + file);
            }                
        } // for
    }
    
    public static void main(String[] args)
    {
        String inputLists = "../wsj/3.0/text/00-24";
        String outputFile = "../wsj/3.0/text/00-24.sentences";
        boolean removePunctuation = false;
        ExportMrgToString emts = new ExportMrgToString(inputLists, outputFile, removePunctuation);
        emts.execute();
    }
}
