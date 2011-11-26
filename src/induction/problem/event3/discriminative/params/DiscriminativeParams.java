package induction.problem.event3.discriminative.params;

import induction.Options;
import induction.problem.ProbVec;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import induction.problem.event3.params.Params;
import java.util.HashMap;
import java.util.Map;

/**
 * The weights of the perceptron model
 * @author konstas
 */
public class DiscriminativeParams extends Params
{
    public ProbVec baselineWeight, ngramWeights, lmWeight;
    public Map<Integer, Double> ngramNegativeWeights;
    
//    public ProbVec bigramWeights;
    private DiscriminativeEvent3Model model;
    
    public DiscriminativeParams(DiscriminativeEvent3Model model, Options opts)
    {
        super(model, opts);
        this.model = model;
        baselineWeight = ProbVec.zeros(1);        
        addVec("baseline", baselineWeight);        
        if(model.isUseKBest())
        {
//            bigramWeights = ProbVec.zeros(model.getWordNegativeNgramMap().size());
//            addVec("bigramWeights", bigramWeights);
            ngramWeights = ProbVec.zeros(model.getWordNgramMap().size());
            addVec("ngramWeights", ngramWeights);
            lmWeight = ProbVec.zeros(1);
            addVec("lmWeight", lmWeight);
            ngramNegativeWeights = new HashMap<Integer, Double>();
        }        
    }
    
    public Double getNgramNegativeWeights(int index)
    {
//        if(ngramNegativeWeights.containsKey(index))
            return ngramNegativeWeights.get(index);
//        ngramNegativeWeights.put(index, 0.0);
//        return 0.0;
    }
    @Override
    public String output()
    {
        String out = outputDiscriminativeOnly();
        out += super.output();
        return out;
    }
    
    public String outputDiscriminativeOnly()
    {
        String out = "";
        out += forEachProbNonZero(baselineWeight, getLabels(1, "baseline", null));
        if(model.isUseKBest())
        {
            out += forEachProbNonZero(lmWeight, getLabels(1, "lm", null));
//            out += forEachProb(bigramWeights, getLabels(model.getWordNegativeNgramMap().size(), 
//                "bigramWeights", model.getWordNgramLabels(2)));
            out += forEachProbNonZero(ngramWeights, getLabels(model.getWordNgramMap().size(), 
                "ngramWeights ", model.getWordNgramLabels(3)));
        }  
        return out;
    }
}
