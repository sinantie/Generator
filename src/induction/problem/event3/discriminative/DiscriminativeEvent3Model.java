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
import induction.MyCallable;
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
import induction.problem.event3.Widget;
import induction.problem.event3.discriminative.optimizer.DefaultPerceptron;
import induction.problem.event3.discriminative.optimizer.GradientBasedOptimizer;
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
    HashMap<Feature, Double> oracleFeatures, modelFeatures;
    /**
     * Keeps count of the number of examples processed so far. Necessary for batch updates
     */
    int numProcessedExamples = 0;
    
    public DiscriminativeEvent3Model(Options opts)
    {
        super(opts);        
        oracleFeatures = new HashMap<Feature, Double>();
        modelFeatures = new HashMap<Feature, Double>();
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
                    new FileOutputStream(Execution.getFile(name + 
                    ".discriminative.params.obj")));
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
        params = newParams();
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
        HashMap<Feature, Double> perceptronSumModel = new HashMap();
        HashMap<Feature, Double[]> perceptronAverageModel = new HashMap();
        
        int batchSize;
        boolean cooling = false;
        switch(lopts.learningScheme)
        {            
            case batch : batchSize = examples.size(); break;    
            case stepwise : batchSize = lopts.miniBatchSize; cooling = true; break;    
            default: case incremental: batchSize = 1;
        }
        // percy's cooling
        GradientBasedOptimizer optimizer = new DefaultPerceptron(
                perceptronSumModel, perceptronAverageModel, 
                examples.size(), 
                batchSize,
                lopts.convergePass, 
                lopts.stepSizeReductionPower,
                lopts.initTemperature);
        //zli's cooling (set initTemperature to 0.1)
//        GradientBasedOptimizer optimizer = new DefaultPerceptron(
//                perceptronSumModel, perceptronAverageModel, 
//                examples.size(), 
//                batchSize,
//                lopts.convergePass, 
//                lopts.initTemperature);
        // we need the cooling scheduling in case we do stepwise updating
        if(!cooling)
            optimizer.setNoCooling();
        for(int iter = 0; iter < lopts.numIters; iter++) // for t = 1...T do
        {
            FullStatFig complexity = new FullStatFig(); // Complexity inference
            Utils.begin_track("Iteration %s/%s: ", Utils.fmt(iter+1), 
                    Utils.fmt(lopts.numIters));
            Record.begin("iteration", iter+1);
            trainPerformance = existsTrain ? newPerformance() : null;
            
            for(int i = 0; i < examples.size(); i++) // for i = 1...N do
            {
//                // create an inference state model
//                DiscriminativeInferState inferState = (DiscriminativeInferState) createInferState(
//                        examples.get(i), 1, null, 1, lopts, iter);
//                // create hypergraph - precompute local features on the fly
//                inferState.createHypergraph();                                
//                // perform reranking on the hypergraph. During the recursive call
//                // in order to extract D_1 (top derivation) update the modelFeatures
//                // map, i.e. compute f(y^). We will need this for the perceptron updates
//                inferState.setFeatures(modelFeatures);
//                inferState.doInference();
//                // compute oracle and update the oracleFeatures map, i.e. compute f(y+)
//                inferState.setFeatures(oracleFeatures);
//                inferState.setCalculateOracle(true);
//                inferState.doInference();
//                // update statistics
//                synchronized(complexity)
//                {
//                    complexity.add(inferState.getComplexity());
//                }                
//                synchronized(trainPerformance)
//                {
//                    trainPerformance.add(inferState.stats());
//                }
                
                Collection<ExampleProcessor> list = new ArrayList(2);
                // perform reranking on the hypergraph. During the recursive call
                // in order to extract D_1 (top derivation) update the modelFeatures
                // map, i.e. compute f(y^). We will need this for the perceptron updates
                list.add(new ExampleProcessor(
                        examples.get(i), modelFeatures, false, lopts, iter, complexity));
                // compute oracle and update the oracleFeatures map, i.e. compute f(y+)
                list.add(new ExampleProcessor(
                        examples.get(i), oracleFeatures, true, lopts, iter, complexity));
                Utils.parallelForeach(opts.numThreads, list);
                list.clear();
//                try{
//                    ExampleProcessor model = new ExampleProcessor(
//                            examples.get(i), modelFeatures, false, lopts, iter, complexity);
//                    model.call();
//                    model = null;
                
//                    ExampleProcessor oracle = new ExampleProcessor(
//                            examples.get(i), oracleFeatures, true, lopts, iter, complexity);
//                    oracle.call();
//                    oracle = null;
//                }                
//                catch(Exception e){
//                    e.printStackTrace();
//                    LogInfo.error(e);
//                }
                
                numProcessedExamples++;
                // update perceptron if necessary (batch update)
                updateOptimizer(false, optimizer);
            } // for (all examples)
            // purge any unprocessed examples
            updateOptimizer(true, optimizer);
            // update the internal average model
            ((DefaultPerceptron)optimizer).forceUpdateAverageModel();
            
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
        // use average model weights instead of sum 
        // (reduces overfitting according to Collins, 2002)
        ((DefaultPerceptron)optimizer).updateParamsWithAvgWeights();
        
        if(!opts.dontOutputParams)
        {
            saveParams(name);
            params.output(Execution.getFile(name+".params"));
        }        
        LogInfo.end_track();
        Record.end();
    }

    public void updateOptimizer(boolean forceUpdate, GradientBasedOptimizer optimizer)
    {
        if(forceUpdate || numProcessedExamples >= optimizer.getBatchSize())
        {            
            optimizer.updateModel(oracleFeatures, modelFeatures);            
//            reset_baseline_feat();            
            oracleFeatures.clear();
            modelFeatures.clear();
            numProcessedExamples = 0;            
            synchronized(trainPerformance)
            {
                ((DiscriminativePerformance)trainPerformance).add();
            }
        }
    }
    
    public void resetBaselineFeature(GradientBasedOptimizer optimizer)
    {        
//        optimizer.setFeatureWeight(((DiscriminativeParams)params).baselineWeight, 1.0);
    }
    @Override
    public void generate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK
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
        // Complexity inference (number of hypergraph nodes)
        FullStatFig complexity = new FullStatFig();
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
        Utils.begin_track("Generation-step " + name);        
        Collection<ExampleProcessor> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new ExampleProcessor(
                    examples.get(i), modelFeatures, false, lopts, 0, complexity));
