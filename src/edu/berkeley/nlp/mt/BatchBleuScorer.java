package edu.berkeley.nlp.mt;

import edu.berkeley.nlp.util.Counter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Ioannis Konstas
 */
public class BatchBleuScorer
{
    final int N;
    double[] numerators, denominators;
    private List<Double> weights;
    private int refLength, candLength;

    public BatchBleuScorer(int maxN)
    {
        this.N = maxN;
        numerators = new double[maxN];
        denominators = new double[maxN];
        weights = new ArrayList<Double>();
        for (int i = 0; i < N; i++) {
                weights.add((double) 1 / (double) N);
        }
    }
    
    public BatchBleuScorer()
    {
        this(4);
    }

    public double evaluateBleu(String candidate,
                    String reference)
    {
        List<String> candList = Arrays.asList(candidate.split(" "));
        List<String> refList = Arrays.asList(reference.split(" "));
        return new AveragedBleuScore(evaluateBleu(candList, refList)).getScore();
    }

    public AveragedBleuScore getScore()
    {
        List<Double> globalScores = new ArrayList<Double>();
        for(int i = 0; i < N; i++)
        {
            globalScores.add(numerators[i] / denominators[i]);
        }
        return new AveragedBleuScore(new BleuScore(
                globalScores, weights, refLength, candLength));
    }

    private BleuScore evaluateBleu(List<String> candidate, List<String> reference)
    {
        List<Double> individualNGramScorings = new ArrayList<Double>();
        for (int i = 0; i < N; i++) {
                individualNGramScorings.add(computeIndividualNGramScoring(i + 1, candidate,
                                reference));
        }
        refLength += reference.size();
        candLength += candidate.size();
        return new BleuScore(individualNGramScorings, weights,
                reference.size(), candidate.size());
    }


    protected double computeIndividualNGramScoring(int n, List<String> candidate,
			List<String> reference)
    {
        double denominator = 0.0;
        double numerator = 0.0;

        // extract the counts of all the k-grams, where k=n....
        // ...in the candidate...
        Counter candidateNGramCounts = extractNGramCounts(n, candidate);
        // ...and in the reference
        Counter referenceNGramCounts = extractNGramCounts(n, reference);

        // compute the modified n-gram precisions
        for (Object currentNGram : candidateNGramCounts.keySet())
        {
            // the count in the candidate sentence of the current n-gram is added to the denominator
            double currentCount = candidateNGramCounts.getCount(currentNGram);
            denominator += currentCount;
            // find in the reference the maximum number of occurrences of the current ngram
            double max = referenceNGramCounts.getCount(currentNGram);

            // the minimum of {max, currentCount} is added to the numerator
            if (max < currentCount) {
                    numerator += max;
            } else {
                    numerator += currentCount;
            }
        }

        numerators[n - 1] += numerator;
        denominators[n - 1] += denominator;

        if (denominator == 0.0)
        {
            return 0.0d;
        }
        else
        {
            return numerator / denominator;
        }
    }
  
    /**
     * Extract all the ngrams and their counts in a given sentence.
     *
     * @param n n in n-gram
     * @param sentences
     * @return
     */
    protected Counter extractNGramCounts(int n, List<String> sentences) {
            Counter<List<String>> nGrams = new Counter<List<String>>();
            for (int i = 0; i <= sentences.size() - n; i++) {
                    nGrams.incrementCount(sentences.subList(i, i + n), 1.0);
            }
            return nGrams;
    }
}
