package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.PCFGRule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author sinantie
 */
public class TrackParams extends AParams
{
    Vec[] eventTypeChoices, noneEventTypeBigramChoices;
    Vec noneEventTypeEmissions;
    Map<Integer, Vec> pcfgRulesChoices;
    private int T, W, c;
    public int none_t, boundary_t;

    public TrackParams(Event3Model model, int c, VecFactory.Type vectorType)
    {
        super(model);
        this.T = model.getT(); this.W = model.W(); this.c = c;
        none_t = model.none_t();
        boundary_t = model.boundary_t();
        // treebank rules
        if(model.getPcfgRules() != null)
        {
            pcfgRulesChoices = new HashMap();
            for(Entry<Integer, List<PCFGRule>> rule : model.getPcfgRules().entrySet())
            {
                Vec v = VecFactory.zeros(vectorType, rule.getValue().size());
                pcfgRulesChoices.put(rule.getKey(), v);
                addVec("pcfgRulesChoices " + model.getRulesIndexer().getObject(rule.getKey()), v);                
            } // for
        } // if
        // t_0, t -> choose event of type t given we were in type t_0        
        eventTypeChoices = VecFactory.zeros2(vectorType, T + 2, T + 2);        
        addVec(getLabels(T + 2, "eventTypeChoices["+c+"]", model.eventTypeStrArray()), eventTypeChoices);
        // w -> generate word w
        noneEventTypeEmissions = VecFactory.zeros(vectorType, W);
        addVec("noneEventTypeEmissions["+c+"]", noneEventTypeEmissions);
        noneEventTypeBigramChoices = VecFactory.zeros2(vectorType, W, W);
        addVec(getLabels(W, "noneFieldWordBiC["+c+"] ",
                          model.wordsToStringArray()), noneEventTypeBigramChoices);
    }

    public Map<Integer, Vec> getPcfgRulesChoices()
    {
        return pcfgRulesChoices;
    }
    
    public Vec[] getEventTypeChoices()
    {
        return eventTypeChoices;
    }

    public Vec getNoneEventTypeEmissions()
    {
        return noneEventTypeEmissions;
    }

    public Vec[] getNoneEventTypeBigramChoices()
    {
        return noneEventTypeBigramChoices;
    }

    @Override
    public String output(ParamsType paramsType)
    {
        Event3Model event3Model = (Event3Model)this.model;
        String[] words = event3Model.wordsToStringArray();
        StringBuilder out = new StringBuilder();        
        String[][] labels = getLabels(T+2, T+2, "eventTypeC [" + event3Model.cstr(c) + "] ",
                event3Model.eventTypeStrArray(), event3Model.eventTypeStrArray());
        int i = 0;
        for(Vec v : eventTypeChoices)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProb(v, labels[i++]));
            else
                out.append(forEachCount(v, labels[i++]));
        }
        // treebank rules
        if(pcfgRulesChoices != null)
        {
            for(Entry<Integer, Vec> rule : pcfgRulesChoices.entrySet())
            {
                String lhs = event3Model.getRulesIndexer().getObject(rule.getKey());
                String[] lab = getLabels(rule.getValue().size(), "pcfgRulesChoices " + lhs, 
                        event3Model.pcfgRulesRhsStrArray(event3Model.getPcfgRules().get(rule.getKey())));
                if(paramsType == ParamsType.PROBS)
                    out.append(forEachProb(rule.getValue(), lab));
                else
                    out.append(forEachCount(rule.getValue(), lab));                
            } // for
        } // if
        if(paramsType == ParamsType.PROBS)
            out.append("\n").append(forEachProb(noneEventTypeEmissions,
                          getLabels(W, "noneEventTypeE [" + event3Model.cstr(c) + "] ", words)));
        else
            out.append("\n").append(forEachCount(noneEventTypeEmissions,
                          getLabels(W, "noneEventTypeE [" + event3Model.cstr(c) + "] ", words)));
        i = 0;
        // if too huge parameter set, comment
//        String[][] labelsNone = getLabels(W, W, "noneEventTypeWordBiC [" + model.cstr(c) + "] ",
//              Event3Model.wordsToStringArray(), Event3Model.wordsToStringArray());
//        for(ProbVec v : noneEventTypeBigramChoices)
//        {
//            out += forEachProb(v, labelsNone[i++]);
//        }
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        Event3Model event3Model = (Event3Model)this.model;
        String[] words = event3Model.wordsToStringArray();
        StringBuilder out = new StringBuilder();
        String[][] labels = getLabels(T+2, T+2, "eventTypeC [" + event3Model.cstr(c) + "] ",
                event3Model.eventTypeStrArray(), event3Model.eventTypeStrArray());
        int i = 0;
        for(Vec v : eventTypeChoices)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProbNonZero(v, labels[i++]));
            else
                out.append(forEachCountNonZero(v, labels[i++]));
        }
        // treebank rules
        if(pcfgRulesChoices != null)
        {
            for(Entry<Integer, Vec> rule : pcfgRulesChoices.entrySet())
            {
                String lhs = event3Model.getRulesIndexer().getObject(rule.getKey());
                String[] lab = getLabels(rule.getValue().size(), "pcfgRulesChoices " + lhs, 
                        event3Model.pcfgRulesRhsStrArray(event3Model.getPcfgRules().get(rule.getKey())));
                if(paramsType == ParamsType.PROBS)
                    out.append(forEachProbNonZero(rule.getValue(), lab));
                else
                    out.append(forEachCountNonZero(rule.getValue(), lab));                
            } // for
        } // if
        if(paramsType == ParamsType.PROBS)
            out.append("\n").append(forEachProbNonZero(noneEventTypeEmissions,
                          getLabels(W, "noneEventTypeE [" + event3Model.cstr(c) + "] ", words)));
        else
            out.append("\n").append(forEachCountNonZero(noneEventTypeEmissions,
                          getLabels(W, "noneEventTypeE [" + event3Model.cstr(c) + "] ", words)));
        i = 0;
        // if too huge parameter set, comment
//        String[][] labelsNone = getLabels(W, W, "noneEventTypeWordBiC [" + model.cstr(c) + "] ",
//              Event3Model.wordsToStringArray(), Event3Model.wordsToStringArray());
//        for(ProbVec v : noneEventTypeBigramChoices)
//        {
//            out += forEachProb(v, labelsNone[i++]);
//        }
        return out.toString();
    }
}
