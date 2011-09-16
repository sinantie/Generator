package induction.problem.event3.generative.generation;

import induction.problem.event3.generative.alignment.Performance;
import induction.problem.event3.Event3Model;
import induction.problem.event3.params.Parameters;
import fig.basic.EvalResult;
import induction.MyList;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.MRToken;
import induction.problem.event3.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Compute precision, recall and f-measure of the semantic parse of a text
 * given gold standard semantic correspondences.
 *
 * @author konstas
 */
public class SemParsePerformance extends Performance
{
    EvalResult eventsResult;
    EvalResult fieldsResult;
    EvalResult valuesResult;
   
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
            Map<Integer, MRToken> predMrTokens = parseMrTokens(ex, predW);
//            Collection<MRToken> trueMrTokens = ((SemParseWidget) trueWidget).trueMrTokens;
            Map<Integer, MRToken> trueMrTokens = new HashMap<Integer, MRToken>();
            for(MRToken mr : ((SemParseWidget) trueWidget).getTrueMrTokens())
            {
                trueMrTokens.put(mr.getEvent(), mr);
            }
            EvalResult eventsSubResult = new EvalResult();
            EvalResult fieldsSubResult = new EvalResult();
            EvalResult valuesSubResult = new EvalResult();
            EvalResult exactSubResult = new EvalResult();
            computeFmeasure(trueMrTokens, predMrTokens, eventsSubResult,
                    fieldsSubResult, valuesSubResult, exactSubResult);

            double evPrecision = eventsSubResult.precision();
            double evRecall = eventsSubResult.recall();
            double evF1 = eventsSubResult.f1();
            double fPrecision = fieldsSubResult.precision();
            double fRecall = fieldsSubResult.recall();
            double fF1 = fieldsSubResult.f1();
            double vPrecision = valuesSubResult.precision();
            double vRecall = valuesSubResult.recall();
            double vF1 = valuesSubResult.f1();
            double precision = exactSubResult.precision();
            double recall = exactSubResult.recall();
            double f1 = exactSubResult.f1();
            
            predW.scores[Parameters.PRECISION_METRIC] = precision;
            predW.scores[Parameters.RECALL_METRIC] = recall;
            predW.scores[Parameters.F_MEASURE_METRIC] = f1;
            predW.scores[Parameters.EVENT_F_MEASURE_METRIC] = evF1;
            predW.scores[Parameters.FIELD_F_MEASURE_METRIC] = fF1;
            predW.scores[Parameters.VALUE_F_MEASURE_METRIC] = vF1;
            trueWidget.performance =
//                                     "\tPrecision : " + precision +
//                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1 +
//                                     "\tEvent Precision : " + evPrecision +
//                                     "\tEvent Recall : " + evRecall +
                                     "\tEvent F-measure : " + evF1 +
//                                     "\tField Precision : " + fPrecision +
//                                     "\tField Recall : " + fRecall +
                                     "\tField F-measure : " + fF1 +
//                                     "\tValue Precision : " + vPrecision +
//                                     "\tValue Recall : " + vRecall +
                                     "\tValue F-measure : " + vF1;

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

    private void computeFmeasure(Map<Integer, MRToken> trueMrTokens,
                               Map<Integer, MRToken> predMrTokens,
                               EvalResult eventsSubResult, EvalResult fieldsSubResult,
                               EvalResult valuesSubResult, EvalResult exactSubResult)
    {                
        // compute event f-measure
        Collection<Integer> predEvents = new ArrayList<Integer>(predMrTokens.size());
        for(MRToken mr : predMrTokens.values())
        {
            if(!mr.isEmpty())
                predEvents.add(mr.getEvent());
        }
        Collection<Integer> trueEvents = new ArrayList<Integer>(trueMrTokens.size());
        for(MRToken mr : trueMrTokens.values())
            trueEvents.add(mr.getEvent());

        // compute events', fields' and values' f-measure simultaneously
        Iterator<Integer> predIterator = predEvents.iterator();
        while(predIterator.hasNext())
        {
            Integer predEvent = predIterator.next();
            if(trueEvents.contains(predEvent))
            {
                MRToken predMr = predMrTokens.get(predEvent);
                Collection<Integer> predFields = predMr.getFieldIds();
                MRToken trueMr = trueMrTokens.get(predEvent);
                Collection<Integer> trueFields = trueMr.getFieldIds();

                for(Integer id : predFields)
                {
                    if(trueFields.contains(id))
                        valuesSubResult.add(computeIndividualFMeasure(trueMr.getValuesOfField(id),
                                              predMr.getValuesOfField(id), valuesResult));
                }
                fieldsSubResult.add(computeIndividualFMeasure(trueFields, predFields, fieldsResult));

                addResult(eventsResult, eventsSubResult, true, true);
                trueEvents.remove(predEvent);
            }
            else
            {
                addResult(eventsResult, eventsSubResult, false, true);
            }
            predIterator.remove();
        }
        for(int i = 0; i < trueEvents.size(); i++)
        {
            addResult(eventsResult, eventsSubResult, true, false);
        }
//        eventsSubResult.add(computeIndividualFMeasure(trueEvents, predEvents, eventsResult));


        // compute exact F-measure
        exactSubResult.add(computeIndividualFMeasure(trueMrTokens.values(), predMrTokens.values(), result));
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

        out += "\n\nTotal Event Precision: " + eventsResult.precision();
        out += "\nTotal Event Recall: " + eventsResult.recall();
        out += "\nTotal Event F-measure: " + eventsResult.f1();
        
        out += "\n\nTotal Field Precision: " + fieldsResult.precision();
        out += "\nTotal Field Recall: " + fieldsResult.recall();
        out += "\nTotal Field F-measure: " + fieldsResult.f1();
        
        out += "\n\nTotal Values Precision: " + valuesResult.precision();
        out += "\nTotal Values Recall: " + valuesResult.recall();
        out += "\nTotal Values F-measure: " + valuesResult.f1();
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
        list.add( "Event Precision", Utils.fmt(eventsResult.precision()) );
        list.add( "Event Recall", Utils.fmt(eventsResult.recall()) );
        list.add( "Event F-measure", Utils.fmt(eventsResult.f1()) );
        list.add( "Field Precision", Utils.fmt(fieldsResult.precision()) );
        list.add( "Field Recall", Utils.fmt(fieldsResult.recall()) );
        list.add( "Field F-measure", Utils.fmt(fieldsResult.f1()) );
        list.add( "Value Precision", Utils.fmt(valuesResult.precision()) );
        list.add( "Value Recall", Utils.fmt(valuesResult.recall()) );
        list.add( "Value F-measure", Utils.fmt(valuesResult.f1()) );
        return list;
    }

    @Override
    protected void output(String path)
    {
        Utils.write(path, output());
    }

    private Map<Integer, MRToken> parseMrTokens(Example ex, SemParseWidget widget)
    {
        // we can only parse a single MR per sentence at the moment
        HashMap<Integer, MRToken> map = new HashMap<Integer, MRToken>();
        int curEvent, curField, curValue;
        MRToken.Type curType;
        for(int i = 0; i < widget.getEvents()[0].length; i++)
        {
            // ugly, but it means to get the index of the type of the predicted event
            // at the i'th position of widget.events[0]
            int eventIndex = widget.getEvents()[0][i];
            if(eventIndex > -1)
            {
                curEvent = ex.events.get(eventIndex).getEventTypeIndex();
                curField = widget.getFields()[0][i];
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
        return map;
    }      
}