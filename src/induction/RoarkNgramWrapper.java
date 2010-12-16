package induction;

import java.util.Arrays;
import roark.parser.SWIGTYPE_p_TDParPtr;
import roark.parser.roark;

/**
 *
 * @author konstas
 */
public class RoarkNgramWrapper extends NgramModel
{
    private SWIGTYPE_p_TDParPtr parseConf;
//    LexicalizedParser lp;

    public RoarkNgramWrapper(String modelFilename)
    {
        System.loadLibrary("roark");
        parseConf = roark.initParser(modelFilename);
//        lp = new LexicalizedParser(modelFilename);
//        lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
    }

    public double getProb(String[] ngramWords)
    {
        StringBuffer sb = new StringBuffer();
        for(String word : ngramWords)
        {
            sb.append(word + " ");
        }
//        lp.parse(sb.toString());
//        return unLogBase2(lp.getPCFGScore());
        return roark.parseString(parseConf, sb.toString());
    }
    
    public static void main(String[] args)
    {       
        RoarkNgramWrapper model = new RoarkNgramWrapper(args[0]);

        String[] input = Arrays.copyOfRange(args, 1, args.length);
        System.out.println("Input : " + Arrays.asList(input));
        System.out.println("LM score: " + model.getProb(input));
    }
}