//            try{
//            ExampleProcessor model = new ExampleProcessor(
//                    examples.get(i), modelFeatures, false, lopts, 0, complexity);
//            model.call();
//            model = null;
//            }
//            catch(Exception e){}
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
     * for a number of examples.
     * @return the average Viterbi log probability
     */
    public double testDiscriminativeLearn(String name, LearnOptions lopts)
    {
        learn(name, lopts);
        return trainPerformance.getAccuracy();
    }

    /**
     * helper method for testing the generation output. Simulates generate(...) method
     * for a single example without the thread mechanism
     * @return a String with the generated SGML text output (contains results as well)
     */
    public String testGenerate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        FullStatFig complexity = new FullStatFig();        
        testPerformance = newPerformance();
        AExample ex = examples.get(0);
        Widget bestWidget = null;
        try{
            ExampleProcessor model = new ExampleProcessor(
                    ex, modelFeatures, false, lopts, 0, complexity);
            model.call();
            model = null;
            bestWidget = model.bestWidget;
        }
        catch(Exception e){}        
        System.out.println(widgetToFullString(ex, bestWidget));
        return widgetToSGMLOutput(ex, bestWidget);
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
    
    protected AInferState createInferState(Example ex, double stepSize,
            LearnOptions lopts, int iter, boolean calculateOracle)
    {
        InferSpec ispec = new InferSpec(1, !lopts.hardUpdate, true, lopts.hardUpdate,
                      false, lopts.mixParamsCounts, lopts.useVarUpdates,
                      stepSize, iter);
        if(calculateOracle)
            return new DiscriminativeInferStateOracle(
                    this, ex, (Params)params, null, ispec, ngramModel);
        else 
            return new DiscriminativeInferState(
                    this, ex, (Params)params, null, ispec, ngramModel);

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

    protected class ExampleProcessor extends MyCallable
    {
        private AExample ex;
        private int iter;
        private HashMap<Feature, Double> features;
        private LearnOptions lopts;
        private final FullStatFig complexity;
        private final boolean calculateOracle;
        private Widget bestWidget;
        
        public ExampleProcessor(AExample ex, HashMap<Feature, Double> features,
                                boolean calculateOracle, LearnOptions lopts, 
                                int iter, FullStatFig complexity)
        {            
            this.ex = ex;
            this.features = features;
            this.lopts = lopts;
            this.iter = iter;
            this.complexity = complexity;
            this.calculateOracle = calculateOracle;
        }
        @Override
        public Object call() throws Exception
        {
            // create an inference state model
            DiscriminativeInferState inferState = 
                    (DiscriminativeInferState) createInferState(
                    (Example)ex, 1, lopts, iter, calculateOracle);            
            // create hypergraph - precompute local features on the fly
            inferState.setCalculateOracle(calculateOracle);
            inferState.createHypergraph();                                            
            inferState.setFeatures(features);            
            inferState.doInference();
            // update statistics
            synchronized(complexity)
            {
                complexity.add(inferState.getComplexity());
            }
            // process results
            if(opts.modelType == Options.ModelType.discriminativeTrain && !calculateOracle)
                synchronized(trainPerformance)
                {
                    trainPerformance.add(inferState.stats());
                }
            if(opts.modelType == Options.ModelType.generate)
                synchronized(testPerformance)
                {
                    testPerformance.add(inferState.stats());
                    testPerformance.add(ex, inferState.bestWidget);
                    bestWidget = inferState.bestWidget;
                    if(testPredOut != null)
                    {
                        testPredOut.println(widgetToSGMLOutput(ex, inferState.bestWidget));                        
                    }
                    if(testFullPredOut != null)
                    {
                        testFullPredOut.println(widgetToFullString(ex, inferState.bestWidget));
                    }
            }
            return null;
        }
    }
}
