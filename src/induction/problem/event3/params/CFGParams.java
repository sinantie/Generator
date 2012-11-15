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
    // map of rules indexed on their name; used for excluding from optimization (fixRecordSelection)
    Map<String, Vec> cfgRulesChoicesMap = new HashMap<String, Vec>(); 
    Vec[] wordsPerRootRule;
    public int none_t;
    int binSize, maxDocLength, numOfBins;
    
    public CFGParams(Event3Model model, VecFactory.Type vectorType)
    {
        super(model);          
        cfgRulesChoices = new HashMap();
        none_t = model.none_t();
        for (Entry<Integer, HashMap<CFGRule, Integer>> rule : model.getCfgRules().entrySet())
        {
            Vec v = VecFactory.zeros(vectorType, rule.getValue().size());
            cfgRulesChoices.put(rule.getKey(), v);
//            addVec("cfgRulesChoices " + model.getRulesIndexer().getObject(rule.getKey()), v);
            cfgRulesChoicesMap.put("cfgRulesChoices " + model.getRulesIndexer().getObject(rule.getKey()), v);
        } // for  
        addVec(cfgRulesChoicesMap);
        binSize = model.getOpts().docLengthBinSize;
        numOfBins = model.getOpts().maxDocLength/binSize;
        Map<CFGRule, Integer> rootRules = model.getCfgCandidateRules(model.getRulesIndexer().getIndex("S"));
        int numOfRootRules = rootRules.size();
        wordsPerRootRule = VecFactory.zeros2(vectorType, numOfRootRules, numOfBins);
        addVec(getLabels(numOfRootRules, "wordsPerRootRule ", model.cfgRulesRhsStrArray(rootRules)), wordsPerRootRule);                
    }

    public Map<Integer, Vec> getCfgRulesChoices()
    {
        return cfgRulesChoices;
    }

    public Map<String, Vec> getCfgRulesChoicesMap()
    {
        return cfgRulesChoicesMap;
    }

    public Vec[] getWordsPerRootRule()
    {
        return wordsPerRootRule;
    }

    public int getNumOfBins()
    {
        return numOfBins;
    }
    
    @Override
    public String output(ParamsType paramsType)
    {
        Event3Model event3Model = (Event3Model) this.model;
        StringBuilder out = new StringBuilder();
        for (Entry<Integer, Vec> rule : cfgRulesChoices.entrySet())
        {
            String lhs = event3Model.getRulesIndexer().getObject(rule.getKey());
            String[] lab = getLabels(rule.getValue().size(), "cfgRulesChoices " + lhs + " ",
                    event3Model.cfgRulesRhsStrArray(event3Model.getCfgRules().get(rule.getKey())));
            if (paramsType == ParamsType.PROBS)            
                out.append(forEachProb(rule.getValue(), lab));            
            else            
                out.append(forEachCount(rule.getValue(), lab));            
        } // for
        Map<CFGRule, Integer> rootRules = event3Model.getCfgCandidateRules(event3Model.getRulesIndexer().getIndex("S"));        
        int numOfRootRules = rootRules.size();
        String []binLabels = new String[numOfBins];
        for(int i = 0; i < numOfBins; i++)
            binLabels[i] = String.format("%s-%s",i*binSize, i*binSize + binSize);        
        String[][] labels = getLabels(numOfRootRules, numOfBins, "wordsPerRootRule ", 
                event3Model.cfgRulesRhsStrArray(rootRules), binLabels);
        int i = 0;
        for(Vec v: wordsPerRootRule)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProb(v, labels[i++]));
            else
                out.append(forEachCount(v, labels[i++]));
        }
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
            String[] lab = getLabels(rule.getValue().size(), "cfgRulesChoices " + lhs + " ",
                    event3Model.cfgRulesRhsStrArray(event3Model.getCfgRules().get(rule.getKey())));
            if (paramsType == ParamsType.PROBS)           
                out.append(forEachProbNonZero(rule.getValue(), lab));            
            else            
                out.append(forEachCountNonZero(rule.getValue(), lab));            
        } // for
        Map<CFGRule, Integer> rootRules = event3Model.getCfgCandidateRules(event3Model.getRulesIndexer().getIndex("S"));        
        int numOfRootRules = rootRules.size();
        String []binLabels = new String[numOfBins];
        for(int i = 0; i < numOfBins; i++)
            binLabels[i] = String.format("%s-%s",i*binSize, i*binSize + binSize);        
        String[][] labels = getLabels(numOfRootRules, numOfBins, "wordsPerRootRule ", 
                event3Model.cfgRulesRhsStrArray(rootRules), binLabels);
        int i = 0;
        for(Vec v: wordsPerRootRule)
        {
            if(paramsType == ParamsType.PROBS)
                out.append(forEachProbNonZero(v, labels[i++]));
            else
                out.append(forEachCountNonZero(v, labels[i++]));
        }
        return out.toString();
    }
}