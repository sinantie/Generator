package induction.ngrams;

import fig.basic.LogInfo;
import fig.exec.Execution;
import java.io.IOException;
import java.util.Arrays;
import kylm.model.ngram.NgramLM;
import kylm.model.ngram.NgramNode;
import kylm.model.ngram.reader.ArpaNgramReader;
import kylm.model.ngram.reader.NgramReader;
import opennlp.tools.util.Cache;

/**
 * Wrapper class for kylm ngram modeling toolkit
 *
 * @author konstas
 */
public class KylmNgramWrapper extends NgramModel
{
    private NgramLM lm;

    /**
     * Creates a new instance of the kylm model, given a trained ngram model in
     * arpa format
     * @param modelFile the filename pointing to the ngram model
     */
    public KylmNgramWrapper(String modelFile)
    {
        NgramReader nr = new ArpaNgramReader();
        lm = null;
        try
        {
            lm = nr.read(modelFile);
        }
        catch(IOException e)
        {
            LogInfo.errors("Problem reading model from file %s: %s",
                          modelFile, e.getMessage());
            Execution.finish();
        }
    }
    
    /**
     * Katz backoff implementation. Essentially tries to find the longest valid
     * n-gram in <code>ids</code> ending at position <code>pos</code>. If a
     * complete n-gram is not found, then it recursively goes for the next (n-1)-gram
     * ending at <code>pos</code>. The result is then the (n-1)-gram score plus
     * its backoff weight (Note we are in the log domain).
     * @param ids an array of mapped words to ids in the vocabulary
     * @param pos the position of the word we condition on
     * @return the weight of the n-gram with backoff (if necessary)
     */
    private float getNgramProb(int[] ids, int pos)
    {
        float ret = 0;
        boolean backoff = false;
        int i, context, idx;
        NgramNode root = lm.getRoot();
        NgramNode node = root, child;
        for(context = Math.max(0, pos-lm.getN()+1); context <= pos; context++)
        {
            // go down the tree trying to find the ngram
            for(i = context; i <= pos; i++)
            {
                idx = (lm.getClassMap()==null?ids[i]:lm.getClassMap().getWordClass(ids[i]));
                child = node.getChild(idx);
                if(child == null) // if complete (n-context)-gram was not found
                {
                    backoff = true; // set flag in case original n-gram was not found
                    return -99;
//                    break;
                }
                node = child;
            } // for
            if(i == pos+1) // if found, return the score
            {
                if(backoff)
                {
                    ret = node.getBackoffScore();
                }
                return ret+node.getScore();
            }
            // if not found, retry with smaller context, beginning with w_context
            else
                node = root;
        } // for
        throw new IllegalArgumentException("could not find n-gram");
    }

    /**
     * Computes the probability of <code>word</code> given the n-1 words in
     * <code>context</code>
     * @param word
     * @param context
     * @return probability of <code>word</code> given <code>context</code>
     */
    public double getProb(String word, String[] context)
    {
        int[] ids = new int[context.length + 1];
        for(int i = 0; i < context.length; i++)
        {
            ids[i] = lm.getId(context[i]);
        }
        ids[context.length] = lm.getId(word);
        return unLog(getNgramProb(ids, ids.length - 1));
    }

    /**
     * Computes the probability of the last word in <code>ngram</code> given
     * the previous n-1 words in it
     * @param ngram
     * @return probability of the <code>ngram</code>
     */
    @Override
    public double getProb(String[] ngram)
    {
        int[] ids = getIdsOfNgram(ngram);
        return unLog(getNgramProb(ids, ngram.length - 1));
    }

    @Override
    public double getLogProb10(String[] ngramWords)
    {
        int[] ids = getIdsOfNgram(ngramWords);
        return getNgramProb(ids, ngramWords.length - 1);
    }
    
    private int[] getIdsOfNgram(String[] ngram)
    {
        int[] ids = new int[ngram.length];
        try
        {
            for(int i = 0; i < ngram.length; i++)
            {
                ids[i] = lm.getId(ngram[i]);
            }
        }
        catch(IllegalArgumentException e)
        {
            LogInfo.logs(e.getMessage());
            Execution.finish();
        }
        return ids;
    }
    public static void main(String[] args)
    {
//        String in = "with a low around 70 , snow between -0.1 and 0.4 inches, at 5pm and -5 degrees";
//        String out = "";
//
//        for(String s : in.split(" "))
//        {
//            out += (s.matches("-\\p{Digit}+|" + // negative numbers
//                         "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
//                         (s.matches("\\p{Digit}+") && // numbers
//                          !(s.contains("am") || s.contains("pm"))) // but not hours!
//                   ) ?  "<num> " : s + " ";
//        }
        String[] t1 = {"with", "west", "wind"};
        String[] t2 = {"with", "west", "wind"};
        Cache c = new Cache(1);
        c.put(Arrays.deepHashCode(t1), -2);
        System.out.println(Arrays.deepHashCode(t1) + " " +
                           Arrays.deepHashCode(t2) + " " +
                           c.containsKey(Arrays.deepHashCode(t2)));
        System.exit(0);
        KylmNgramWrapper model = new KylmNgramWrapper(args[0]);
        String[] input = Arrays.copyOfRange(args, 1, args.length);
        System.out.println("Input : " + Arrays.asList(input));
        System.out.println("LM score: " + model.getProb(input));
    }
}
