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
import induction.problem.InferSpec;
import induction.problem.dmv.params.Params;
import induction.problem.wordproblem.Example;
import induction.problem.wordproblem.WordModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author konstas
 */
public class GenerativeDMVModel extends WordModel implements Serializable
{

    public GenerativeDMVModel(Options opts)
    {
        super(opts);
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
                        examples.add(new Example(indexWordsOfText(wordIndexer, words), 
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
    
    private int[] indexWordsOfText(Indexer<String> wordIndexer, List<String> text)
    {
        int[] indices = new int[text.size()];
        for(int i = 0; i < indices.length; i++)
            indices[i] = wordIndexer.getIndex(text.get(i));
        return indices;
    }
    
    @Override
    protected Params newParams()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Initialize with an E-step which puts a uniform distribution over z
     * This works for models with natural asymmetries such as word alignment and DMV,
     * but not for cluster-based models such as GMMs, PMMMs, HMMs,
     * where random initialization is preferred (need noise)
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
            //TO-DO
            list.add(new BatchBaitInit());
        }
        params = counts;
        params.optimise(opts.initSmoothing);
        LogInfo.end_track();
    }
    //
//      track("baitInitParams: using harmonic initializer")
//      val counts = newParams
//      params.setUniform_!(1)
//      Utils.parallel_foreach(opts.numThreads, examples, { (i:Int,ex:Example,log:Boolean) =>
//        if(log) track("Example %s/%s", fmt(i), fmt(examples.length))
//        // difference from learn is in the 3rd parameter: hardInfer, which is set to false, and interprets to not perform Viterbi search in the end
//        new InferState(ex, params, counts, InferSpec(1, true, false, false, false, false, false, 1, -1), true).updateCounts
//        if(log) end_track
//      })
//      params = counts
//      params.optimize_!(opts.initSmoothing)
//      end_track
              
    @Override
    protected APerformance newPerformance()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected AInferState newInferState(AExample ex, AParams params, AParams counts, InferSpec ispec)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected AInferState newInferState(AExample ex, AParams params, AParams counts, InferSpec ispec, Graph graph)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected AExample genExample(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Integer[] widgetToIntSeq(AWidget widget)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String widgetToSGMLOutput(AExample ex, AWidget widget)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String exampleToString(AExample ex)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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

        @Override
        public Object call() throws Exception
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
