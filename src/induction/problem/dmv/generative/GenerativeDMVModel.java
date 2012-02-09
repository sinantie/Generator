package induction.problem.dmv.generative;

import edu.berkeley.nlp.ling.Tree;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import induction.DepTree;
import induction.LearnOptions;
import induction.MyCallable;
import induction.Options;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.AInferState;
import induction.problem.AParams;
import induction.problem.APerformance;
import induction.problem.AWidget;
import induction.problem.InductionUtils;
import induction.problem.InferSpec;
import induction.problem.VecFactory;
import induction.problem.dmv.params.Params;
import induction.problem.wordproblem.Example;
import induction.problem.wordproblem.WordModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    public int wordIndexerLength(int w)
    {
        return localWordIndexer[w].size();
    }
    
    public String wordIndexerToString(int w1, int wordToInteger)
    {
        return wordToString(localWordIndexer[w1].getObject(wordToInteger));
    }
  
    @Override
    public void preInit()
    {
        
    }
    
    @Override
    protected void readExamples(String input, int maxExamples)
    {
        if (opts.inputFormat == Options.InputFormat.mrg)
        {
            Tree tempTree = null;
            int counter = 0;
            try
            {
                List<Tree<String>> trees = Utils.loadTrees(input, maxExamples, opts.removePunctuation);
                for(Tree tree : trees)
                {
                    tempTree = tree;
                    List words = opts.useTagsAsWords ? tree.getPreTerminalYield() : tree.getYield();
                    if(words.size() <= opts.maxExampleLength)
                        examples.add(new Example(InductionUtils.indexWordsOfText(wordIndexer, words), 
                                     DepTree.toDepTree(tree), "Example_" + counter++));
                }
            }
            catch(IOException ioe)
            {
                LogInfo.error("Error loading file " + input);
            }
            catch(Exception e)
            {
                LogInfo.error("Error loading " + input + " " + counter + " " + tempTree);
                e.printStackTrace();                
            }
        } // if
        else
            super.readExamples(input, maxExamples);
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
        params.setUniform(1);
        Collection<BatchBaitInit> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new BatchBaitInit(i, examples.get(i), counts));
        }
        params = counts;
        params.optimise(opts.initSmoothing);
        LogInfo.end_track();
    }
              
    @Override
    protected APerformance newPerformance()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected AInferState newInferState(AExample aex, AParams aparams, AParams acounts, InferSpec ispec)
    {
        Example ex = (Example)aex;
        Params localParams = (Params)aparams;
        Params counts = (Params)acounts;
        
        return new DMVInferState(this, ex, localParams, counts, ispec, useHarmonicWeights);
    }

    @Override
    protected AInferState newInferState(AExample ex, AParams params, AParams counts, InferSpec ispec, Graph graph)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return new Example(words, new DepTree(null));
    }

    @Override
    protected Integer[] widgetToIntSeq(AWidget widget)
    {
        return Utils.int2Integer(((DepTree)widget).getParent());
    }

    @Override
    protected String widgetToSGMLOutput(AExample ex, AWidget widget)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String exampleToString(AExample aex)
    {
        Example ex = (Example)aex;
        return ex.getName() + ": " + Utils.mkString(
                InductionUtils.getObject(wordIndexer, ex.getText()), " ");
    }

    @Override
    protected void saveParams(String name)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stagedInitParams()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void learn(String name, LearnOptions lopts)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void generate(String name, LearnOptions lopts)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String testGenerativeLearn(String name, LearnOptions lopts)
    {
        return "YES";
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
                Utils.begin_track("Example %s/%s: %s", Utils.fmt(i+1), Utils.fmt(examples.size()));
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
//            currentInferState.doInference(); // We don't need to do inference, we are only initialising the parameters
            currentInferState.updateCounts();
            
        }       
    }
}
