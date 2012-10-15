package induction.problem.event3.params;

import induction.Options;
import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.discriminative.params.DiscriminativeEventTypeParams;

/**
 *
 * @author konstas
 */
public class Params extends AParams
{
    private int T, W, C;
    public Vec trackChoices, genericEmissions, genericLabelChoices;
    public Vec[] eventTypeChoicesGivenWord;    
    public TrackParams[] trackParams;
    public CFGParams cfgParams;
    public EventTypeParams[] eventTypeParams;
    private EventType[] eventTypes;    
    private Options opts;
    public Params(Event3Model model, Options opts, VecFactory.Type vectorType)
    {
        super(model);
        genParams(model, opts, vectorType);

        // t -> generate words for event type t
        eventTypeParams = new EventTypeParams[T];
        for(int t = 0; t < T; t++)
        {
            eventTypeParams[t] = new EventTypeParams(model, eventTypes[t], vectorType);
            addVec(eventTypeParams[t].getVecs());
        }
    }
    
    public Params(Event3Model model, Options opts,
                  VecFactory.Type vectorType, int maxNumOfWordsPerField)
    {
        super(model);
        genParams(model, opts, vectorType);
        // t -> generate words for event type t
        eventTypeParams = new EventTypeParams[T];
        for(int t = 0; t < T; t++)
        {
            eventTypeParams[t] = new DiscriminativeEventTypeParams(model, eventTypes[t], 
                    vectorType, maxNumOfWordsPerField);
            addVec(eventTypeParams[t].getVecs());
        }
    }

    private void genParams(Event3Model model, Options opts, VecFactory.Type vectorType)
    {
        this.opts = opts;
        T = model.getT();
        W = model.W();
        C = model.getC();
        this.model = model;
        this.eventTypes = model.getEventTypes();
        trackChoices = VecFactory.zeros(vectorType, model.getPC());
        addVec("trackChoices", trackChoices);
        trackParams = new TrackParams[C];
        for(int c = 0; c < C; c++)
        {
            trackParams[c] = new TrackParams(model, c, vectorType);
            addVec(trackParams[c].getVecs());
        }
        // treebank rules
        if(model.getCfgRules() != null)
        {
            cfgParams = new CFGParams(model, vectorType);
            addVec(cfgParams.getVecs());
        }
        // ideally for function words
        genericEmissions = VecFactory.zeros(vectorType, W);       
        addVec("genericEmissions", genericEmissions);
        // Generate labels
        genericLabelChoices = VecFactory.zeros(vectorType, Event3Model.LB());        
        addVec("genericLabelChoices", genericLabelChoices);
        // w, t -> probability of generating an event type given word
        // (not useful in practice)
        if(opts.includeEventTypeGivenWord)
        {            
            eventTypeChoicesGivenWord = VecFactory.zeros2(vectorType, W, T+1);
            addVec(getLabels(W, "eventTypeChoicesGivenWord", model.wordsToStringArray()),
                    eventTypeChoicesGivenWord);
        }
    }
    
