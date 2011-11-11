package induction.problem.event3.discriminative.params;

import induction.Options;
import induction.problem.ProbVec;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import induction.problem.event3.params.Params;

/**
 * The weights of the perceptron model
 * @author konstas
 */
public class DiscriminativeParams extends Params
{
    public ProbVec baselineWeight, ngramWeights, lmWeight;
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
//            bigramWeights = ProbVec.zeros(model.getWordBigramMap().size());
//            addVec("bigramWeights", bigramWeights);
            ngramWeights = ProbVec.zeros(model.getWordNgramMap().size());
            addVec("ngramWeights", ngramWeights);
            lmWeight = ProbVec.zeros(1);
            addVec("lmWeight", lmWeight);
        }        
    }
    
    @Override
    public String output()
    {
        String out = "";
        out += forEachProb(baselineWeight, getLabels(1, "baseline", null));
        if(model.isUseKBest())
        {
            out += forEachProb(lmWeight, getLabels(1, "lm", null));
//            out += forEachProb(bigramWeights, getLabels(model.getWordBigramMap().size(), 
//                "bigramWeights", model.getWordNgramLabels(2)));
            out += forEachProb(ngramWeights, getLabels(model.getWordNgramMap().size(), 
                "ngramWeights", model.getWordNgramLabels(3)));
        }       
        out += super.output();
        return out;
    }
    
}
