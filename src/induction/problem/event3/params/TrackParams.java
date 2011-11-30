package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;

/**
 *
 * @author sinantie
 */
public class TrackParams extends AParams
{
    Event3Model model;
    Vec[] eventTypeChoices, noneEventTypeBigramChoices;
    Vec noneEventTypeEmissions;
    private int T, W, c;
    public int none_t, boundary_t;

    public TrackParams(Event3Model model, int c, VecFactory.Type vectorType)
    {
        super();
        this.model = model;
        this.T = model.getT(); this.W = Event3Model.W(); this.c = c;
        none_t = model.none_t();
        boundary_t = model.boundary_t();
        // t_0, t -> choose event of type t given we were in type t_0        
        eventTypeChoices = VecFactory.zeros2(vectorType, T + 2, T + 2);        
        addVec(getLabels(T + 2, "eventTypeChoices["+c+"]", model.eventTypeStrArray()), eventTypeChoices);
        // w -> generate word w
        noneEventTypeEmissions = VecFactory.zeros(vectorType, W);        
        addVec("noneEventTypeEmissions["+c+"]", noneEventTypeEmissions);        
        noneEventTypeBigramChoices = VecFactory.zeros2(vectorType, W, W);        
        addVec(getLabels(W, "noneFieldWordBiC["+c+"] ",
                          Event3Model.wordsToStringArray()), noneEventTypeBigramChoices);
    }

    public Vec[] getEventTypeChoices()
    {
        return eventTypeChoices;
    }

    public Vec getNoneEventTypeEmissions()
    {
        return noneEventTypeEmissions;
    }

    public Vec[] getNoneEventTypeBigramChoices()
    {
        return noneEventTypeBigramChoices;
    }

    @Override
    public String output()
    {
        String out = "";
        String[][] labels = getLabels(T+2, T+2, "eventTypeC [" + model.cstr(c) + "] ",
                model.eventTypeStrArray(), model.eventTypeStrArray());
        int i = 0;
        for(Vec v : eventTypeChoices)
        {
            out += forEachProb(v, labels[i++]);
        }
        out += "\n" + forEachProb(noneEventTypeEmissions,
                getLabels(W, "noneEventTypeE [" + model.cstr(c) + "] ",
                Event3Model.wordsToStringArray()));
        i = 0;
        // if too huge parameter set, comment
//        String[][] labelsNone = getLabels(W, W, "noneEventTypeWordBiC [" + model.cstr(c) + "] ",
//              Event3Model.wordsToStringArray(), Event3Model.wordsToStringArray());
//        for(ProbVec v : noneEventTypeBigramChoices)
//        {
//            out += forEachProb(v, labelsNone[i++]);
//        }
        return out;
    }
}
