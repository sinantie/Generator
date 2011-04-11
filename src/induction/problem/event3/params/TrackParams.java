package induction.problem.event3.params;

import induction.problem.AParams;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;

/**
 *
 * @author sinantie
 */
public class TrackParams extends AParams
{
    Event3Model model;
    ProbVec[] eventTypeChoices, noneEventTypeBigramChoices;
    ProbVec noneEventTypeEmissions;
    private int T, W, c;
    public int none_t, boundary_t;

    public TrackParams(Event3Model model, int c)
    {
        super();
        this.model = model;
        this.T = model.getT(); this.W = Event3Model.W(); this.c = c;
        none_t = model.none_t();
        boundary_t = model.boundary_t();
        // t_0, t -> choose event of type t given we were in type t_0
        eventTypeChoices = ProbVec.zeros2(T + 2, T + 2);
//        addVec(eventTypeChoices);
        addVec(getLabels(T + 2, "eventTypeChoices["+c+"]", model.eventTypeStrArray()), eventTypeChoices);
        // w -> generate word w
        noneEventTypeEmissions = ProbVec.zeros(W);
//        addVec(noneEventTypeEmissions);
        addVec("noneEventTypeEmissions["+c+"]", noneEventTypeEmissions);

        noneEventTypeBigramChoices = ProbVec.zeros2(W, W);
        addVec(getLabels(W, "noneFieldWordBiC["+c+"] ",
                          Event3Model.wordsToStringArray()), noneEventTypeBigramChoices);
    }

    public ProbVec[] getEventTypeChoices()
    {
        return eventTypeChoices;
    }

    public ProbVec getNoneEventTypeEmissions()
    {
        return noneEventTypeEmissions;
    }

    public ProbVec[] getNoneEventTypeBigramChoices()
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
        for(ProbVec v : eventTypeChoices)
        {
            out += forEachProb(v, labels[i++]);
        }
        out += "\n" + forEachProb(noneEventTypeEmissions,
                getLabels(W, "noneEventTypeE [" + model.cstr(c) + "] ",
                Event3Model.wordsToStringArray()));
        i = 0;
        String[][] labelsNone = getLabels(W, W, "noneEventTypeWordBiC [" + model.cstr(c) + "] ",
              Event3Model.wordsToStringArray(), Event3Model.wordsToStringArray());
        for(ProbVec v : noneEventTypeBigramChoices)
        {
            out += forEachProb(v, labelsNone[i++]);
        }
        return out;
    }

}
