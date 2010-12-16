package induction.problem.event3;

import induction.Options;
import induction.problem.AParams;
import induction.problem.ProbVec;

/**
 *
 * @author konstas
 */
public class Params extends AParams
{
    private int T, W, C;
    public ProbVec trackChoices, genericEmissions, genericLabelChoices;
    public ProbVec[] eventTypeChoicesGivenWord;
    public TrackParams[] trackParams;
    public EventTypeParams[] eventTypeParams;
    private EventType[] eventTypes;
    private Event3Model model;
    private Options opts;
    public Params(Event3Model model, Options opts)
    {
        super();
        this.opts = opts;
        T = model.T();
        W = model.W();
        C = model.C;
        this.model = model;
        this.eventTypes = model.getEventTypes();
        trackChoices = ProbVec.zeros(model.PC);
        addVec(trackChoices);
        trackParams = new TrackParams[C];
        for(int c = 0; c < C; c++)
        {
            trackParams[c] = new TrackParams(model, c);
            addVec(trackParams[c].getVecs());
        }
        // ideally for function words
        genericEmissions = ProbVec.zeros(W);
        addVec(genericEmissions);
        // Generate labels
        genericLabelChoices = ProbVec.zeros(model.LB());
        addVec(genericLabelChoices);
        // w, t -> probability of generating an event type given word
        // (not useful in practice)
        eventTypeChoicesGivenWord = ProbVec.zeros2(W, T+1);
        addVec(eventTypeChoicesGivenWord);
        // t -> generate words for event type t
        eventTypeParams = new EventTypeParams[T];
        for(int t = 0; t < T; t++)
        {
            eventTypeParams[t] = new EventTypeParams(model, eventTypes[t] );
            addVec(eventTypeParams[t].getVecs());
        }
    }
    
    @Override
    public void optimise(double smoothing)
    {
        // Apply targeted smoothing/discounting to individual parameters
        for(int t = 0; t < T + 1; t++)
        {
            for(int c = 0; c < C; c++)
            {
                // Select the none event more often
//                trackParams[c].eventTypeChoices[t].addCount(model.none_t, opts.noneEventTypeSmoothing);
                // Select the none event more often
                trackParams[c].eventTypeChoices[t].addCount(T,
                        opts.noneEventTypeSmoothing);

                if (!Double.isNaN(opts.fixedNoneEventTypeProb))
                {
//                    trackParams[c].eventTypeChoices[t].setCountToObtainProb(model.none_t, opts.fixedNoneEventTypeProb);
                    trackParams[c].eventTypeChoices[t].setCountToObtainProb(T,
                            opts.fixedNoneEventTypeProb);
                }
            } // for c
//            if (t != none_t) {
            if (t != T)
            {
                for(int f = 0; f < eventTypes[t].F + 1; f++)
                {
                    // Select none field more often
                    eventTypeParams[t].fieldChoices[f].addCount(
                            eventTypeParams[t].none_f, opts.noneFieldSmoothing);
                    if (f != eventTypeParams[t].none_f)
                    {
                        // Select name more than value
                        eventTypeParams[t].genChoices[f].addCount(
                                Parameters.G_FIELD_NAME, opts.fieldNameSmoothing);
                        // Fix the generic probability
                        if (!Double.isNaN(opts.fixedGenericProb))
                        {
                            eventTypeParams[t].genChoices[f].
                                    setCountToObtainProb(Parameters.G_FIELD_GENERIC,
                                    opts.fixedGenericProb);
                        }
                        if (opts.discountCatEmissions)
                        {
                            AParams fparams = eventTypeParams[t].fieldParams[f];
                            if(fparams instanceof CatFieldParams)
                            {
                                for(ProbVec v: ((CatFieldParams)fparams).emissions)
                                {
                                    v.addCount(-smoothing);
                                }
                            }
                        } // if
                    } // if f
                } // for f
            } // if t
        } // for t
        super.optimise(smoothing);
    }

    @Override
    public String output()
    {
        String out = "";
        out += forEachProb(trackChoices,
                getLabels(model.PC, "trackC ", model.pcstrArray()));
        for(AParams params : trackParams)
        {
            out += params.output() + "\n";
        }
        out += forEachProb(genericEmissions,
               getLabels(W, "genericE ", model.wordsToStringArray())) +
               forEachProb(genericLabelChoices,
               getLabels(model.LB(), "genericLabelC ", model.labelsToStringArray()));
        if(opts.includeEventTypeGivenWord)
        {
            String[][] labels = getLabels(W, T + 1, "eventTypeChoice|w ",
                    model.wordsToStringArray(), model.eventTypeStrArray());
            int i = 0;
            for(ProbVec v: eventTypeChoicesGivenWord)
            {
                forEachProb(v, labels[i++]);
            }
        }
        out += "\n";
        for(AParams params : eventTypeParams)
        {
            out += params.output() + "\n";
        }
        return out;
    }
}
