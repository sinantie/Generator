/**
 * 
 */
package edu.berkeley.nlp.mt;

import java.util.List;


/**
 * @author Alexandre Bouchard
 *
 */
public class AveragedBleuScorer
{		
	 protected BleuScorer baseScorer;
	
	/**
	 * 
	 * @param maxN
	 */
	public AveragedBleuScorer(int maxN)
	{
		baseScorer = new BleuScorer(maxN);
	}
	
	public AveragedBleuScorer()
	{
		this(4);
	}

	/**
	 * 
	 * @param candidates
	 * @param references
	 * @param normalize
	 * @return
	 */
	public AveragedBleuScore evaluateBleu(List<List<String>> candidates,
			List<TestSentence> testSentences, boolean normalize)
	{
		return new AveragedBleuScore(
                        baseScorer.evaluateBleu(candidates, testSentences, normalize));
	}	
}
