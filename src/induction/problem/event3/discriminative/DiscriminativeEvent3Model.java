package induction.problem.event3.discriminative;

import induction.problem.event3.generative.*;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.FullStatFig;
import fig.basic.IOUtils;
import induction.problem.event3.params.Params;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import fig.record.Record;
import induction.LearnOptions;
import induction.Options;
import induction.Options.NgramWrapper;
import induction.Utils;
import induction.ngrams.KylmNgramWrapper;
import induction.ngrams.RoarkNgramWrapper;
import induction.ngrams.SrilmNgramWrapper;
import induction.problem.InferSpec;
import induction.problem.ProbVec;
import induction.problem.event3.Event3InferState;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.generative.generation.GenInferState;
import induction.problem.event3.generative.generation.GenPerformance;
import induction.problem.event3.generative.alignment.InferState;
import induction.problem.event3.generative.alignment.Performance;
import induction.problem.event3.generative.generation.SemParseInferState;
import induction.problem.event3.generative.generation.SemParsePerformance;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A discriminative model of events and their text summaries
 *
 * @author konstas
 */
public class DiscriminativeEvent3Model extends Event3Model implements Serializable
{  
    public DiscriminativeEvent3Model(Options opts)
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
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(opts.stagedParamsFile));
            wordIndexer = ((Indexer<String>) ois.readObject());
            labelIndexer = ((Indexer<String>) ois.readObject());
            eventTypes = (EventType[]) ois.readObject();
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

            params = newParams();
            params.setVecs((Map<String, ProbVec>) ois.readObject());
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ opts.stagedParamsFile);
            ioe.printStackTrace();
            Execution.finish();
        }
        LogInfo.end_track();
    }

    @Override
    protected void saveParams(String name)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(Execution.getFile(name + ".params.obj")));
            oos.writeObject(wordIndexer);
            oos.writeObject(labelIndexer);
            oos.writeObject(eventTypes);
            oos.writeObject(params.getVecs());
            oos.close();
        }
        catch (IOException ex)
        {
            Utils.log(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    protected void supervisedInitParams()
    {
        
    }

    
    @Override
    protected Params newParams()
    {
        return new Params(this, opts);
    }

    @Override
    protected Performance newPerformance()
    {
        switch(opts.modelType)
        {
            case generate : return new GenPerformance(this);
            case semParse : return new SemParsePerformance(this);
            default : return new Performance(this);
        }        
    }

    @Override
    public void learn(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK        
        Record.begin(name);
        Utils.begin_track("Train: " + name);
        boolean existsTrain = false, existsTest = false, output, fullOutput;
        for(int i = 0; i < examples.size(); i++)
        {
            if(isTrain(i))
            {
                existsTrain = true; break;
            }
        }
        for(int i = 0; i < examples.size(); i++)
        {
            if(isTest(i))
            {
                existsTest = true; break;
            }
        }
        
        int iter = 0;
        while (iter < lopts.numIters)
        {
            FullStatFig complexity = new FullStatFig(); // Complexity inference
            // Gradually reduce temperature
            double temperature = (lopts.numIters == 1) ? lopts.initTemperature :
                lopts.initTemperature +
                (lopts.finalTemperature- lopts.initTemperature) *
                iter / (lopts.numIters - 1);

            Utils.begin_track("Iteration %s/%s: temperature = %s",
                    Utils.fmt(iter+1), Utils.fmt(lopts.numIters),
                    Utils.fmt(temperature));
            Record.begin("iteration", iter+1);
            Execution.putOutput("currIter", iter+1);

            trainPerformance = existsTrain ? newPerformance() : null;
            testPerformance = existsTest ? newPerformance() : null;

//            output = opts.outputIterFreq != 0 && iter % opts.outputIterFreq == 0;
            output = (iter+1) % lopts.numIters == 0; // output only at the last iteration
            fullOutput = output && opts.outputFullPred;
            try
            {
                trainPredOut = (output && existsTrain) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".train.pred."+iter)) : null;
                testPredOut = (output && existsTest) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".test.pred."+iter)) : null;
                trainFullPredOut = (fullOutput && existsTrain) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".train.full-pred."+iter)) : null;
                testFullPredOut = (fullOutput && existsTest) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".test.full-pred."+iter)) : null;
            }
            catch(IOException ioe)
            {
                Utils.begin_track("Error opening file");
                LogInfo.end_track();
            }

            // Batch EM only
            Params counts = newParams();

            // E-step
            Utils.begin_track("E-step");
            Collection<BatchEM> list = new ArrayList(examples.size());
            for(int i = 0; i < examples.size(); i++)
            {
                list.add(new BatchEM(i, examples.get(i), counts, temperature,
                        lopts, iter, complexity));
            }
            Utils.parallelForeach(opts.numThreads, list);
            LogInfo.end_track();
            list.clear();
            // M-step
            params = counts;
