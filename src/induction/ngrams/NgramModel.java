package induction.ngrams;

import edu.stanford.nlp.util.ArrayUtils;
import fig.basic.Indexer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public static Map<List<Integer>, Integer> readNgramsFromArpaFile(String modelFile, 
            int N, Indexer<String> vocabulary)
    {
        Map<List<Integer>, Integer> map = null;
        File f = new File(modelFile);
        if(f.exists())
        {
            try
            {
                BufferedReader fr = new BufferedReader(new FileReader(f));
                String line = "";
                boolean nSizeExists = false;                
                // check that the arpa file contains the correct n-grams we are looking for
                while(!line.equals("\\data\\"))
                    line = fr.readLine();
                
                while((line = fr.readLine()).contains("ngram"))
                {
                    if(line.split("[ =]")[1].equals(String.valueOf(N)))
                    {
                        nSizeExists = true;
                        break;
                    }
                }
                // read the ngrams
                if(nSizeExists)
                {                    
                    map = new HashMap<List<Integer>, Integer>();
                    // go to the correct position interface the file
                    while(!line.contains("\\"+N+"-grams:"))
                        line = fr.readLine();                    
                        
                    int pos = 0;
                    line = fr.readLine();
                    while(!line.equals("\\"+(N+1)+"-grams:") &&
                          !line.equals("\\end\\"))                        
                    {
                        if(!line.equals(""))
                        {
                            String[] ar = line.split("[\t ]");
                            List<Integer> list = new ArrayList<Integer>(N);
                            for(int i = 1; i < N + 1; i++)
                            {
                                try {
                                list.add(vocabulary.getIndex(ar[i]));
                                }catch(Exception e)
                                {
                                    System.out.println("execption!!");
                                }
                            }
                            map.put(list, pos++);
                        } // if
                        line = fr.readLine();
                    } // while                    
                }
                fr.close();
            }
            catch(IOException ioe){   }
        }        
        return map;
    }
    
    public static List<Integer> getNgramIndices(Map<List<Integer>, Integer> ngrams, 
            int N, int i, int j, int[] text)
    {
        List<Integer> indices = new ArrayList<Integer>();
        if(j - i >= N - 1)
        {
            for(int k = i; k < j - (N - 1); k++)
            {                    
                Integer index = ngrams.get(ArrayUtils.asList(Arrays.copyOfRange(text, k, k + N - 1)));
                if(index != null)
                    indices.add(index);
            }
        }
        return indices;
    }
}
