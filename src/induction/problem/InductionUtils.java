package induction.problem;

import fig.basic.Indexer;
import java.util.List;

/**
 * Random utilities
 * @author konstas
 */
public class InductionUtils
{
    public static int[] getIndex(Indexer<String> indexer, String[] v)
    {
        int[] out = new int[v.length];
        for(int i = 0; i < v.length; i++)
        {
            out[i] = indexer.getIndex(v[i]);
        }
        return out;
    }

    /**
     * Take a list of string words, and convert it to an array of integers. Mappings
     * from string to integers are being found/added to wordIndexer
     * @param wordIndexer the indexer of strings to integers and vice versa
     * @param text the list of string words
     * @return an array of integers corresponding to the text
     */
    public static int[] indexWordsOfText(Indexer<String> wordIndexer, List<String> text)
    {
        int[] indices = new int[text.size()];
        for(int i = 0; i < indices.length; i++)
            indices[i] = wordIndexer.getIndex(text.get(i));
        return indices;
    }
    
    public static String[] getObject(Indexer<String> indexer, int[] v)
    {
        String[] out = new String[v.length];
        for(int i = 0; i < v.length; i++)
        {
            if(v[i] == -1)
            {
                out[i] = "*NONE*";
            }
            else
            {
                out[i] = indexer.getObject(v[i]);
            }
        }
        return out;
    }
}
