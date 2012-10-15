package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.CFGRule;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author sinantie
 */
public class CFGParams extends AParams
{
    Map<Integer, Vec> cfgRulesChoices; // map of rules indexed on the lhs nonterminal symbol      
    public int none_t;
    
    public CFGParams(Event3Model model, VecFactory.Type vectorType)
    {
        super(model);          
        cfgRulesChoices = new HashMap();
        none_t = model.none_t();
        for (Entry<Integer, HashMap<CFGRule, Integer>> rule : model.getCfgRules().entrySet())
        {
            Vec v = VecFactory.zeros(vectorType, rule.getValue().size());
            cfgRulesChoices.put(rule.getKey(), v);
            addVec("cfgRulesChoices " + model.getRulesIndexer().getObject(rule.getKey()), v);
        } // for        
    }

    public Map<Integer, Vec> getCfgRulesChoices()
    {
        return cfgRulesChoices;
    }

    @Override
    public String output(ParamsType paramsType)
    {
        Event3Model event3Model = (Event3Model) this.model;
        StringBuilder out = new StringBuilder();
        for (Entry<Integer, Vec> rule : cfgRulesChoices.entrySet())
        {
            String lhs = event3Model.getRulesIndexer().getObject(rule.getKey());
            String[] lab = getLabels(rule.getValue().size(), "cfgRulesChoices " + lhs,
                    event3Model.cfgRulesRhsStrArray(event3Model.getCfgRules().get(rule.getKey())));
            if (paramsType == ParamsType.PROBS)            
                out.append(forEachProb(rule.getValue(), lab));            
            else            
                out.append(forEachCount(rule.getValue(), lab));            
        } // for
        return out.toString();
    }

    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        Event3Model event3Model = (Event3Model) this.model;
        StringBuilder out = new StringBuilder();
        // treebank rules
        for (Entry<Integer, Vec> rule : cfgRulesChoices.entrySet())
        {
            String lhs = event3Model.getRulesIndexer().getObject(rule.getKey());
            String[] lab = getLabels(rule.getValue().size(), "cfgRulesChoices " + lhs,
                    event3Model.cfgRulesRhsStrArray(event3Model.getCfgRules().get(rule.getKey())));
            if (paramsType == ParamsType.PROBS)           
                out.append(forEachProbNonZero(rule.getValue(), lab));            
            else            
                out.append(forEachCountNonZero(rule.getValue(), lab));            
        } // for
        return out.toString();
    }
}