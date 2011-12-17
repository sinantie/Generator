package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Constants;
import induction.problem.event3.EventType;

/**
 *
 * @author konstas
 */
public class EventTypeParams extends AParams
{
    private Event3Model model;
    private EventType eventType;
    protected final int W, F, FS, F2, onlyabsent_efs;
    protected final int dontcare_efs;
    private final int EFS_ABSENT = 1; // 01
    private final int EFS_PRESENT = 2; // 10
    private final int EFS_DONTCARE = 3; // 11
    protected String[] fieldToString, fieldSetToString;
    protected String typeToString;
    protected int[] allowed_fs;
    public int none_f, boundary_f;
    
    
    public Vec[] fieldChoices, genChoices, fieldNameEmissions, noneFieldBigramChoices;    
    
    public AParams[] fieldParams;
    public Vec fieldSetChoices, noneFieldEmissions, filters;

    public EventTypeParams(Event3Model model, EventType eventType, VecFactory.Type vectorType)
    {
        super();
        this.model = model;
        this.eventType = eventType;
        none_f = eventType.getNone_f();
        boundary_f = eventType.getBoundary_f();
        this.W = Event3Model.W();
        this.F = eventType.getF();
        this.typeToString = eventType.getName();
        this.fieldToString = new String[F + 2];
        for(int i = 0; i < F + 2; i++)
        {
            fieldToString[i] = eventType.fieldToString(i);
        }
        // Field sets
        FS = 1 << F;
        this.fieldSetToString = new String[FS];
        for(int i = 0; i < FS; i++)
        {
            fieldSetToString[i] = Constants.setstr(F, i);
        }
        // fs is a binary mask of what fields can be present (1) or not (0)
        // efs is a binary mask of whether each field's presence and
        // absence is allowed (1) or not (0)
        F2 = F + F; // Maximum number of elements in the efs set

        int efs = 0;
        for(int f = 0; f < F; f++)
        {
            efs = efs_addAbsent(efs, f);
        }
        onlyabsent_efs = efs;
        allowed_fs = new int[FS];
        for(int fs = 0; fs < FS; fs++)
        {
            int size = Constants.setSize(fs);
            if(model.getOpts().minFieldSetSize <= size &&
               size <= model.getOpts().maxFieldSetSize)
            {
                allowed_fs[fs] = fs;
            }
        }
        dontcare_efs = (1 << (F2)) - 1; // All 11s

         // f0, f -> choose field f given previous field f_0 (in event type t)        
        fieldChoices = VecFactory.zeros2(vectorType, F+2, F+2);        
        addVec(getLabels(F+2, "fieldChoices " + typeToString + " ",
                          fieldToString), fieldChoices);
        // Distribution over field sets
        fieldSetChoices = VecFactory.zeros(vectorType, FS);
        addVec("fieldSetChoices" + typeToString, fieldSetChoices);
        // w -> directly use word w (for none_f)
        noneFieldEmissions = VecFactory.zeros(vectorType, W);
        addVec("noneFieldEmissions" + typeToString, noneFieldEmissions);
        // f, g -> how to generate (g) a word in event f
        genChoices = VecFactory.zeros2(vectorType, F, Parameters.G);
        addVec(getLabels(F, "genChoices " + typeToString + " ",
                          fieldToString), genChoices);
        
        noneFieldBigramChoices = VecFactory.zeros2(vectorType, W, W);
        addVec(getLabels(W, "noneFieldWordBiC " + typeToString + " ",
                          Event3Model.wordsToStringArray()), noneFieldBigramChoices);

        // f, w -> express field f with word w (G_FIELD_NAME) (not used)
//        fieldNameEmissions = ProbVec.zeros2(F, W);
//        addVec(fieldNameEmissions);
        fieldParams = new AParams[F];
        for(int f = 0; f < F; f++)
        {
            fieldParams[f] = eventType.getFields()[f].newParams(vectorType, 
                    typeToString + " " + fieldToString[f]);
            addVec(fieldParams[f].getVecs());
        }
        // whether this type should be generated or not
        filters = VecFactory.zeros(vectorType, Parameters.B);
        addVec("filters" + typeToString, filters);                
    } 
    
