package induction.ngrams;

import edu.jhu.ckyDecoder.LMGrammar;
import edu.jhu.ckyDecoder.LMGrammar_SRILM;
import java.util.Arrays;

/**
 *
 * @author konstas
 */
public class SrilmNgramWrapper extends NgramModel
{
    private final LMGrammar lm;
    private final boolean BACKOFF = false;
    public SrilmNgramWrapper(String modelFilename, int order)
    {
        System.loadLibrary("srilm");
        lm = new LMGrammar_SRILM(order, BACKOFF);
        lm.read_lm_grammar_from_file(modelFilename);
    }

    @Override
    public double getProb(String[] ngramWords)
    {
//        double log10 = getLogProb10(ngramWords);        
//        return unLog(log10 == -99 ? -500 : log10);        
        return unLog(getLogProb10(ngramWords));
    }

    @Override
    public double getLogProb10(String[] ngramWords)
    {
        return lm.getProb(ngramWords);
    }
    
    public static void main(String[] args)
    {
        int N = 3;
        SrilmNgramWrapper model = new SrilmNgramWrapper(args[0], N);

        String[] input = Arrays.copyOfRange(args, 1, args.length);
        System.out.println("Input : " + Arrays.asList(input));
        System.out.println("LM score: " + model.getProb(input));
    }   
}
