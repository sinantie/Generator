package induction.problem.event3.generative;

import edu.berkeley.nlp.ling.Tree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
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
import induction.ngrams.SrilmNgramWrapper;
import induction.problem.AExample;
import induction.problem.AInferState;
import induction.problem.AParams;
import induction.problem.AParams.ParamsType;
import induction.problem.APerformance;
import induction.problem.InferSpec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.Example;
import induction.problem.event3.Field;
import induction.problem.event3.generative.alignment.AlignmentPerformance;
import induction.problem.event3.generative.generation.GenInferState;
import induction.problem.event3.generative.generation.GenerationPerformance;
import induction.problem.event3.generative.alignment.InferState;
import induction.problem.event3.generative.alignment.InferStatePCFG;
import induction.problem.event3.generative.generation.GenInferStatePCFG;
import induction.problem.event3.generative.generation.SemParseInferState;
import induction.problem.event3.generative.generation.SemParsePerformance;
import induction.problem.event3.params.CFGParams;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A model of events and their text summaries (ACL 2009).
 * Model:
 *  - Select events to talk about
 *  - Generate text of those events
 * An event type has a fixed set of fields.
 * Each field has a type (numeric, categorical, symbol, string).
 * Each event has a fixed event type and values for each of the fields.
 * The model is essentially a hierarchical labelled segmentation process.
 *
 * Change log:
 *  - 03/02/10: complete rewrite in Java
 *
 *  - 02/08/09: make field type explicit
 *  - 02/08/09: make the non-field type a possible field
 *  - 02/10/09: geometric distribution on numeric noise
 *  - 02/11/09: multiple trueEvents
 *  - 02/13/09: redo event model
 *  - 02/16/09: add tracks
 *  - 02/17/09: add word roles
 *  - 02/18/09: add labels, field set
 *
 * @author konstas
 */
public class GenerativeEvent3Model extends Event3Model implements Serializable
{  
    public GenerativeEvent3Model(Options opts)
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

//                ois.readObject(); ois.readObject();
//
//                for(EventType e : eventTypes)
//                {
//                    for(Field f : e.fields)
//                    {
//                        if(f instanceof CatField)
//                        {
////                            ((CatField)f).setIndexer((Indexer<String>)ois.readObject());
//                            ois.readObject();
//                        }
//                        else if(f instanceof StrField)
//                        {
////                            ((StrField)f).setIndexer((Indexer<StrField.ArrayPair>)ois.readObject());
//                            ois.readObject();
//                        }
//                    }
//                }
            //eventTypesBuffer = (ArrayList<EventType>) ois.readObject();            
            params = newParams();
//            params.setVecs((List<ProbVec>) ois.readObject());
            params.setVecs((Map<String, Vec>) ois.readObject());                        
//            System.out.println("BEFORE\n" +((Params)params).cfgParams.outputNonZero(ParamsType.PROBS));
//            }
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

    public void saveParams(String name, String filename)
    {
        try
        {
            ObjectOutputStream oos = IOUtils.openObjOut(filename);
            oos.writeObject(wordIndexer);
            oos.writeObject(labelIndexer);
            oos.writeObject(eventTypes); // NEW
            oos.writeObject(params.getVecs());
            oos.close();
        }
        catch (IOException ex)
        {
            Utils.log(ex.getMessage());
            ex.printStackTrace(LogInfo.stderr);
        }
    }
    
    @Override
    public void saveParams(String name)
    {
        saveParams(name, Execution.getFile(name + ".params.obj.gz"));
    }

