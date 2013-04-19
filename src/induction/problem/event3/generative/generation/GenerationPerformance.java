package induction.problem.event3.generative.generation;

import induction.problem.event3.generative.alignment.AlignmentPerformance;
import induction.problem.event3.params.Parameters;
import edu.berkeley.nlp.mt.BatchBleuModifiedScorer;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import fig.basic.EvalResult;
import fig.basic.Fmt;
import fig.basic.Indexer;
import induction.MyList;
import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.TERMetric;
import induction.problem.event3.Widget;
import induction.problem.wordproblem.WordModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import tercom.TERalignment;
import tercom.TERcalc;

/**
 * Compute BLEU, METEOR and TER score for predicted generated text,
 * given gold standard text.
 *
 * @author konstas
 */
public class GenerationPerformance extends AlignmentPerformance
{
    BatchBleuScorer bleuScorer, bleuModifiedScorer;
    MeteorScorer meteorScorer;
    MeteorStats meteorAggStats;
    TERMetric terScorer;
    double TERTotalEdits = 0.0, TERTotalWords = 0.0;
    
    public GenerationPerformance(Event3Model model)
    {
        super(model);
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(model.getOpts().modifiedBleuScoreSize);
        meteorScorer = Meteor.MeteorScorerFactory();
        meteorAggStats = new MeteorStats();
        terScorer = new TERMetric();
    }

    @Override
    public void add(Widget trueWidget, Widget predWidget)
    {
        if(trueWidget != null)
        {
            String predStr = widgetToString(model.getWordIndexer(), (GenWidget)predWidget, model.getOpts().posAtSurfaceLevel, 
                    model.getOpts().tagDelimiter).toLowerCase();
//            String trueStr = widgetToString(model.getTestSetWordIndexer().isEmpty() ? model.getWordIndexer() : model.getTestSetWordIndexer(), (GenWidget)trueWidget, model.getOpts().tagDelimiter).toLowerCase();
            String trueStr = widgetToString(model.getTestSetWordIndexer().isEmpty() ? model.getWordIndexer() : model.getTestSetWordIndexer(), 
                    (GenWidget)trueWidget, model.getOpts().posAtSurfaceLevel, model.getOpts().tagDelimiter).toLowerCase();

//            String predModifiedStr = modifyPredStr(predStr, trueStr, (GenWidget) predWidget, (GenWidget) trueWidget);
            // Compute BLEU
            double bleuScore = bleuScorer.evaluateBleu(predStr, trueStr);
            // Compute modified BLEU (Don't penalise number deviations of 5 scalars)
            double bleuModifiedScore = bleuModifiedScorer.evaluateBleu(predStr, trueStr);
            // Compute METEOR
            MeteorStats meteorWidgetStats = meteorScorer.getMeteorStats(
                                      predStr,
                                      trueStr);
            meteorAggStats.addStats(meteorWidgetStats);
            // Compute TER
            TERalignment terWidgetStats = terScorer.getScore(predStr, trueStr);
            TERTotalEdits += terWidgetStats.numEdits;
            TERTotalWords += terWidgetStats.numWords;
            double terScore = terWidgetStats.score();
            // Compute Event Precision, Recall, F-measure
            EvalResult subResult = computeFmeasure((GenWidget)trueWidget, (GenWidget)predWidget);
            double precision = subResult.precision();
            double recall = subResult.recall();
            double f1 = subResult.f1();
            double wer = computeWer(trueWidget, predWidget);
            ((GenWidget)predWidget).scores[Parameters.BLEU_METRIC] = bleuScore;
            ((GenWidget)predWidget).scores[Parameters.BLEU_METRIC_MODIFIED] = bleuModifiedScore;
            ((GenWidget)predWidget).scores[Parameters.METEOR_METRIC] = meteorWidgetStats.score;
            ((GenWidget)predWidget).scores[Parameters.TER_METRIC] = terScore;
            ((GenWidget)predWidget).scores[Parameters.PRECISION_METRIC] = precision;
            ((GenWidget)predWidget).scores[Parameters.RECALL_METRIC] = recall;
            ((GenWidget)predWidget).scores[Parameters.F_MEASURE_METRIC] = f1;
            ((GenWidget)predWidget).scores[Parameters.WER_METRIC] = wer;
            trueWidget.performance = "BLEU score : " + bleuScore +
                                     "\tBLEU modified score : " + bleuModifiedScore +
                                     "\tMETEOR score : " + meteorWidgetStats.score +
                                     "\tTER score : " + terScore +
                                     "\tPrecision : " + precision +
                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1 +
                                     "\tWER : " + wer;

        }
    }

