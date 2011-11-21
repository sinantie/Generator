package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.ProbVec;
import java.util.Map;

/**
 * Wrapper class for all the emission multinomials, i.e. Categorical Field Values,
 * None Eventype words, None Field words and generic emission words.
 * It is used in the Discriminative training framework, in order to re-sort 
 * only these particular emission probabilities using the generative baseline parameters
 * and the discriminative weights produced by local features during training.
 * 
 * @author konstas
 */
public class EmissionParams extends AParams
{
    Map<Integer, ProbVec> noneFieldEmissions;
    Map<Integer, Map<Integer, ProbVec[]>> catEmissions;
    ProbVec noneEventTypeEmissions, genericEmissions;
    
    public EmissionParams(Map<Integer, Map<Integer, ProbVec[]>> catEmissions, Map<Integer, ProbVec> noneFieldEmissions,
                          ProbVec noneEventTypeEmissions, ProbVec genericEmissions)
    {
        this.catEmissions = catEmissions;
        this.noneFieldEmissions = noneFieldEmissions;
        this.noneEventTypeEmissions = noneEventTypeEmissions;
        this.genericEmissions = genericEmissions;
    }

    public Map<Integer, Map<Integer, ProbVec[]>> getCatEmissions()
    {
        return catEmissions;
    }

    public Map<Integer, ProbVec> getNoneFieldEmissions()
    {
        return noneFieldEmissions;
    }

    public ProbVec getGenericEmissions()
    {
        return genericEmissions;
    }

    public ProbVec getNoneEventTypeEmissions()
    {
        return noneEventTypeEmissions;
    }
            
    @Override
    public String output()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