    @Override
    public void optimise(double smoothing)
    {
        // Apply targeted smoothing/discounting to individual parameters
        for(int t = 0; t < T + 2; t++)
        {
            for(int c = 0; c < C; c++)
            {                
                // Select the none event more often
                trackParams[c].getEventTypeChoices()[t].addCount(((Event3Model)model).none_t(),
                        opts.noneEventTypeSmoothing);

                if (!Double.isNaN(opts.fixedNoneEventTypeProb))
                {
                    trackParams[c].getEventTypeChoices()[t].setCountToObtainProb(((Event3Model)model).none_t(),
                            opts.fixedNoneEventTypeProb);
                }
            } // for c
//            if (t != none_t) {
            if (t < T)
            {
                for(int f = 0; f < eventTypes[t].getF() + 1; f++)
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
                                for(Vec v: ((CatFieldParams)fparams).emissions)
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
    public String output(ParamsType paramsType)
    {
        String[] words = ((Event3Model)model).wordsToStringArray();
        StringBuilder out = new StringBuilder();
        if(paramsType == ParamsType.PROBS)
            out.append(forEachProb(trackChoices,
                    getLabels(((Event3Model)model).getPC(), "trackC ", ((Event3Model)model).pcstrArray())));
        else
            out.append(forEachCount(trackChoices,
                    getLabels(((Event3Model)model).getPC(), "trackC ", ((Event3Model)model).pcstrArray())));
        for(AParams params : trackParams)        
            out.append(params.output(paramsType)).append("\n");
        // treebank rules
        if(cfgParams != null)
            out.append(cfgParams.output(paramsType)).append("\n");
        if(paramsType == ParamsType.PROBS)
            out.append(forEachProb(genericEmissions,
                   getLabels(W, "genericE ", words))).
                    append(forEachProb(genericLabelChoices, getLabels(Event3Model.LB(), 
                    "genericLabelC ", Event3Model.labelsToStringArray())));
        else
            out.append(forEachCount(genericEmissions,
                   getLabels(W, "genericE ", words))).
                    append(forEachCount(genericLabelChoices, getLabels(Event3Model.LB(), 
                    "genericLabelC ", Event3Model.labelsToStringArray())));
        if(opts.includeEventTypeGivenWord)
        {
            String[][] labels = getLabels(W, T + 1, "eventTypeChoice|w ",
                    words, ((Event3Model)model).eventTypeStrArray());
            int i = 0;
            for(Vec v: eventTypeChoicesGivenWord)
            {
                if(paramsType == ParamsType.PROBS)
                    out.append(forEachProb(v, labels[i++]));
                else
                    out.append(forEachCount(v, labels[i++]));
            }
        }
        out.append("\n");
        for(AParams params : eventTypeParams)
        {
            out.append(params.output(paramsType)).append("\n");
        }
        return out.toString();
    }
    
    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        String[] words = ((Event3Model)model).wordsToStringArray();
        StringBuilder out = new StringBuilder();
        if(paramsType == ParamsType.PROBS)
            out.append(forEachProbNonZero(trackChoices,
                    getLabels(((Event3Model)model).getPC(), "trackC ", ((Event3Model)model).pcstrArray())));
        else
            out.append(forEachCountNonZero(trackChoices,
                    getLabels(((Event3Model)model).getPC(), "trackC ", ((Event3Model)model).pcstrArray())));
        for(AParams params : trackParams)
            out.append(params.outputNonZero(paramsType)).append("\n");
        // treebank rules
        if(cfgParams != null)
            out.append(cfgParams.outputNonZero(paramsType)).append("\n");
        if(paramsType == ParamsType.PROBS)
            out.append(forEachProbNonZero(genericEmissions,
                   getLabels(W, "genericE ", words))).
                    append(forEachProbNonZero(genericLabelChoices, getLabels(Event3Model.LB(), 
                    "genericLabelC ", Event3Model.labelsToStringArray())));
        else
            out.append(forEachCountNonZero(genericEmissions,
                   getLabels(W, "genericE ", words))).
                    append(forEachCountNonZero(genericLabelChoices, getLabels(Event3Model.LB(), 
                    "genericLabelC ", Event3Model.labelsToStringArray())));
        if(opts.includeEventTypeGivenWord)
        {
            String[][] labels = getLabels(W, T + 1, "eventTypeChoice|w ",
                    words, ((Event3Model)model).eventTypeStrArray());
            int i = 0;
            for(Vec v: eventTypeChoicesGivenWord)
            {
                if(paramsType == ParamsType.PROBS)
                    out.append(forEachProbNonZero(v, labels[i++]));
                else
                    out.append(forEachCountNonZero(v, labels[i++]));
            }
        }
        out.append("\n");
        for(AParams params : eventTypeParams)
        {
            out.append(params.outputNonZero(paramsType)).append("\n");
        }
        return out.toString();
    }
}