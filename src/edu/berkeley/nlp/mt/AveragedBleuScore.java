/**
 * 
 */
package edu.berkeley.nlp.mt;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;



/**
 * @author Alexandre Bouchard
 * 
 * A "Soft" version of bleu score to prevent zeros when there are not 4 grams found.
 *
 */
public class AveragedBleuScore
{
	/**
	 * 
	 */
	private BleuScore baseBleuScore;
	
	/**
	 * 
	 * @param score
	 */
	public AveragedBleuScore(BleuScore score)
	{
		this.baseBleuScore = score;
	}
	
	/**
	 * 
	 */
	double score = Double.NEGATIVE_INFINITY;
	
	/**
	 * 
	 * Compute (and cache) the geometric mean of the individual n grams and 
	 * multiply by the brevity penalty
	 * 
	 * @return
	 */
	public double getScore()
	{
              return getScore(baseBleuScore.getIndividualNGramScorings().size());
	}

        public double getScore(int maxN)
        {            
            List<Double> individualNGramScorings = baseBleuScore.getIndividualNGramScorings();
            /*normal BLEU score*/
//                        List<Double> weights = baseBleuScore.getWeights();
//                        double exponent = 0.0;
//			for (int i = 0; i < individualNGramScorings.size(); i++) {
//				exponent += Math.log(individualNGramScorings.get(i)) * weights.get(i);
//			}
//			score = baseBleuScore.brevityPenalty() * Math.exp(exponent);

            // first compute the logz
            List<Double> individualNGramLogs = new ArrayList<Double>();
            for (double currentScore : individualNGramScorings)
            {
                    individualNGramLogs.add(Math.log(currentScore));
            }
            // then the brevity penalty:
            double penalty = baseBleuScore.brevityPenalty();
            // do a geometrically-weighted arithmetic average of the 1-gram bleu score ... n-gram bleu score
            int i = 1;
            double average = 0.0;
            for (int N = maxN; N > 0; N--)
            {
                    double exponent = 0.0;
                    for (int j = 0; j < N; j++)
                    {
                            exponent += individualNGramLogs.get(j) / N;
                    }
                    average += penalty * Math.exp(exponent) / Math.pow(2, i);
                    i++;
            }
            // cache the result
            score = average;
            return score;

        }
	/**
	 * 
	 * @param arg0
	 * @return
	 */
	public int compareTo(Object arg0)
	{
		if (arg0 instanceof BleuScore)
		{
			BleuScore other = (BleuScore) arg0;
			if (this.getScore() < other.getScore())
			{
				return -1;
			}
			else if (this.getScore() > other.getScore())
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		else
		{
			throw new ClassCastException();
		}
	}
	
	/**
	 * 
	 * @param arg0
	 * @return
	 */
    @Override
	public boolean equals(Object arg0)
	{
		if (arg0 instanceof BleuScore)
		{
			BleuScore other = (BleuScore) arg0;
			if (this.getScore() == other.getScore())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Averaged Bleu scores: " + formatDouble(getScore()) + "\n");
		buffer.append("\n" +
				"Individual N-gram scoring\n" +
				"        1-gram   2-gram   3-gram   4-gram   5-gram   6-gram   7-gram   8-gram   9-gram   ...\n" +
				"        ------   ------   ------   ------   ------   ------   ------   ------   ------ \n" +
				"BLEU:");
		for (double score : baseBleuScore.getIndividualNGramScorings())
		{
			buffer.append("  " + formatDouble(score) + " ");
		}
		buffer.append("\n" +
				"Cumulative N-gram scoring\n" +
				"        1-gram   2-gram   3-gram   4-gram   5-gram   6-gram   7-gram   8-gram   9-gram   ...\n" +
				"        ------   ------   ------   ------   ------   ------   ------   ------   ------ \n" +
				"BLEU:");
		for (int i = 1; i <= baseBleuScore.getIndividualNGramScorings().size(); i++)
		{
			buffer.append("  " + formatDouble(getScore(i)) + " ");
		}
		return buffer.toString();
	}
  
  /**
   * Formats a double in 0.0000 format.
   * 
   * Forces the decimal separator to be a dot to avoid
   * different display on foreign locales (e.g. a comma
   * with the french locale)
   * 
   * @return
   */
  private static DecimalFormat format = null;
  public static String formatDouble(double number)
  {
    if (format == null)
    {
      DecimalFormatSymbols dsymb = new DecimalFormatSymbols();
      dsymb.setDecimalSeparator('.');
      format = new DecimalFormat("0.0000");
      format.setDecimalFormatSymbols(dsymb);
    }
    return format.format(number);
  }
}
