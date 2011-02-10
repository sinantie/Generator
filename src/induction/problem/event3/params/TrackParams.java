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
    ProbVec[] eventTypeChoices;
    ProbVec noneEventTypeEmissions;
    private int T, W, c;

    public TrackParams(Event3Model model, int c)
    {
        super();
        this.model = model;
        this.T = model.getT(); this.W = Event3Model.W(); this.c = c;
        // t_0, t -> choose event of type t given we were in type t_0
        eventTypeChoices = ProbVec.zeros2(T + 1, T + 1);
//        addVec(eventTypeChoices);
        addVec(getLabels(T + 1, "eventTypeChoices ", model.eventTypeStrArray()), eventTypeChoices);
        // w -> generate word w
        noneEventTypeEmissions = ProbVec.zeros(W);
//        addVec(noneEventTypeEmissions);
        addVec("noneEventTypeEmissions", noneEventTypeEmissions);
    }

    public ProbVec[] getEventTypeChoices()
    {
        return eventTypeChoices;
    }

    public ProbVec getNoneEventTypeEmissions()
    {
        return noneEventTypeEmissions;
    }

    @Override
    public String output()
    {
        String out = "";
        String[][] labels = getLabels(T+1, T+1, "eventTypeC [" + model.cstr(c) + "] ",
                model.eventTypeStrArray(), model.eventTypeStrArray());
        int i = 0;
        for(ProbVec v : eventTypeChoices)
        {
            out += forEachProb(v, labels[i++]);
        }
        out += "\n" + forEachProb(noneEventTypeEmissions,
                getLabels(W, "noneEventTypeE [" + model.cstr(c) + "] ",
                Event3Model.wordsToStringArray()));
        return out;
    }

}
