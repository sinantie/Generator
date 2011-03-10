package induction.problem.event3;

import induction.problem.event3.params.Parameters;
import fig.basic.EvalResult;
import induction.MyList;
import induction.Utils;
import induction.problem.AExample;
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
    public SemParsePerformance(Event3Model model)
    {
        super(model);
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
            EvalResult subResult = computeFmeasure(ex, (SemParseWidget)trueWidget, predW);
            double precision = subResult.precision();
            double recall = subResult.recall();
            double f1 = subResult.f1();              
            predW.scores[Parameters.PRECISION_METRIC] = precision;
            predW.scores[Parameters.RECALL_METRIC] = recall;
            predW.scores[Parameters.F_MEASURE_METRIC] = f1;
            trueWidget.performance = "\tPrecision : " + precision +
                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1;

        }
    }

    private EvalResult computeFmeasure(Example ex, SemParseWidget trueWidget,
                                       SemParseWidget predWidget)
    {
        EvalResult subResult = new EvalResult();
        Collection<MRToken> predMrTokens = parseMrTokens(ex, predWidget);
        Collection<MRToken> trueMrTokens = trueWidget.trueMrTokens;
        Iterator<MRToken> predIterator = predMrTokens.iterator();
        while(predIterator.hasNext())
        {
            MRToken predMrToken = predIterator.next();
            if(trueMrTokens.contains(predMrToken))
            {
                addResult(subResult, true, true);
                trueMrTokens.remove(predMrToken);
            }
            else
            {
                addResult(subResult, false, true);
            }
            predIterator.remove();
        }
        for(int i = 0; i < trueMrTokens.size(); i++)
        {
            addResult(subResult, true, false);
        }
        return subResult;
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

        for(int i = 0; i < widget.events[0].length; i++)
        {
            // ugly, but it means to get the index of the type of the predicted event
            // at the i'th position of widget.events[0]
            int eventIndex = widget.events[0][i];
            if(eventIndex > -1)
            {
                curEvent = ex.events[eventIndex].getEventTypeIndex();
                curField = widget.fields[0][i];
                curValue = widget.nums[i] > -1 ? widget.nums[i] : widget.text[i];
                if(!map.containsKey(curEvent))
                {
                    MRToken mr = new MRToken(model, curEvent);
                    mr.parseMrToken(curEvent, curField, curValue);
                    map.put(curEvent, mr);
                }
                else
                {
                    map.get(curEvent).parseMrToken(curEvent, curField, curValue);
                }
            }
        }
        return map.values();
    }      
}