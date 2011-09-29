package induction.problem;

import fig.basic.StopWatchSet;
import induction.Hypergraph;
import induction.Options;

/**
 *
 * @author konstas
 */
public abstract class AHypergraphInferState<Widget extends AWidget,
                                            Example extends AExample<Widget>,
                                            Params extends AParams>
        extends AInferState<Widget, Example, Params>
{
    protected Options opts;
    protected AModel model;
    protected final Hypergraph hypergraph = new Hypergraph<Widget>();
    //protected final double logZ, elogZ, entropy, logVZ;    

    public AHypergraphInferState(AModel model, Example ex, Params params,
                                 Params counts, InferSpec ispec)
    {
        super(ex, params, counts, ispec);
        this.opts = model.opts;
        this.model = model;               
        
    }

    @Override
    public void createHypergraph()
    {
        initInferState(model);
        StopWatchSet.begin("createHypergraph");
        createHypergraph(hypergraph);
        StopWatchSet.end();
    }
    
    public abstract void doInference();
//    {
//        if(opts.modelType == Options.ModelType.discriminativeTrain ||
//           opts.modelType == Options.ModelType.generate ||
//           opts.modelType == Options.ModelType.semParse)
//        {
//            HyperpathResult result;
//                if(opts.fullPredRandomBaseline)
//            {
//                StopWatchSet.begin("1-best Viterbi");
//                result = hypergraph.oneBestViterbi(newWidget(), opts.initRandom);
//                StopWatchSet.end();
//            }
//            else
//            {
//                StopWatchSet.begin("k-best Viterbi");
//                result = hypergraph.kBestViterbi(newWidget());
//                StopWatchSet.end();
//            }
//            bestWidget = (Widget) result.widget;
////            System.out.println(bestWidget);
//            logVZ = result.logWeight;
//
//        }
//        else
//        {
//            StopWatchSet.begin("computePosteriors");
//    //        hypergraph.computePosteriors(ispec.isHardUpdate());
//            hypergraph.computePosteriors(false);
//            StopWatchSet.end();
//            // Hard inference
//            if (hardInfer)
//            {
//                HyperpathResult result = hypergraph.fetchBestHyperpath(newWidget());
//    //            HyperpathResult<Widget> result = hypergraph.fetchSampleHyperpath(opts.initRandom, newWidget());
//                bestWidget = (Widget)result.widget;
//                logVZ = result.logWeight;
//            }
//            else
//            {
//                bestWidget = newWidget();
//                logVZ = Double.NaN;
//            }
//        } // else
//        updateStats();
//    }

    public void updateStats()
    {
        if (opts.computeELogZEntropy)
        {
            hypergraph.computeELogZEntropy(ispec.isHardUpdate());
        }
        logZ = hypergraph.getLogZ();
        elogZ = hypergraph.getELogZ() * temperature;
        entropy = hypergraph.getEntropy();
        initialiseValues();
    }
    
    public void updateCounts()
    {
        synchronized(counts)
        {
          if(ispec.isMixParamsCounts())
          {
              counts.saveSum();
          }
          StopWatchSet.begin("fetchPosteriors");
          hypergraph.fetchPosteriors(ispec.isHardUpdate());
          StopWatchSet.end();
        }
    }

    // Main functions to override: specifies the entire model
    protected abstract void createHypergraph(Hypergraph<Widget> hypergraph);
    protected abstract Widget newWidget();
    protected abstract void initInferState(AModel model);
    public int getComplexity()
    {
        return hypergraph.numNodes();
    }

}
