package induction.utils;

import fig.basic.Pair;
import induction.Options;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.TrackParams;
import java.io.File;

/**
 *
 * @author sinantie
 */
public class PrintParamsSelective
{
    public enum ParamsName {eventTypeChoices, eventTypeGenericEmissions, fieldChoices, noneFieldEmissions, fieldGenChoices};
    
    private final AParams.ParamsType paramsType = AParams.ParamsType.PROBS;
    private final Options opts;
    private ParamsName paramsName;    
    private final GenerativeEvent3Model model;

    public PrintParamsSelective(Options opts, ParamsName paramsName)
    {
        this.opts = opts;
        this.paramsName = paramsName;
        model = new GenerativeEvent3Model(opts);
        model.init(Options.InitType.staged, opts.initRandom, "");
    }
    
    public PrintParamsSelective(Options opts)
    {
        this.opts = opts;
        model = new GenerativeEvent3Model(opts);
        model.init(Options.InitType.staged, opts.initRandom, "");
    }
        
    private void execute()
    {
        if(paramsName != null)
        {
            System.out.println(printParams(paramsName));
        }
        else
        {
            File dir = new File(opts.stagedParamsFile).getParentFile();
            Utils.write(dir.getPath() + "/eventTypeChoices_probs.txt", printParams(ParamsName.eventTypeChoices));
            Utils.write(dir.getPath() + "/eventTypeGenericEmissions_probs.txt", printParams(ParamsName.eventTypeGenericEmissions));
            Utils.write(dir.getPath() + "/fieldChoices_probs.txt", printParams(ParamsName.fieldChoices));
            Utils.write(dir.getPath() + "/fieldGenChoices_probs.txt", printParams(ParamsName.fieldGenChoices));
            Utils.write(dir.getPath() + "/noneFieldEmissions_probs.txt", printParams(ParamsName.noneFieldEmissions));
            Utils.write(dir.getPath() + "/fieldGenChoices_probs.txt", printParams(ParamsName.fieldGenChoices));
        }
    }
    
    private String printParams(ParamsName paramsName)
    {
        Params p = (Params)model.getParams();
        StringBuilder str = new StringBuilder();
        if(paramsName == ParamsName.eventTypeChoices)
        {
            int i = 0;
            for(TrackParams track : p.trackParams)
            {
                str.append("Track ").append(i++).append("\n");
                str.append(track.outputEventTypeChoicesNonZero(paramsType));
            }
        }
        else if(paramsName == ParamsName.eventTypeGenericEmissions)
        {
            str.append(p.outputGenericEmissions(paramsType, model.wordsToStringArray()));
        }
        else if(paramsName == ParamsName.noneFieldEmissions)
        {
            for(EventTypeParams eventType : p.eventTypeParams)
            {
                str.append(eventType.outputNoneFieldEmissions(paramsType, 100));
                str.append("");
            }
        }
        else if(paramsName == ParamsName.fieldGenChoices)
        {
            for(EventTypeParams eventType : p.eventTypeParams)
            {
                for(int f = 0; f < eventType.F; f++)
                {
                    str.append(eventType.outputGenChoices(paramsType, f));
                    str.append("");
                }
            }
        }
        else if(paramsName == ParamsName.fieldChoices)
        {
            for(EventTypeParams eventType : p.eventTypeParams)
            {
                str.append(eventType.outputFieldChoices(paramsType));
                str.append("");
            }
        }
        return str.toString();
    }
    
    public static void main(String[] args)
    {        
        Options opts = new Options();
        opts.stagedParamsFile = "results/output/amr/ldc/alignments/model_3-thres-5-bootstrap-ignoreFields/5.exec/stage1.params.obj.gz";
        opts.indepWords = new Pair<>(0, -1);
        opts.useFieldSets = new Pair<>(0, -1);
        ParamsName name = ParamsName.fieldChoices;
        
//        PrintParamsSelective pps = new PrintParamsSelective(opts, name);
        PrintParamsSelective pps = new PrintParamsSelective(opts);
        pps.execute();
    }
}