    public void restoreArtificialInitParams()
    {
        // restore estimates from corpus and replace what is already in the params file
        CFGParams cfgParams = ((Params)params).cfgParams; 
        cfgParams.setUniform(0); // reset the cfg rules vectors to 0
        Map<Integer, Vec> cfgRulesChoices = cfgParams.getCfgRulesChoices();
        for(AExample aex : examples)
        {
            Tree<String> tree = ((Example)aex).getTrueWidget().getRecordTree();
            if(tree == null)
            {
                LogInfo.error("Input file does not contain parse trees!");
                Execution.finish();
            }
            // add 1 count to each cfg rule in each subtree of the parse tree
            for(Iterator<Tree> it = tree.iterator(); it.hasNext(); )
            {
                Tree<String> subtree = it.next();
                if(Utils.countableRule(subtree)) // count only the binary rules
                {
                    CFGRule rule = new CFGRule(subtree, rulesIndexer);
                    cfgRulesChoices.get(rule.getLhs()).addCount(getCfgRuleIndex(rule), 1.0);
                }
            }
        }
        cfgParams.optimise(opts.initSmoothing);
        for(Vec v : cfgParams.getVecs().values())
            v.setProbSortedIndices();
//        System.out.println("AFTER\n" +((Params)params).cfgParams.outputNonZero(ParamsType.PROBS));
    }
    
    @Override
    protected void artificialInitParams()
    {
        params = newParams();
        // initialise randomly all parameters
        params.randomise(opts.initRandom, opts.initNoise);
        params.optimise(opts.initSmoothing);
        
        // update the cfg rules with estimates from input parse trees        
        CFGParams cfgParams = ((Params)params).cfgParams; 
        cfgParams.setUniform(0); // reset the cfg rules vectors to 0
        Map<Integer, Vec> cfgRulesChoices = cfgParams.getCfgRulesChoices();
        for(AExample aex : examples)
        {
            Tree<String> tree = ((Example)aex).getTrueWidget().getRecordTree();
            if(tree == null)
            {
                LogInfo.error("Input file does not contain parse trees!");
                Execution.finish();
            }
            // add 1 count to each cfg rule in each subtree of the parse tree
            for(Iterator<Tree> it = tree.iterator(); it.hasNext(); )
            {
                Tree<String> subtree = it.next();
                if(Utils.countableRule(subtree)) // count only the binary rules
                {
                    CFGRule rule = new CFGRule(subtree, rulesIndexer);
                    cfgRulesChoices.get(rule.getLhs()).addCount(getCfgRuleIndex(rule), 1.0);
                }
            }
        }
//        System.out.println(cfgParams.outputNonZero(ParamsType.COUNTS));
//        cfgParams.optimiseVar(opts.stage1.smoothing);
        cfgParams.optimise(opts.initSmoothing);
    }            
    
    @Override
    protected void baitInitParams()
    { // Hard code things
        params = newParams();
        params.setUniform(1);
    }
    @Override
    protected Params newParams()
    {
        if(!opts.fixRecordSelection || params == null)
            return new Params(this, opts, VecFactory.Type.DENSE);
        else // in case we are using a treebank for record selection, copy rule probabilites from previous iteration
        {
            Params p = new Params(this, opts, VecFactory.Type.DENSE);
            p.cfgParams.setVecs(((Params)params).cfgParams.getVecs());
//            p.cfgParams = ((Params)params).cfgParams;
            return p;
        }
    }

    @Override
    protected APerformance newPerformance()
    {
        switch(opts.modelType)
        {
            case generate : case generatePcfg : return new GenerationPerformance(this);
            case semParse : return new SemParsePerformance(this);
            default : return new AlignmentPerformance(this);
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
            
            record(String.valueOf(iter), name, complexity, output);
            if(trainPredOut != null) trainPredOut.close();
            if(testPredOut != null) testPredOut.close();
            if(trainFullPredOut != null)
            {
                if(opts.forceOutputOrder)
                    writeFullPredOut(trainFullPredOut, fullPredOutArray);
                trainFullPredOut.close();
            }
            if(testFullPredOut != null) 
            {
                if(opts.forceOutputOrder)
                    writeFullPredOut(testFullPredOut, fullPredOutArray);
                testFullPredOut.close();
            }

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
        if(!opts.dontOutputParams)
            params.outputNonZero(Execution.getFile(name+".params.gz"), ParamsType.PROBS);
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
            testPcfgTreesPredOut = (opts.outputPcfgTrees) ? 
                    IOUtils.openOut(Execution.getFile(
                    name+".test.full-pred-trees-gen")) : null;
        }
        catch(Exception ioe)
        {
            Utils.begin_track("Error opening file(s) for writing. No output will be written!");
            LogInfo.end_track();
        }                
        // E-step
        Utils.begin_track("Generation-step " + name);
//        Params counts = newParams();
        Collection<BatchEM> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new BatchEM(i, examples.get(i), null, temperature, // used to have counts instead of null, but they are never used
                    lopts, 0, complexity));                
        }
        Utils.parallelForeach(opts.numThreads, list);
        LogInfo.end_track();
        list.clear();

        if(testFullPredOut != null) 
        {
            if(opts.forceOutputOrder)
                writeFullPredOut(testFullPredOut, fullPredOutArray);
            testFullPredOut.close();
        }
        if(testPredOut != null)
        {
            // write prediction file footer, conforming to SGML NIST standard
            testPredOut.println("</tstset>\n</mteval>");
            testPredOut.close();
        }
        if(testPcfgTreesPredOut != null) 
        {
            if(opts.forceOutputOrder)
                writeFullPredOut(testPcfgTreesPredOut, pcfgTreesPredOutArray);
            testPcfgTreesPredOut.close();
        }
        Execution.putOutput("currExample", examples.size());

        // Final
