package induction.problem.event3.discriminative.params;

import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.params.EventTypeParams;

/**
 *
 * @author konstas
 */
public class DiscriminativeEventTypeParams extends EventTypeParams
{
    protected int maxNumOfWords;
    public Vec[] numberOfWordsPerField;
    
    public DiscriminativeEventTypeParams(Event3Model model, EventType eventType, 
                           VecFactory.Type vectorType, int maxNumOfWords)
    {
        super(model, eventType, vectorType);
        this.maxNumOfWords = maxNumOfWords;        
        numberOfWordsPerField = VecFactory.zeros2(vectorType, F + 1, maxNumOfWords);            
        addVec(getLabels(F+1, "numWordsC " + typeToString + " ", fieldToString), numberOfWordsPerField);       
    }

    @Override
    public String output()
    {
        String out = "";
        
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
            out += forEachProb(v, labels[i++]);
        }        
        out += super.output();
        return out;
    }
    
    
}