    private EvalResult computeFmeasure(GenWidget trueWidget, GenWidget predWidget)
    {
        EvalResult subResult = new EvalResult();
        Collection<Integer> predHit = new ArrayList<Integer>();
        int prev = -5, cur;
        for(int i = 0; i < predWidget.getEvents()[0].length; i++)
        {
            cur = new Integer(predWidget.getEvents()[0][i]);
            if(Parameters.isRealEvent(cur))
            {                
                if(prev != cur)
                    predHit.add(cur);
                prev = cur;
            }
        }
        // Get the things in common
        HashSet<Integer> set = new HashSet<Integer>(trueWidget.trueEvents.size());
        set.addAll(trueWidget.trueEvents);
//        Iterator<Integer> it = trueWidget.trueEvents.iterator();
        Iterator<Integer> it = set.iterator();
        while(it.hasNext())
        {
            Integer e = it.next();
            if(predHit.contains(e))
            {
                it.remove();
                predHit.remove(e);
                addResult(subResult, true, true);
            }
        } // for
        // Record differences between two sets
//        for(Integer e : trueWidget.trueEvents)
        for(Integer e : set)
        {
            addResult(subResult, true, false);
        }
        for(Integer e : predHit)
        {
            addResult(subResult, false, true);
        }
        return subResult;
    }
    
    public static String widgetToString(Indexer<String> wordIndexer, GenWidget widget, String tagDelimiter)
    {
        return widgetToString(wordIndexer, widget, false, tagDelimiter);
    }
    
    public static String widgetToString(Indexer<String> wordIndexer, GenWidget widget, boolean stripPosTags, String tagDelimiter)
    {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < widget.text.length; i++)
        {            
            out.append(widget.nums[i] > -1 ? widget.nums[i] :
                    WordModel.wordToString(wordIndexer, widget.text[i], stripPosTags, tagDelimiter)).append(" ");
        }
        return out.toString().trim();
    }
    
    /**
     * Print metrics
     */
    @Override
    public String output()
    {
        meteorScorer.computeMetrics(meteorAggStats);
        String out = "BLEU scores";
        out += "\n-----------\n";
        out += bleuScorer.getScore().toString();
        out += "\n\nModified BLEU scores";
        out += "\n-----------\n";
        out += bleuModifiedScorer.getScore().toString();
        out += "\n\nMETEOR scores";
        out += "\n-------------";
        out += "\nTest words:\t\t" + meteorAggStats.testLength;
        out += "\nReference words:\t" + meteorAggStats.referenceLength;
        out += "\nChunks:\t\t\t" + meteorAggStats.chunks;
        out += "\nPrecision:\t\t" + meteorAggStats.precision;
        out += "\nRecall:\t\t\t" + meteorAggStats.recall;
        out += "\nf1:\t\t\t" + meteorAggStats.f1;
        out += "\nfMean:\t\t\t" + meteorAggStats.fMean;
        out += "\nFragmentation penalty:\t" + meteorAggStats.fragPenalty;
        out += "\n";
        out += "\nFinal score:\t\t" + Fmt.D(meteorAggStats.score);

        out += "\n\nTER scores";
        out += "\n---------";
        out += "\nTotal TER: " + Fmt.D(TERTotalEdits / TERTotalWords) + " (" +
			   TERTotalEdits + "/" + TERTotalWords + ")";
	out += "\n\nNumber of calls to beam search: " + TERcalc.numBeamCalls();
	out += "\nNumber of segments scored: " + TERcalc.numSegsScored();
	out += "\nNumber of shifts tried: " + TERcalc.numShiftsTried();

        out += "\n\nPrecision - Recall - F-measure - Record WER";
        out += "\n------------------------------";
        out += "\nTotal Precision: " + Fmt.D(result.precision());
        out += "\nTotal Recall: " + Fmt.D(result.recall());
        out += "\nTotal F-measure: " + Fmt.D(result.f1());
        out += "\nTotal Record WER: " + Fmt.D(totalWer / (float) totalCounts);
        return out;
    }

    @Override
    public double getAccuracy()
    {
        meteorScorer.computeMetrics(meteorAggStats);
        return meteorAggStats.score;
    }

    @Override
    protected MyList<String> foreachStat()
    {
        MyList<String> list = new MyList();
        list.add( "BLEU-4", Utils.fmt(bleuScorer.getScore().getScore()) );
//        list.add( "METEOR", Utils.fmt(getAccuracy()) );
        return list;
    }

//    @Override
//    protected String summary()
//    {
//        return String.format("logZ = %s, logVZ = %s\n",
//                            Utils.fmt(stats.getAvg_logZ()),
//                            Utils.fmt(stats.getAvg_logVZ())
//                            );
//    }

//    @Override
//    protected void record(String name)
//    {
//        Utils.logs(name + ": " + summary());
//    }

    @Override
    public void output(String path)
    {
        Utils.write(path, output());
    }
}