package induction.problem.event3.generative.planning;

import edu.uci.ics.jung.graph.Graph;
import fig.basic.IOUtils;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.AInferState;
import induction.problem.AParams;
import induction.problem.InferSpec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.EventType;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.generative.alignment.InferState;
import induction.problem.event3.generative.alignment.InferStatePCFG;
import induction.problem.event3.generative.generation.GenInferState;
import induction.problem.event3.generative.generation.GenInferStatePCFG;
import induction.problem.event3.generative.generation.SemParseInferState;
import induction.problem.event3.params.Params;
import induction.problem.event3.planning.PlanningEvent3Model;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author konstas
 */
public class GenerativePlanningEvent3Model extends PlanningEvent3Model
{

    public GenerativePlanningEvent3Model(Options opts) 
    {
        super(opts);
    }

    @Override
    public void stagedInitParams()
    {
        Utils.begin_track("stagedInitParams");
        try
        {
            Utils.log("Loading " + opts.stagedParamsFile);
            ObjectInputStream ois = IOUtils.openObjIn(opts.stagedParamsFile);
            wordIndexer = (Indexer<String>) ois.readObject();
            labelIndexer = (Indexer<String>) ois.readObject();
            eventTypes = (EventType[]) ois.readObject(); // NEW
            eventTypesBuffer = new ArrayList<EventType>(Arrays.asList(eventTypes));
            // fill in eventTypesNameIndexer
            fieldsMap = new HashMap<Integer, HashMap<String, Integer>>(eventTypes.length);
            for(EventType e: eventTypes)
            {
                eventTypeNameIndexer.add(e.getName());
                HashMap<String, Integer> fields = new HashMap<String, Integer>();
                int i = 0;
                for(Field f : e.getFields())
                {
                    fields.put(f.getName(), i++);
                }
                fields.put("none_f", i++);
                fieldsMap.put(e.getEventTypeIndex(), fields);
            }
            if(opts.treebankRules != null)
                readTreebankRules();                
            params = newParams();
            params.setVecs((Map<String, Vec>) ois.readObject());                        
//            System.out.println("BEFORE\n" +((Params)params).cfgParams.outputNonZero(ParamsType.PROBS));
            ois.close();
            
            if(opts.useDependencies)         
                loadDMVModel();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ opts.stagedParamsFile);
            ioe.printStackTrace(LogInfo.stderr);
            Execution.finish();
        }
        LogInfo.end_track();
        loadLengthPredictionModel();
        loadPosTagger();
        loadLanguageModel();
    }

    @Override
    protected AParams newParams() 
    {
        if(!opts.fixRecordSelection || params == null) 
        {
            return new Params(this, opts, VecFactory.Type.DENSE);
        }
        else // in case we are using a treebank for record selection, copy rule probabilites from previous iteration
        {
            Params p = new Params(this, opts, VecFactory.Type.DENSE);
            p.cfgParams.setVecs(((Params)params).cfgParams.getVecs());
            return p;
        }
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aparams, AParams acounts, InferSpec ispec) 
    {
        Example ex = (Example)aex;
        Params params = (Params)aparams;
        Params counts = (Params)acounts;
        
        switch(opts.modelType)
        {
            case evalPlanning : return new GenInferState(this, ex, params, counts, ispec, ngramModel);
            case evalPlanningPcfg : default: return new GenInferStatePCFG(this, ex, params, counts, ispec, ngramModel);            
        }
    }
    
}
