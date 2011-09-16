package induction.problem.event3.generative;

import java.util.HashMap;
import tercom.TERalignment;
import tercom.TERcalc;
import tercom.TERcost;
import tercom.TERpara;

/**
 *
 * @author konstas
 */
public class TERMetric
{
    private TERcost costfunc = new TERcost();

    public TERMetric()
    {
        HashMap paras = TERpara.getOpts(new String[] {"-r", " dummy", "-h", "dummy"} );
        costfunc._delete_cost = (Double) paras.get(TERpara.OPTIONS.DELETE_COST);
	costfunc._insert_cost = (Double) paras.get(TERpara.OPTIONS.INSERT_COST);
	costfunc._shift_cost = (Double) paras.get(TERpara.OPTIONS.SHIFT_COST);
	costfunc._match_cost = (Double) paras.get(TERpara.OPTIONS.MATCH_COST);
	costfunc._substitute_cost = (Double) paras.get(TERpara.OPTIONS.SUBSTITUTE_COST);

        TERcalc.setNormalize((Boolean)paras.get(TERpara.OPTIONS.NORMALIZE));
	TERcalc.setCase((Boolean)paras.get(TERpara.OPTIONS.CASEON));
	TERcalc.setPunct((Boolean)paras.get(TERpara.OPTIONS.NOPUNCTUATION));
	TERcalc.setBeamWidth((Integer)paras.get(TERpara.OPTIONS.BEAMWIDTH));
	TERcalc.setShiftDist((Integer)paras.get(TERpara.OPTIONS.SHIFTDIST));
    }
  

    public TERalignment getScore(String hyp, String ref)
    {
        return TERcalc.TER(hyp, ref, costfunc);
    }

    public static void main(String[] args)
    {
        String hyp = "He noted that the authorities responsible for the British " +
                "American Security insisted the questioning of all the passengers ," +
                " \" he said , adding that the passengers were forced to quit the" +
                " plane and interrogated by American authorities .";
        String ref = "The British spokesman indicated that \"the American " +
                "authorities responsible for security insisted that all passengers " +
                "be questioned,\" adding that \"passengers were forced to leave the " +
                "plane and undergo questioning by the American authorities.\"";
        TERMetric ter = new TERMetric();
        TERalignment result = ter.getScore(hyp, ref);
        System.out.println(result.score() + " " + result.numEdits + " / " + result.numWords);
        
    }
}
