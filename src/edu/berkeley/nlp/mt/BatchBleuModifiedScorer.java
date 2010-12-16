package edu.berkeley.nlp.mt;

import edu.berkeley.nlp.util.Counter;
import java.util.List;

/**
 *
 * @author Ioannis Konstas
 */
public class BatchBleuModifiedScorer extends BatchBleuScorer
{
    public BatchBleuModifiedScorer(int N)
    {
        super(N);
    }
//    @Override
//    protected double computeIndividualNGramScoring(int n, List<String> candidate,
//			List<String> reference)
//    {
//        double denominator = 0.0;
//        double numerator = 0.0;
//
//        // extract the counts of all the k-grams, where k=n....
//        // ...in the candidate...
//        Counter candidateNGramCounts = super.extractNGramCounts(n, candidate);
//        // ...and in the reference
//        Counter referenceNGramCounts = super.extractNGramCounts(n, reference);
//
//        // compute the modified n-gram precisions
//        for (Object currentNGram : candidateNGramCounts.keySet())
//        {
//            // the count in the candidate sentence of the current n-gram is added to the denominator
//            double currentCount = candidateNGramCounts.getCount(currentNGram);
//            denominator += currentCount;
//            // find in the reference the maximum number of occurrences of the current ngram
////            double max = referenceNGramCounts.getCount(currentNGram);
//            double max = getCountModified(referenceNGramCounts, (List<String>) currentNGram);
//
//            // the minimum of {max, currentCount} is added to the numerator
//            if (max < currentCount) {
//                    numerator += max;
//            } else {
//                    numerator += currentCount;
//            }
//        }
//
//        numerators[n - 1] += numerator;
//        denominators[n - 1] += denominator;
//
//        if (denominator == 0.0)
//        {
//            return 0.0d;
//        }
//        else
//        {
//            return numerator / denominator;
//        }
//    }
//
//    private double getCountModified(Counter referenceNgramCounter, List<String> candidateNgram)
//    {
//
//        for(List<String> referenceNgram : (Set<List<String>>) referenceNgramCounter.keySet())
//        {
//            boolean equal = true;
//            for(int i = 0; i < referenceNgram.size(); i++)
//            {
//                if(referenceNgram.get(i).matches("\\p{Digit}+") &&
//                        candidateNgram.get(i).matches("\\p{Digit}+"))
//                    equal = equal && Math.abs(
//                            Integer.valueOf(referenceNgram.get(i)) -
//                            Integer.valueOf(candidateNgram.get(i))) <= 5;
//                else
//                    equal = equal && referenceNgram.get(i).equals(candidateNgram.get(i));
//            }
//            if(equal)
//                return referenceNgramCounter.getCount(referenceNgram);
//        }
//        return 0.0;
//    }

    /**
     * Extract all the ngrams and their counts in a given sentence.
     *
     * @param n n in n-gram
     * @param sentences
     * @return
     */

    @Override
    protected Counter<BleuModifiedNgram> extractNGramCounts(int n, List<String> sentences) {
//            Counter<BleuModifiedNgram> nGrams = new Counter<BleuModifiedNgram>(new MapFactory.TreeMapFactory());
            Counter<BleuModifiedNgram> nGrams = new Counter<BleuModifiedNgram>();
            for (int i = 0; i <= sentences.size() - n; i++) {
                    nGrams.incrementCount(new BleuModifiedNgram(sentences.subList(i, i + n)), 1.0);
            }
            return nGrams;
    }   
}
