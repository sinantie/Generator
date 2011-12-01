package induction.problem.event3.discriminative.params;

import induction.Options;
import induction.problem.MapVec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import induction.problem.event3.params.Params;

/**
 * The weights of the perceptron model
 * @author konstas
 */
public class DiscriminativeParams extends Params
{
    public Vec baselineWeight, ngramWeights, lmWeight;
    public Vec ngramNegativeWeights;
    
    private DiscriminativeEvent3Model model;
    
    public DiscriminativeParams(DiscriminativeEvent3Model model, Options opts, VecFactory.Type vectorType)
    {
        super(model, opts, vectorType);
        genParams(model, vectorType);
    }
    
    public DiscriminativeParams(DiscriminativeEvent3Model model, Options opts, 
                                VecFactory.Type vectorType, int maxNumOfWordsPerField)
    {
        super(model, opts, vectorType, maxNumOfWordsPerField);
        genParams(model, vectorType);
    }
    
    private void genParams(DiscriminativeEvent3Model model, VecFactory.Type vectorType)
    {
        this.model = model;
        baselineWeight = VecFactory.zeros(vectorType, 1);        
        addVec("baseline", baselineWeight);        
        if(model.isUseKBest())
        {
            ngramWeights = VecFactory.zeros(vectorType, model.getWordNgramMap().size());
            addVec("ngramWeights", ngramWeights);
            ngramNegativeWeights = new MapVec();
            addVec("negativeNgramWeights", ngramNegativeWeights);
            lmWeight = VecFactory.zeros(vectorType, 1);
            addVec("lmWeight", lmWeight);            
        }
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
            out += forEachProbNonZero(ngramWeights, getLabels(model.getWordNgramMap().size(), 
                "ngramWeights ", model.getWordNgramLabels(model.getWordNgramMap(), 3)));
            out += forEachProbNonZero(ngramNegativeWeights, getLabels(model.getWordNegativeNgramMap().size(), 
                "negativeNgramWeights ", model.getWordNgramLabels(model.getWordNegativeNgramMap(), 3)));
        }
        return out;
    }
}