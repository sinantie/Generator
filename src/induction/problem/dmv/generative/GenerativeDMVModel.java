package induction.problem.dmv.generative;

import edu.berkeley.nlp.ling.Tree;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.FullStatFig;
import fig.basic.IOUtils;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import fig.record.Record;
import induction.DepTree;
import induction.LearnOptions;
import induction.MyCallable;
import induction.Options;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.AInferState;
import induction.problem.AParams;
import induction.problem.AParams.ParamsType;
import induction.problem.APerformance;
import induction.problem.AWidget;
import induction.problem.InductionUtils;
import induction.problem.InferSpec;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.dmv.params.Params;
import induction.problem.event3.Event3Model;
import induction.problem.wordproblem.WordModel;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Rewrite of the DMV model (Klein and Manning 2006), based on the implementation
 * of Percy Liang in Scala. It can parse both mrg and raw files.
 * @author konstas
 */
public class GenerativeDMVModel extends WordModel implements Serializable
{

    private boolean useHarmonicWeights = false;
    private Indexer<Integer>[] localWordIndexer;
    
    public GenerativeDMVModel(Options opts)
    {
        super(opts);
    }

    public Indexer<Integer>[] getLocalWordIndexer()
    {
        return localWordIndexer;
    }
        
    public int wordIndexerLength(int w)
    {
        return localWordIndexer[w].size();
    }
    
    public String wordIndexerToString(int w1, int wordToInteger)
    {
        return wordToString(localWordIndexer[w1].getObject(wordToInteger));
    }
  
    public List<String>[] wordIndexerToArray()
    {
        int W = W();
        List<String>[] out = new ArrayList[W];
        for(int i = 0; i < W; i++)
        {
            out[i] = new ArrayList<String>(wordIndexerLength(i));
            for(int j = 0; j < localWordIndexer[i].size(); j++)
                out[i].add(wordIndexerToString(i, j));
        }
        return out;
    }
    
    /**
     * Form the bilexical index mapping based on examples. Not recommended
     * when using the lexicalised version of DMV, as there might be unseen
     * words in the test set.
     */
    @Override
    public void preInit()
    {
        localWordIndexer = new Indexer[WordModel.W()];
        for(int i = 0; i < localWordIndexer.length; i++)
            localWordIndexer[i] = new Indexer<Integer>();        
        if(opts.useTagsAsWords)
        {            
            for(AExample ex : examples)
            {
                int[] text = ex.getText();
                int N = text.length;
                for(int i = 0; i < N; i++)
                {                
                    for(int j = 0; j < N; j++)
                        localWordIndexer[text[i]].getIndex(text[j]);
                } // for
            } // for
        } // if
        else // bad way to do this...we are replicating the same vector many times
        {
            for(int i = 0; i < localWordIndexer.length; i++)
                for(int j = 0; j < WordModel.W(); j++)
                    localWordIndexer[i].getIndex(j);
        }                
    }
    
    @Override
    protected void readFromSingleFile(ArrayList<String> inputLists)
    {
        if (opts.inputFormat == Options.InputFormat.mrg)
        {
            for(String file : inputLists)
            {
                if(new File(file).exists())
                {               
                    try
                    {
                        List<Tree<String>> trees = Utils.loadTrees(file, opts.removePunctuation);
                        for(Tree tree : trees)
                        {
                            readExamples(tree, maxExamples);
                        }
                    }
                    catch(IOException ioe)
                    {
                        LogInfo.error("Error loading file " + file);
                    }                
                } // if  
            } // for
        }
        else
            super.readFromSingleFile(inputLists);
    }
    
    protected void readExamples(Tree input, int maxExamples)
    {        
        Tree tempTree = null;
        try
        {
            tempTree = input;
            List words = opts.useTagsAsWords ? input.getPreTerminalYield() : input.getYield();
            if(words.size() <= opts.maxExampleLength)
                examples.add(new DMVExample(InductionUtils.indexWordsOfText(wordIndexer, words), 
                             DepTree.toDepTree(input), "Example_" + numExamples++));
        }
        catch(Exception e)
        {
            LogInfo.error("Error reading " + numExamples + " " + tempTree);
            e.printStackTrace();
        }        
    }
    