//            params.saveSum(); // 02/07/09: for printing out posterior mass (see AParams.foreachProb)
            if (lopts.useVarUpdates)
            {
                params.optimiseVar(lopts.smoothing);
            }
            else
            {
                params.optimise(lopts.smoothing);
            }
            
            record(String.valueOf(iter), name, complexity);
            if(trainPredOut != null) trainPredOut.close();
            if(testPredOut != null) testPredOut.close();
            if(trainFullPredOut != null) trainFullPredOut.close();
            if(testFullPredOut != null) testFullPredOut.close();

            Execution.putOutput("currExample", examples.size());
            LogInfo.end_track();
            Record.end();
            // Final
            if (iter == lopts.numIters - 1)
            {
                LogInfo.track("Final", true);
                if(trainPerformance != null)
                    trainPerformance.record("train");
                if(testPerformance != null)
                    testPerformance.record("test");
            }
            iter++;
            if (Execution.shouldBail() || existsTest) // it makes sense to perform one iteration at test time
                lopts.numIters = iter;
        } // while (iter < lopts.numIters)
        saveParams(name);
        params.output(Execution.getFile(name+".params"));
        LogInfo.end_track();
        LogInfo.end_track();
        Record.end();
        Record.end();
        Execution.putOutput("currIter", lopts.numIters);
    }

    @Override
    public void generate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK
//        saveParams("stage1");
//        System.exit(1);
        if(!opts.fullPredRandomBaseline)
        {
            Utils.begin_track("Loading Language Model: " + name);
            if(opts.ngramWrapper == NgramWrapper.kylm)
                ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
            else if(opts.ngramWrapper == NgramWrapper.srilm)
                ngramModel = new SrilmNgramWrapper(opts.ngramModelFile, opts.ngramSize);
            else if(opts.ngramWrapper == NgramWrapper.roark)
                ngramModel = new RoarkNgramWrapper(opts.ngramModelFile);
            LogInfo.end_track();
        }
        FullStatFig complexity = new FullStatFig(); // Complexity inference (number of hypergraph nodes)
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();       
        try
        {
            testFullPredOut = (opts.outputFullPred) ?
                IOUtils.openOut(Execution.getFile(
                name+".test.full-pred-gen")) : null;
            testPredOut = IOUtils.openOut(Execution.getFile(name+".tst.xml"));
            // write prediction file header, conforming to SGML NIST standard
            testPredOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<mteval>\n" +
                                "<tstset setid=\"" + name + "\" srclang=\"English\" " +
                                "trglang=\"English\" sysid=\"sample_system\">");
        }
        catch(Exception ioe)
        {
            Utils.begin_track("Error opening file(s) for writing. No output will be written!");
            LogInfo.end_track();
        }                
        // E-step
        Utils.begin_track("Generation-step " + name);
        Params counts = newParams();
        Collection<BatchEM> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new BatchEM(i, examples.get(i), counts, temperature,
                    lopts, 0, complexity));                
        }
        Utils.parallelForeach(opts.numThreads, list);
        LogInfo.end_track();
        list.clear();

        if(testFullPredOut != null) testFullPredOut.close();
        if(testPredOut != null)
        {
            // write prediction file footer, conforming to SGML NIST standard
            testPredOut.println("</tstset>\n</mteval>");
            testPredOut.close();
        }
        Execution.putOutput("currExample", examples.size());

        // Final
//        testPerformance.output(Execution.getFile(name+".test.performance"));
        Record.begin("generation");
        record("results", name, complexity);
        Record.end();
        LogInfo.end_track();
    }
    

    @Override
    protected Event3InferState newInferState(Example ex, Params params, Params counts,
                                       InferSpec ispec)
    {
        switch(opts.modelType)
        {
            case generate : return new GenInferState(this, ex, params, counts, ispec, ngramModel);
            case semParse : return new SemParseInferState(this, ex, params, counts, ispec, ngramModel);
            default : return new InferState(this, ex, params, counts, ispec);
        }
    }

    protected Event3InferState newInferState(Example ex, Params params, Params counts,
                                           InferSpec ispec, Graph graph)
    {
        switch(opts.modelType)
        {
            case generate: return new GenInferState(this, ex, params, counts, ispec, ngramModel, graph);
            case semParse: default: return new SemParseInferState(this, ex, params, counts, ispec, graph);
        }
        
    }

}
