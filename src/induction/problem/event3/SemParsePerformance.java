package induction.problem.event3;

import induction.problem.event3.params.Parameters;
import fig.basic.EvalResult;
import induction.MyList;
import induction.Utils;
import induction.problem.AExample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Compute precision, recall and f-measure of the semantic parse of a text
 * given gold standard semantic correspondences.
 *
 * @author konstas
 */
public class SemParsePerformance extends Performance
{
    EvalResult eventsResult, eventsSubResult;
    EvalResult fieldsResult, fieldsSubResult;
    EvalResult valuesResult, valuesSubResult;

    public SemParsePerformance(Event3Model model)
    {
        super(model);
        eventsResult = new EvalResult();
        fieldsResult = new EvalResult();
        valuesResult = new EvalResult();
    }

    @Override
    protected void add(AExample example, Widget predWidget)
    {
        Example ex = (Example)example;
        Widget trueWidget = ex.getTrueWidget();
        if(trueWidget != null)
        {
            SemParseWidget predW = (SemParseWidget) predWidget;
            // Compute Event Precision, Recall, F-measure
            Collection<MRToken> predMrTokens = parseMrTokens(ex, predW);
            Collection<MRToken> trueMrTokens = ((SemParseWidget) trueWidget).trueMrTokens;
            
            double evPrecision = eventsSubResult.precision();
            double evRecall = eventsSubResult.recall();
            double evF1 = eventsSubResult.f1();
            predW.scores[Parameters.EVENT_F_MEASURE_METRIC] = evF1;

            EvalResult exactResult = computeFmeasure(trueMrTokens, predMrTokens);
            double precision = exactResult.precision();
            double recall = exactResult.recall();
            double f1 = exactResult.f1();
            
            predW.scores[Parameters.PRECISION_METRIC] = precision;
            predW.scores[Parameters.RECALL_METRIC] = recall;
            predW.scores[Parameters.F_MEASURE_METRIC] = f1;
            
            trueWidget.performance = "\tPrecision : " + precision +
                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1 +
                                     "\tEvent Match Precision : " + evPrecision +
                                     "\tEvent Match Recall : " + evRecall +
                                     "\tEvent Match F-measure : " + evF1;

        }
    }
   
    private EvalResult computeIndividualFMeasure(Collection trueList, Collection predList,
                                EvalResult totalResult)

    {
        EvalResult subResult = new EvalResult();
        Iterator predIterator = predList.iterator();
        while(predIterator.hasNext())
        {
            Object predItem = predIterator.next();
            if(trueList.contains(predItem))
            {
                addResult(totalResult, subResult, true, true);
                trueList.remove(predItem);
            }
            else
            {
                addResult(totalResult, subResult, false, true);
            }
            predIterator.remove();
        }
        for(int i = 0; i < trueList.size(); i++)
        {
            addResult(totalResult, subResult, true, false);
        }
        return subResult;
    }

//    private EvalResult computeFmeasure(Example ex, SemParseWidget trueWidget,
//                                       SemParseWidget predWidget)
    private EvalResult computeFmeasure(Collection<MRToken> trueMrTokens,
                                       Collection<MRToken> predMrTokens)
    {
        EvalResult exactSubResult = new EvalResult();
        EvalResult fieldsSubtotalResult = new EvalResult();
        EvalResult valuesSubtotalResult = new EvalResult();
        // compute event f-measure
        Collection<Integer> predEvents = new ArrayList<Integer>(predMrTokens.size());
        for(MRToken mr : predMrTokens)
        {
            if(!mr.isEmpty())
                predEvents.add(mr.getEvent());
        }
        Collection<Integer> trueEvents = new ArrayList<Integer>(trueMrTokens.size());
        for(MRToken mr : trueMrTokens)
            trueEvents.add(mr.getEvent());
        eventsSubResult = computeIndividualFMeasure(trueEvents, predEvents, eventsResult);

        // compute exact F-measure simultaneously with fields' and values' f-measure
        Iterator<MRToken> predIterator = predMrTokens.iterator();
        while(predIterator.hasNext())
        {
            MRToken predMrToken = predIterator.next();
            if(trueMrTokens.contains(predMrToken))
            {

                addResult(exactSubResult, true, true);
                trueMrTokens.remove(predMrToken);
            }
            else
            {
                addResult(exactSubResult, false, true);
            }
            predIterator.remove();
        }
        for(int i = 0; i < trueMrTokens.size(); i++)
        {
            addResult(exactSubResult, true, false);
        }
        return exactSubResult;
    }
   
    /**
     * Print metrics
     */
    @Override
    protected String output()
    {
        String out = "\n\nPrecision - Recall - F-measure";
        out += "\n------------------------------";
        out += "\nTotal Precision: " + result.precision();
        out += "\nTotal Recall: " + result.recall();
        out += "\nTotal F-measure: " + result.f1();
        out += "\nTotal Event Match Precision: " + eventsResult.precision();
        out += "\nTotal Event Match Recall: " + eventsResult.recall();
        out += "\nTotal Event Match F-measure: " + eventsResult.f1();
        return out;
    }

    @Override
    public double getAccuracy()
    {
        return result.f1();
    }

    @Override
    protected MyList<String> foreachStat()
    {
        MyList<String> list = new MyList();
        list.add( "Precision", Utils.fmt(result.precision()) );
        list.add( "Recall", Utils.fmt(result.recall()) );
        list.add( "F-measure", Utils.fmt(getAccuracy()) );
        list.add( "Event Match Precision", Utils.fmt(eventsResult.precision()) );
        list.add( "Event Match Recall", Utils.fmt(eventsResult.recall()) );
        list.add( "Event Match F-measure", Utils.fmt(eventsResult.f1()) );
        return list;
    }

    @Override
    protected void output(String path)
    {
        Utils.write(path, output());
    }

    private Collection<MRToken> parseMrTokens(Example ex, SemParseWidget widget)
    {
        // we can only parse a single MR per sentence at the moment
        HashMap<Integer, MRToken> map = new HashMap<Integer, MRToken>();
        int curEvent, curField, curValue;
        MRToken.Type curType;
        for(int i = 0; i < widget.events[0].length; i++)
        {
            // ugly, but it means to get the index of the type of the predicted event
            // at the i'th position of widget.events[0]
            int eventIndex = widget.events[0][i];
            if(eventIndex > -1)
            {
                curEvent = ex.events.get(eventIndex).getEventTypeIndex();
                curField = widget.fields[0][i];
                if(widget.nums[i] > -1)
                {
                    curValue = widget.nums[i];
                    curType = MRToken.Type.num;
                }
                else
                {
                    curValue = widget.text[i];
                    curType = MRToken.Type.cat;
                }
//                curValue = widget.nums[i] > -1 ? widget.nums[i] : widget.text[i];
                if(!map.containsKey(curEvent))
                {
                    MRToken mr = new MRToken(model, curEvent);
                    mr.parseMrToken(curEvent, curField, curType, curValue);
                    map.put(curEvent, mr);
                }
                else
                {
                    map.get(curEvent).parseMrToken(curEvent, curField, curType, curValue);
                }
            }
        }
        return map.values();
    }      
}