package induction.problem;

import fig.basic.Indexer;

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
