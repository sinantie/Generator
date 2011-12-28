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
    public Vec baselineWeight, bigramWeights, ngramWeights, lmWeight, 
               hasConsecutiveWordsWeight, 
               hasConsecutiveBigramsWeight,
               hasConsecutiveTrigramsWeight,            
               hasEmptyValueWeight;
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
            bigramWeights = VecFactory.zeros(vectorType, model.getWordBigramMap().size());
            addVec("bigramWeights", bigramWeights);
            ngramNegativeWeights = new MapVec();
            addVec("negativeNgramWeights", ngramNegativeWeights);
            lmWeight = VecFactory.zeros(vectorType, 1);
            addVec("lmWeight", lmWeight);            
            hasConsecutiveWordsWeight = VecFactory.zeros(vectorType, 1);
            addVec("hasConsecutiveWordsWeight", hasConsecutiveWordsWeight);
            hasConsecutiveBigramsWeight = VecFactory.zeros(vectorType, 1);
            addVec("hasConsecutiveBigramsWeight", hasConsecutiveBigramsWeight);
            hasConsecutiveTrigramsWeight = VecFactory.zeros(vectorType, 1);
            addVec("hasConsecutiveTrigramsWeight", hasConsecutiveTrigramsWeight);
            hasEmptyValueWeight = VecFactory.zeros(vectorType, 1);
            addVec("hasEmptyValueWeight", hasEmptyValueWeight);            
        }
    }
    
    @Override
    public String output(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder(outputDiscriminativeOnly(paramsType));
        out.append(super.output(paramsType));
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder(outputDiscriminativeOnly(paramsType));
        out.append(super.outputNonZero(paramsType));
        return out.toString();
    }
    
    public String outputDiscriminativeOnly(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder();
        out.append(forEachCountNonZero(baselineWeight, getLabels(1, "baseline", null)));
        if(model.isUseKBest())
        {
            out.append(forEachCountNonZero(lmWeight, getLabels(1, "lm", null)));
            out.append(forEachCountNonZero(hasConsecutiveWordsWeight, getLabels(1, "hasConsecutiveWords", null)));
            out.append(forEachCountNonZero(hasConsecutiveBigramsWeight, getLabels(1, "hasConsecutiveBigrams", null)));
            out.append(forEachCountNonZero(hasConsecutiveTrigramsWeight, getLabels(1, "hasConsecutiveTrigrams", null)));
            out.append(forEachCountNonZero(hasEmptyValueWeight, getLabels(1, "hasEmptyValue", null)));
            out.append(forEachCountNonZero(ngramWeights, getLabels(model.getWordNgramMap().size(), 
                "ngramWeights ", model.getWordNgramLabels(model.getWordNgramMap(), 3))));
            out.append(forEachCountNonZero(bigramWeights, getLabels(model.getWordBigramMap().size(), 
                "bigramWeights ", model.getWordNgramLabels(model.getWordBigramMap(), 2))));
            out.append(forEachCountNonZero(ngramNegativeWeights, getLabels(model.getWordNegativeNgramMap().size(), 
                "negativeNgramWeights ", model.getWordNgramLabels(model.getWordNegativeNgramMap(), 3))));
            
            for(int i = 0; i < eventTypeParams.length; i++)
            {
                out.append(((DiscriminativeEventTypeParams)eventTypeParams[i]).outputDiscriminativeOnly(paramsType)).append("\n");
            }
        }
    return out.toString();
    }
}