    public String efsstr(int efs)
    {
        return Constants.setstr(F2, efs);
    }
    public boolean efs_canBeAbesent(int efs, int f)
    {
        return Constants.setContains(efs, f+f);
    }
    public boolean efs_canBePresent(int efs, int f)
    {
        return Constants.setContains(efs, f+f+1);
    }
    public boolean efs_canBeEmpty(int efs)
    {
        return Constants.setContainsSet(efs, onlyabsent_efs);
    }
    public final int efs_addAbsent(int efs, int f)
    {
        return Constants.setAdd(efs, f+f);
    }
    public int efs_addPresent(int efs, int f)
    {
        return Constants.setAdd(efs, f+f+1);
    }
    public int fs2efs(int fs)
    {
        int efs = 0;
        for(int i = 0; i < F; i++)
        {
            if(Constants.setContains(fs, i))
            {
                efs = efs_addAbsent(efs, i);
            }
            else
            {
                efs = efs_addAbsent(efs, i);
            }
        } // for
        return efs;
    }

    public int[] getAllowed_fs()
    {
        return allowed_fs;
    }

    public int getDontcare_efs()
    {
        return dontcare_efs;
    }

    @Override
    public String output(ParamsType paramsType)
    {
        String out = "";
        String[][] labels = getLabels(F+2, F+2, "fieldC " + typeToString + " ",
                          fieldToString, fieldToString);
        int i = 0;
        for(Vec v : fieldChoices)
        {
            if(paramsType == ParamsType.PROBS)
                out += forEachProb(v, labels[i++]);
            else
                out += forEachCount(v, labels[i++]);
        }
        if(paramsType == ParamsType.PROBS)
            out += forEachProb(fieldSetChoices,
                   getLabels(FS, "fieldSetC " + typeToString + " ", fieldSetToString)) +
                   forEachProb(noneFieldEmissions,
                   getLabels(W, "noneFieldE " + typeToString + " ",
                              Event3Model.wordsToStringArray()));
        else
            out += forEachCount(fieldSetChoices,
                   getLabels(FS, "fieldSetC " + typeToString + " ", fieldSetToString)) +
                   forEachCount(noneFieldEmissions,
                   getLabels(W, "noneFieldE " + typeToString + " ",
                              Event3Model.wordsToStringArray()));
            
        // if too huge parameter set, comment
//        i = 0;
//        String[][] labelsNone = getLabels(W, W, "noneFieldWordBiC " + typeToString + " ",
//                          GenerativeEvent3Model.wordsToStringArray(), GenerativeEvent3Model.wordsToStringArray());
//        for(ProbVec v : noneFieldBigramChoices)
//        {
//            out += forEachProb(v, labelsNone[i++]);
//        }
        String[][] labelsGen = getLabels(F, Parameters.G, "genC " + typeToString + " ",
                          fieldToString, Parameters.generateToString);
//        String[][] labelsEm = getLabels(F, W, "fieldNameE " + typeToString + " " ,
//                          fieldToString, GenerativeEvent3Model.wordsToStringArray());
        for(int f = 0; f < F; f++)
        {
            if(paramsType == ParamsType.PROBS)
                out += forEachProb(genChoices[f], labelsGen[f]) +
    //                   forEachProb(fieldNameEmissions[f], labelsEm[f]) +
                       fieldParams[f].output(paramsType) + "\n";
            else
                out += forEachCount(genChoices[f], labelsGen[f]) +
    //                   forEachCount(fieldNameEmissions[f], labelsEm[f]) +
                       fieldParams[f].output(paramsType) + "\n";
        }
        if(paramsType == ParamsType.PROBS)
            out += forEachProb(filters,
                   getLabels(Parameters.B, "filter " + typeToString + " ",
                              Parameters.booleanToString));
        else
            out += forEachCount(filters,
                   getLabels(Parameters.B, "filter " + typeToString + " ",
                              Parameters.booleanToString));
        return out;
    }

    @Override
    public String toString()
    {
        return "fieldC " + typeToString;
    }
    
    
}