//        testPerformance.output(Execution.getFile(name+".test.performance"));
        Record.begin("generation");
        record("results", name, complexity, true);
        Record.end();
        LogInfo.end_track();
    }    

    /**
     * helper method for testing the semantic parse output. Simulates generate(...) method
     * for a single example without the thread mechanism
     * @return the accuracy of the semantic parsing
     */
    public String testSemParse(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        if(opts.ngramWrapper == NgramWrapper.kylm)
            ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        else if(opts.ngramWrapper == NgramWrapper.srilm)
            ngramModel = new SrilmNgramWrapper(opts.ngramModelFile, opts.ngramSize);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        AParams counts = newParams(); int i = 0;
        for(AExample ex: examples)
        {
//        Example ex = examples.get(0);
            try
            {
                AInferState inferState =  createInferState(ex, 1, counts, temperature,
                        lopts, 0, complexity);
                testPerformance.add(ex, inferState.bestWidget);
                System.out.println(widgetToFullString(ex, inferState.bestWidget));
            }
            catch(Exception e)
            {
                System.out.println(i+ " " + e.getMessage());
                e.printStackTrace(LogInfo.stderr);
//                e.printStackTrace();
            }
            i++;
        }

        return testPerformance.output();
    }

    public Graph testSemParseVisualise(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
//        ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        AParams counts = newParams();
        AExample ex = examples.get(0);
        Graph graph = new DirectedSparseGraph<String, String>();
        AInferState inferState =  createInferState(ex, 1, counts, temperature,
                lopts, 0, complexity, graph);
        testPerformance.add(ex, inferState.bestWidget);
        System.out.println(widgetToFullString(ex, inferState.bestWidget));
                                    
        return graph;
    }    
    
    @Override
    protected AInferState newInferState(AExample aex, AParams aparams,
                                                 AParams acounts, InferSpec ispec)
    {
        Example ex = (Example)aex;
        Params params = (Params)aparams;
        Params counts = (Params)acounts;
        
        switch(opts.modelType)
        {
            case generate : return new GenInferState(this, ex, params, counts, ispec, ngramModel);
            case generatePcfg : return new GenInferStatePCFG(this, ex, params, counts, ispec, ngramModel);
            case semParse : return new SemParseInferState(this, ex, params, counts, ispec, ngramModel);
            case event3pcfg : return new InferStatePCFG(this, ex, params, counts, ispec);
            case event3 : default : return new InferState(this, ex, params, counts, ispec);
        }
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aparams, AParams acounts,
                                           InferSpec ispec, Graph graph)
    {
        Example ex = (Example)aex;
        Params params = (Params)aparams;
        Params counts = (Params)acounts;
        
        switch(opts.modelType)
        {
            case generate: return new GenInferState(this, ex, params, counts, ispec, ngramModel, graph);
            case semParse: default: return new SemParseInferState(this, ex, params, counts, ispec, graph);
        }        
    }
}
