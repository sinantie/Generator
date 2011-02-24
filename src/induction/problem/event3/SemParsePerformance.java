package induction.problem.event3;

import induction.problem.event3.params.Parameters;
import fig.basic.EvalResult;
import induction.MyList;
import induction.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    protected void add(Widget trueWidget, Widget predWidget)
    {
        if(trueWidget != null)
        {
 
            // Compute Event Precision, Recall, F-measure
            EvalResult subResult = computeFmeasure((GenWidget)trueWidget, (GenWidget)predWidget);
            double precision = subResult.precision();
            double recall = subResult.recall();
            double f1 = subResult.f1();              
            ((GenWidget)predWidget).scores[Parameters.PRECISION_METRIC] = precision;
            ((GenWidget)predWidget).scores[Parameters.RECALL_METRIC] = recall;
            ((GenWidget)predWidget).scores[Parameters.F_MEASURE_METRIC] = f1;
            trueWidget.performance = "\tPrecision : " + precision +
                                     "\tRecall: " + recall +
                                     "\tF-measure : " + f1;

        }
    }

    private EvalResult computeFmeasure(GenWidget trueWidget, GenWidget predWidget)
    {
        EvalResult subResult = new EvalResult();
        Collection<Integer> predHit = new ArrayList<Integer>();

        ArrayList<MRToken> trueMrTokens = parseMrTokens(trueWidget);
        ArrayList<MRToken> predMrTokens = parseMrTokens(predWidget);
        Iterator predIterator = predMrTokens.iterator();
        while(predIterator.hasNext())
        {
            MRToken predMrToken = (MRToken) predIterator.next();
            if(trueMrTokens.contains(predMrToken))
            {
                addResult(subResult, true, true);
            }
            else
            {
                addResult(subResult, false, true);
            }
            predIterator.remove();
        }

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

    private ArrayList<MRToken> parseMrTokens(GenWidget widget)
    {
        // we can only parse a single MR per sentence at the moment
        HashMap<Integer, MRToken> map = new HashMap<Integer, MRToken>();
        int curEvent, curField, curValue;
        MRToken mr;
        for(int i = 0; i < widget.events[0].length; i++)
        {
            curEvent = widget.events[0][i];
            curField = widget.fields[0][i];
            curValue = widget.text[i];
            if(!map.containsKey(i))
            {
                mr = new MRToken(curEvent);
                if(curField < model.getEventTypes()[curEvent].F)
                    mr.addField(curField);
                if(curValue > -1)
                    mr.addValue(curField, curValue);
                map.put(i, mr);
            }
            else
            {
                mr = map.get(i);
                if(curField < model.getEventTypes()[curEvent].F)
                    mr.addField(curField);
                if(curValue > -1)
                    mr.addValue(curField, curValue);
            }
        }
        return (ArrayList<MRToken>) map.values();
    }

    class MRToken
    {
        private int event;
        private HashMap<Integer, ArrayList<Integer>> fields;

        public MRToken(int event)
        {
            this.event = event;
            this.fields = new HashMap<Integer, ArrayList<Integer>>();
        }

        public void addField(int fieldId)
        {
            if(!fields.containsKey(fieldId))
                fields.put(fieldId, new ArrayList<Integer>());
        }

        public void addValue(int fieldId, int value)
        {
            fields.get(fieldId).add(value);
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof MRToken;
            MRToken mr = (MRToken)obj;
            if(this.event != mr.event)
                return false;
            for(Integer fieldId : this.fields.keySet())
            {
                if(!mr.fields.containsKey(fieldId))
                    return false;
                // normally not a list
                ArrayList<Integer> thisValues = fields.get(fieldId);
                ArrayList<Integer> mrValues = mr.fields.get(fieldId);
                // change that, very naive
                if(thisValues.size() != mrValues.size())
                    return false;
                for(Integer value : thisValues)
                {
                    if(!mrValues.contains(value))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 79 * hash + this.event;
            hash = 79 * hash + (this.fields != null ? this.fields.hashCode() : 0);
            return hash;
        }


    }
}