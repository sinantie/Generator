package induction.ngrams;

import fig.basic.Indexer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author konstas
 */
public abstract class NgramModel
{
    public abstract double getProb(String[] ngramWords);
 
    /**
     * Converts a 10-base log probability to a normal probability in [0, 1]
     * @param value the log probability
     * @return the probability in [0, 1]
     */
    protected double unLog(float value)
    {
        return Math.pow(10, value);
    }

    protected double unLog(double value)
    {
        return Math.pow(10, value);
    }
    /**
     * Converts a 2-base log probability to a normal probability in [0, 1]
     * @param value the log probability
     * @return the probability in [0, 1]
     */
    private double unLogBase2(double value)
    {
        return Math.pow(2, value);
    }
    
    private static HashSet<List<Integer>> readNgramFromArpaFile(String modelFile, 
            int N, Indexer vocabulary)
    {
        HashSet<List<Integer>> set = null;
        File f = new File(modelFile);
        if(f.exists())
        {
            try
            {
                FileReader fr = new FileReader(f);
                
                fr.close();
            }
            catch(IOException ioe){   }
        }        
        return set;
    }
}
