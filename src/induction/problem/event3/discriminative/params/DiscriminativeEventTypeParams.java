package induction.problem.event3.discriminative.params;

import induction.problem.MapVec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import induction.problem.event3.params.EventTypeParams;
import java.util.Map;

/**
 *
 * @author konstas
 */
public class DiscriminativeEventTypeParams extends EventTypeParams
{
    protected int maxNumOfWords;
    public Vec[] numberOfWordsPerField;
    public Vec fieldNgrams;
    private DiscriminativeEvent3Model model;
    private EventType eventType;
    
    public DiscriminativeEventTypeParams(Event3Model model, EventType eventType, 
                           VecFactory.Type vectorType, int maxNumOfWords)
    {        
        super(model, eventType, vectorType);
        this.model = (DiscriminativeEvent3Model)model;
        this.eventType = eventType;
        this.maxNumOfWords = maxNumOfWords;        
        numberOfWordsPerField = VecFactory.zeros2(vectorType, F + 1, maxNumOfWords);            
        addVec(getLabels(F+1, "numWordsC " + typeToString + " ", fieldToString), numberOfWordsPerField);
        fieldNgrams = new MapVec();
        addVec("fieldNgramsC " + typeToString, fieldNgrams);
    }

    @Override
    public String output(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder();
        
        String[] numAr = new String[maxNumOfWords];
        for(int j = 0; j < maxNumOfWords; j++)
        {
            numAr[j] = String.valueOf(j + 1);
        }
        String[][] labels = getLabels(F+1, maxNumOfWords, "numWordsC " + typeToString + " ",
                      fieldToString, numAr);
        int i = 0;
        for(Vec v : numberOfWordsPerField)
        {
            out.append(forEachCount(v, labels[i++]));
        }
        Map map = model.getFieldNgramsMapPerEventTypeArray()[eventType.getEventTypeIndex()];
        out.append(forEachCount(fieldNgrams, getLabels(map.size(), 
                "fieldNgramWeights ", model.getFieldNgramLabels(eventType, map, 3))));
        out.append(super.output(paramsType));
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder(outputDiscriminativeOnly(paramsType));                
        out.append(super.output(paramsType));
        return out.toString();
    }
    
    public String outputDiscriminativeOnly(ParamsType paramsType)
    {
        StringBuilder out = new StringBuilder();
        
        String[] numAr = new String[maxNumOfWords];
        for(int j = 0; j < maxNumOfWords; j++)
        {
            numAr[j] = String.valueOf(j + 1);
        }
        String[][] labels = getLabels(F+1, maxNumOfWords, "numWordsC " + typeToString + " ",
                      fieldToString, numAr);
        int i = 0;
        for(Vec v : numberOfWordsPerField)
        {
            out.append(forEachCountNonZero(v, labels[i++]));
        }
        Map map = model.getFieldNgramsMapPerEventTypeArray()[eventType.getEventTypeIndex()];
        out.append(forEachCountNonZero(fieldNgrams, getLabels(map.size(), 
                "fieldNgramWeights ", model.getFieldNgramLabels(eventType, map, 3))));        
        return out.toString();
    }
}
