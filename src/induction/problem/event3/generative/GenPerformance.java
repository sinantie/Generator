package induction.problem.event3.generative;

import induction.problem.event3.params.Parameters;
import edu.berkeley.nlp.mt.BatchBleuModifiedScorer;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import fig.basic.EvalResult;
import induction.MyList;
import induction.Utils;
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
public class GenPerformance extends Performance
{
    BatchBleuScorer bleuScorer, bleuModifiedScorer;
    MeteorScorer meteorScorer;
    MeteorStats meteorAggStats;
    TERMetric terScorer;
    double TERTotalEdits = 0.0, TERTotalWords = 0.0;

    public GenPerformance(Event3Model model)
    {
        super(model);
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(model.getOpts().modifiedBleuScoreSize);
        meteorScorer = Meteor.MeteorScorerFactory();
        meteorAggStats = new MeteorStats();
        terScorer = new TERMetric();
    }

    @Override
    protected void add(Widget trueWidget, Widget predWidget)
    {
        if(trueWidget != null)
        {
            String predStr = widgetToString((GenWidget)predWidget);
            String trueStr = widgetToString((GenWidget)trueWidget);

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
            
            ((GenWidget)predWidget).scores[Parameters.BLEU_METRIC] = bleuScore;
            ((GenWidget)predWidget).scores[Parameters.BLEU_METRIC_MODIFIED] = bleuModifiedScore;
            ((GenWidget)predWidget).scores[Parameters.METEOR_METRIC] = meteorWidgetStats.score;
            ((GenWidget)predWidget).scores[Parameters.TER_METRIC] = terScore;
            ((GenWidget)predWidget).scores[Parameters.PRECISION_METRIC] = precision;
            ((GenWidget)predWidget).scores[Parameters.RECALL_METRIC] = recall;
            ((GenWidget)predWidget).scores[Parameters.F_MEASURE_METRIC] = f1;
            trueWidget.performance = "BLEU score : " + bleuScore +
                                     "\tBLEU modified score : " + bleuModifiedScore +
                                     "\tMETEOR score : " + meteorWidgetStats.score +
                                     "\tTER score : " + terScore +
                                     "\tPrecision : " + precision +
                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1;

        }
    }

    private EvalResult computeFmeasure(GenWidget trueWidget, GenWidget predWidget)
    {
        EvalResult subResult = new EvalResult();
        Collection<Integer> predHit = new ArrayList<Integer>();
        int prev = -5, cur;
        for(int i = 0; i < predWidget.events[0].length; i++)
        {
            cur = new Integer(predWidget.events[0][i]);
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

    public static String widgetToString(GenWidget widget)
    {
        String out = "";
        for(int i = 0; i < widget.text.length; i++)
        {
            out += (widget.nums[i] > -1 ? widget.nums[i] :
                    Event3Model.wordToString(widget.text[i])) + " ";
        }
        return out.trim();
    }

    /**
     * Print metrics
     */
    @Override
    protected String output()
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
        out += "\nFinal score:\t\t" + meteorAggStats.score;

        out += "\n\nTER scores";
        out += "\n---------";
        out += "\nTotal TER: " + (TERTotalEdits / TERTotalWords) + " (" +
			   TERTotalEdits + "/" + TERTotalWords + ")";
	out += "\n\nNumber of calls to beam search: " + TERcalc.numBeamCalls();
	out += "\nNumber of segments scored: " + TERcalc.numSegsScored();
	out += "\nNumber of shifts tried: " + TERcalc.numShiftsTried();

        out += "\n\nPrecision - Recall - F-measure";
        out += "\n------------------------------";
        out += "\nTotal Precision: " + result.precision();
        out += "\nTotal Recall: " + result.recall();
        out += "\nTotal F-measure: " + result.f1();
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
    protected void output(String path)
    {
        Utils.write(path, output());
    }

    //    private String modifyPredStr(String predStr, String trueStr,
//                                 GenWidget predWidget, GenWidget trueWidget)
//    {
//        String[] predAr = predStr.split(" ");
//        String[] trueAr = trueStr.split(" ");
//        String tokenPr, tokenTr;
//        ArrayList<Integer> identifiedIndices = new ArrayList<Integer>();
//        int event, startIndex = -1, endIndex = -1;
//        boolean changesMade = false;
//        // parse each token of the predicted string and match any numbers
//        for(int i = 0; i < predAr.length; i++)
//        {
//            tokenPr = predAr[i];
//            // grab the event and identify the corresponding window of tokens
//            // in the true string
//            if(tokenPr.matches("\\p{Digit}+"))
//            {
//                event = predWidget.events[0][i]; // we are always on track 0
//                for(int c = 0; c < trueWidget.events.length; c++)
//                {
//                    startIndex = endIndex = -1;
//                    for(int k = 0; k < trueWidget.events[0].length; k++)
//                    {
//                        // indentify start and end index in the true string
//                        if(trueWidget.events[c][k] == event)
//                        {
//                            startIndex = k;
//                            while( k < trueWidget.events[0].length && trueWidget.events[c][k] == event)
//                            {
//                                k++;
//                            }
//                            endIndex = k;
//                            break;
//                        } // if
//                    } // for
//                    // search for numbers in the identified window of the true string
//                    for(int j = startIndex; j < endIndex; j++)
//                    {
//                        tokenTr = trueAr[j];
//                        // if we have not matched this number before
//                        if(tokenTr.matches("\\p{Digit}+") && !identifiedIndices.contains(j))
//                        {
//                            // matched a deviation between numbers smaller than 5
//                            if(Math.abs(Integer.valueOf(tokenPr) - Integer.valueOf(tokenTr)) <= 5 )
//                            {
//                                predAr[i] = tokenTr;
//                                changesMade = true;
//                                identifiedIndices.add(j);
//                            }
//                        }
//                    }
//                } // for
//            } // if
//        } // for
//        if(changesMade)
//        {
//            String out = "";
//            for(String s : predAr)
//                out += s + " ";
//            return out.trim();
//        }
//        return predStr;
//    }
}