    /**
     * In case we don't have the dependencies' information. Works only with .events files!
     * @param input
     * @param maxExamples 
     */
    @Override
    protected void readExamples(String input, int maxExamples)
    {
        if(opts.inputFileExt.equals("events"))
        {
            String[] res = Event3Model.extractExampleFromString(input); // res[0] = name, res[1] = text
            List words;
            if(opts.useTagsAsWords)
                words = posTag(res[1]);
            else
                words = Arrays.asList(res[1].split(" "));
            if(words.size() <= opts.maxExampleLength)
                examples.add(new DMVExample(InductionUtils.indexWordsOfText(wordIndexer, words), null, res[0]));
        }
    }
    
    /**
     * POS tag a sentence using the Stanford MaxEnt POS Tagger
     * @param sentence
     * @return 
     */
    private List<String> posTag(String sentence)
    {
        List<String> out = new ArrayList<String>();
        String[] tokens = posTagger.tagString(sentence).split(" ");
        for(String token : tokens)
            out.add(token.substring(token.lastIndexOf("/") + 1));
        return out;
    }
    
    @Override
    protected Params newParams()
    {
        return new Params(this, opts, VecFactory.Type.DENSE);
    }

    /**
     * Initialise with an E-step which puts a uniform distribution over z
     * This works for models with natural asymmetries such as word alignment and DMV,
     * but not for cluster-based models such as GMMs, PMMMs, HMMs,
     * where random initialisation is preferred (need noise)
     */
    @Override
    protected void baitInitParams()
    {
        Utils.begin_track("baitInitParams: using harmonic initializer");
        Params counts = newParams();
        params = newParams(); 
        params.setUniform(1);       
        Collection<BatchBaitInit> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new BatchBaitInit(i, examples.get(i), counts));
        }
        Utils.parallelForeach(opts.numThreads, list);
        params = counts;
        params.optimise(opts.initSmoothing);
        LogInfo.end_track();
    }
              
    @Override
    protected APerformance newPerformance()
    {
        return new DMVPerformance();
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aparams, AParams acounts, InferSpec ispec)
    {
        DMVExample ex = (DMVExample)aex;
        Params localParams = (Params)aparams;
        Params counts = (Params)acounts;
        
        return new DMVInferState(this, ex, localParams, counts, ispec, useHarmonicWeights);
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aparams, AParams acounts, InferSpec ispec, Graph graph)
    {
        DMVExample ex = (DMVExample)aex;
        Params localParams = (Params)aparams;
        Params counts = (Params)acounts;
        
        return new DMVInferState(this, ex, localParams, counts, ispec, useHarmonicWeights, graph);
        
    }

    /**
     * Copy-pasted from Percy's code - so far it is incomplete
     * @param index
     * @return 
     */
    @Override
    protected AExample genExample(int index)
    {
        int N = opts.genMaxTokens;
        // TODO
        int[] words = null;
        return new DMVExample(words, new DepTree(null));
    }

    @Override
    protected Integer[] widgetToIntSeq(AWidget widget)
    {
        return Utils.int2Integer(((DepTree)widget).getParent());
    }

    @Override
    protected String widgetToSGMLOutput(AExample ex, AWidget widget)
    {
        return widgetToFullString(ex, widget);
    }

    /**
     * Outputs the gold-standard dependency tree
     * @param aex
     * @return 
     */
    @Override
    protected String exampleToString(AExample aex)
    {
        DMVExample ex = (DMVExample)aex;
        StringBuilder out = new StringBuilder(ex.getName()); 
        out.append(" : ");
        for(int i = 0; i < ex.N(); i++)
            out.append(i).append(wordToString(ex.getText()[i])).append("->").
                    append(ex.getTrueWidget().getParent()[i]).append(" ");        
        return out.toString();
    }

    @Override
    protected String widgetToFullString(AExample ex, AWidget widget)
    {
        return ((DMVExample)ex).widgetToNiceFullString((DepTree)widget);
        // (it outputs the trueWidget dependencies, and a list of the heads for each word
//        return super.widgetToFullString(ex, widget); 
    }

    
    @Override
    protected void saveParams(String name)
    {
        try
        {
            ObjectOutputStream oos = IOUtils.openObjOut(Execution.getFile(name + ".dmv.params.obj.gz"));
            oos.writeObject(wordIndexer);
            oos.writeObject(localWordIndexer);            
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
    public void stagedInitParams()
    {
        Utils.begin_track("stagedInitParams");
        try
        {
            Utils.log("Loading " + opts.stagedParamsFile);
            ObjectInputStream ois = IOUtils.openObjIn(opts.stagedParamsFile);
            wordIndexer = (Indexer<String>) ois.readObject();
            localWordIndexer = (Indexer<Integer>[]) ois.readObject();
            params = newParams();
            params.setVecs((Map<String, Vec>) ois.readObject());
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ opts.stagedParamsFile);            
            ioe.printStackTrace(LogInfo.stderr);
            Execution.finish();
        }
        LogInfo.end_track();
    }

    @Override
    public void learn(String name, LearnOptions lopts)
    {
        useHarmonicWeights = false;
        
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
        if(!opts.dontOutputParams)
        {
            saveParams(name);             
            params.output(Execution.getFile(name+".params"), ParamsType.PROBS);
        }
        LogInfo.end_track();
        LogInfo.end_track();
        Record.end();
        Record.end();
        Execution.putOutput("currIter", lopts.numIters);        
    }

    /**
     * Test on a specific dataset given the learned parameters.
     * @param name
     * @param lopts 
     */
    @Override
    public void generate(String name, LearnOptions lopts)
    {
//        saveParams("stage1");
//        System.exit(1);
        useHarmonicWeights = false;
        FullStatFig complexity = new FullStatFig(); // Complexity inference (number of hypergraph nodes)
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();       
        try
        {
            testFullPredOut = (opts.outputFullPred) ?
                IOUtils.openOut(Execution.getFile(
                name+".test.full-pred-gen")) : null;
            testPredOut = IOUtils.openOut(Execution.getFile(name+".tst.seq"));            
        }
        catch(Exception ioe)
        {
            Utils.begin_track("Error opening file(s) for writing. No output will be written!");
            LogInfo.end_track();
        }                
        // E-step
        Utils.begin_track("Testing " + name);
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

        if(testFullPredOut != null) testFullPredOut.close();
        if(testPredOut != null) testPredOut.close();
        Execution.putOutput("currExample", examples.size());

        // Final
//        testPerformance.output(Execution.getFile(name+".test.performance"));
        Record.begin("test");
        record("results", name, complexity);
        Record.end();
        LogInfo.end_track();
    }       
    
    public String testInitLearn(String name, LearnOptions lopts)
    {
        useHarmonicWeights = false;
        return super.testInitLearn(name, lopts);
    }
    
    protected class BatchBaitInit extends MyCallable
    {

        AExample ex; 
        int i; 
        AParams counts;
        boolean outputLog;
        
        public BatchBaitInit(int i, AExample ex, AParams counts)
        {
            this.ex = ex;
            this.i = i;
            this.counts = counts;
            outputLog = opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0;
        }
        
        @Override
        public Object call() throws Exception
        {
            if(outputLog)            
                Utils.begin_track("Example %s/%s", Utils.fmt(i+1), Utils.fmt(examples.size()));
                initExample();
            if(outputLog)
                LogInfo.end_track();
            
            return null;
        }
        
        private void initExample()
        {
            useHarmonicWeights = true;
            AInferState currentInferState = newInferState(ex, params, counts, 
                    new InferSpec(1, true, false, false, false, false, false, 1, -1));
            currentInferState.createHypergraph();
            currentInferState.doInference(); // We don't need to do inference, we are only initialising the parameters
            currentInferState.updateCounts();
            
        }       
    }
}
