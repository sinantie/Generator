package induction.problem.event3.discriminative.params;

import induction.problem.MapVec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
import induction.problem.event3.params.EventTypeParams;
import java.io.PrintWriter;
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
    public Vec numOfFields;
    public Vec emptyValue;
    private final DiscriminativeEvent3Model model;
    private final EventType eventType;
    
    public DiscriminativeEventTypeParams(Event3Model model, EventType eventType, 
                           VecFactory.Type vectorType, int maxNumOfWords)
    {        
        super(model, eventType, model.W(), vectorType);
        this.model = (DiscriminativeEvent3Model)model;
        this.eventType = eventType;
        this.maxNumOfWords = maxNumOfWords;        
        numberOfWordsPerField = VecFactory.zeros2(vectorType, F + 1, maxNumOfWords);            
        addVec(getLabels(F+1, "numWordsC " + typeToString + " ", fieldToString), numberOfWordsPerField);
        fieldNgrams = new MapVec();
        addVec("fieldNgramsC " + typeToString, fieldNgrams);
        numOfFields = VecFactory.zeros(vectorType, maxNumOfWords);
        addVec("numOfFieldsC " + typeToString, fieldNgrams);
        emptyValue = VecFactory.zeros(vectorType, F +1);
        addVec("emptyValueF " + typeToString, emptyValue);
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
        out.append(forEachCount(numOfFields, getLabels(maxNumOfWords, "numOfFieldsC " + typeToString + " ", numAr)));
        out.append(forEachCount(emptyValue, getLabels(F + 1, "emptyValueF " + typeToString + " ", fieldToString)));
        out.append(super.output(paramsType));        
        return out.toString();
    }
    
    @Override
    public void outputNonZero(ParamsType paramsType, PrintWriter out)
    {
//        StringBuilder out = new StringBuilder(outputDiscriminativeOnly(paramsType));                
        out.append(outputDiscriminativeOnly(paramsType));
        out.append(super.output(paramsType));
//        return out.toString();
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
        if(model.getFieldNgramsMapPerEventTypeArray() != null)
        {
            Map map = model.getFieldNgramsMapPerEventTypeArray()[eventType.getEventTypeIndex()];
            out.append(forEachCountNonZero(fieldNgrams, getLabels(map.size(), 
                    "fieldNgramWeights ", model.getFieldNgramLabels(eventType, map, 3))));        
        }
        out.append(forEachCountNonZero(numOfFields, getLabels(maxNumOfWords, "numOfFieldsC " + typeToString + " ", numAr)));
        out.append(forEachCountNonZero(emptyValue, getLabels(F + 1, "emptyValueF " + typeToString + " ", fieldToString)));
        return out.toString();
    }
}
