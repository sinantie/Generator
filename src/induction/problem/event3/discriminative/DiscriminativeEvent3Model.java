package induction.problem.event3.discriminative;

import edu.uci.ics.jung.graph.Graph;
import fig.basic.FullStatFig;
import fig.basic.IOUtils;
import induction.problem.AInferState;
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
import induction.problem.AExample;
import induction.problem.AParams;
import induction.problem.APerformance;
import induction.problem.InferSpec;
import induction.problem.ProbVec;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.discriminative.params.DiscriminativeParams;
import induction.problem.event3.generative.generation.GenerationPerformance;
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
import joshua.discriminative.training.learning_algorithm.DefaultPerceptron;
import joshua.discriminative.training.learning_algorithm.GradientBasedOptimizer;

/**
 * A discriminative model of events and their text summaries
 *
 * @author konstas
 */
public class DiscriminativeEvent3Model extends Event3Model implements Serializable
{  
    Params baselineModelParams;
    /**
     * maps that contain the total feature counts extracted from the Viterbi search
     * of the oracle model and the model under train
     */
    HashMap oracleFeatures, modelFeatures;
    
    public DiscriminativeEvent3Model(Options opts)
    {
        super(opts);        
        oracleFeatures = new HashMap();
        modelFeatures = new HashMap();
    }

    @Override
    public void stagedInitParams()
    {
        // Load generative model parameters
        baselineModelParams = loadGenerativeModelParams();
        
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
            ioe.printStackTrace(LogInfo.stderr);
//            ioe.printStackTrace();
            Execution.finish();
        }
        LogInfo.end_track();
    }
    
    private Params loadGenerativeModelParams()
    {
        Params generativeParams = null;
        Utils.begin_track("generativeModelInitParams");
        try
        {
            Utils.log("Loading " + opts.generativeModelParamsFile);
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(opts.generativeModelParamsFile));
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
            generativeParams = new Params(this, opts);
            generativeParams.setVecs((Map<String, ProbVec>) ois.readObject());
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ opts.stagedParamsFile);
            ioe.printStackTrace(LogInfo.stderr);
//            ioe.printStackTrace();
            Execution.finish();
        }
        LogInfo.end_track();
        return generativeParams;
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
            ex.printStackTrace(LogInfo.stderr);
//            ex.printStackTrace();
        }
    }

    @Override
    protected void supervisedInitParams()
    {
        // Load generative model parameters
        baselineModelParams = loadGenerativeModelParams();
        //do nothing, initialise to zero by default
    }

    
    @Override
    protected AParams newParams()
    {
        return new DiscriminativeParams(this, opts);
    }

    @Override
    protected APerformance newPerformance()
    {
        switch(opts.modelType)
        {
            case discriminativeTrain: return new DiscriminativePerformance();
            default: case generate : return new GenerationPerformance(this);            
//            default : return new InductionPerformance(this);
        }        
    }

    /**
     * Averaged Perceptron training (Collins 2002)
     * @param name
     * @param lopts 
     */
    @Override
    public void learn(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK        
        Record.begin(name);
        Utils.begin_track("Train: " + name);
        boolean existsTrain = false;
        for(int i = 0; i < examples.size(); i++)
        {
            if(isTrain(i))
            {
                existsTrain = true; break;
            }
        }        
        
        // initialise model
        HashMap perceptronSumModel = new HashMap();
        HashMap perceptronAverageModel = new HashMap();
        GradientBasedOptimizer optimizer = new DefaultPerceptron(
                perceptronSumModel, perceptronAverageModel, 
                examples.size(), lopts.batchUpdateSize);
        
        for(int iter = 0; iter < lopts.numIters; iter++) // for t = 1...T do
        {
            FullStatFig complexity = new FullStatFig(); // Complexity inference
            Utils.begin_track("Iteration %s/%s: ", Utils.fmt(iter+1), 
                    Utils.fmt(lopts.numIters));
            Record.begin("iteration", iter+1);
            trainPerformance = existsTrain ? newPerformance() : null;
            
            for(int i = 0; i < examples.size(); i++) // for i = 1...N do
            {
                // create an inference state model
                DiscriminativeInferState inferState = (DiscriminativeInferState) createInferState(
                        examples.get(i), 1, null, 1, lopts, iter);
                // create hypergraph - precompute local features on the fly
                inferState.createHypergraph();                                
                // perform reranking on the hypergraph. During the recursive call
                // in order to extract D_1 (top derivation) update the modelFeatures
                // map, i.e. compute f(y^). We will need this for the perceptron updates
                inferState.setFeatures(modelFeatures);
                inferState.doInference();
                
                // update statistics
                synchronized(complexity)
                {
                    complexity.add(inferState.getComplexity());
                }
                //TODO processExample
                
                
//                Params counts = newParams();
//                Collection<BatchEM> list = new ArrayList(examples.size());
//                // Batch Update
//                for(int j = 0; j < lopts.batchUpdateSize; j++)
//                {
//                    list.add(new BatchEM(i, examples.get(i), counts, lopts.initTemperature,
//                            lopts, iter, complexity));
//                }
//                Utils.parallelForeach(opts.numThreads, list);
//                LogInfo.end_track();
//                list.clear();
            } // for
            
            record(String.valueOf(iter), name, complexity);            
            LogInfo.end_track();
            Record.end();
            // Final
            if (iter == lopts.numIters - 1)
            {
                LogInfo.track("Final", true);
                if(trainPerformance != null)
                    trainPerformance.record("train");
                LogInfo.end_track();
            }
            iter++;
            if (Execution.shouldBail())
                lopts.numIters = iter;
        } // for (all iterations)
        if(!opts.dontOutputParams)
        {
            saveParams(name);
            params.output(Execution.getFile(name+".params"));
        }        
        LogInfo.end_track();
        Record.end();
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
        AParams counts = newParams();
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
    
    /**
     * helper method for testing the discriminative learning scheme. 
     * Simulates learn(...) method from the DiscriminativeEvent3Model class
     * for a number of examples without the thread mechanism.
     * @return the average Viterbi log probability
     */
    public double testDiscriminativeLearn(String name, LearnOptions lopts)
    {
        learn(name, lopts);
        return trainPerformance.getAccuracy();
    }

    public Params getBaselineModelParams()
    {
        return baselineModelParams;
    }

    public HashMap getOracleFeatures()
    {
        return oracleFeatures;
    }

    public HashMap getModelFeatures()
    {
        return modelFeatures;
    }
    
    @Override
    protected AInferState newInferState(AExample aex, AParams aweights, AParams acounts,
                                       InferSpec ispec)
    {
        Example ex = (Example)aex;
        Params weights = (Params)aweights;
        Params counts = (Params)acounts;        
        return new DiscriminativeInferState(this, ex, weights, counts, ispec, ngramModel);
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aweights, AParams acounts,
                                           InferSpec ispec, Graph graph)
    {
        Example ex = (Example)aex;
        Params weights = (Params)aweights;
        Params counts = (Params)acounts;
        return new DiscriminativeInferState(this, ex, weights, counts, ispec, ngramModel);        
    }

}
