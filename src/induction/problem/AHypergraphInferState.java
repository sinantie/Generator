package induction.problem;

import fig.basic.StopWatchSet;
import induction.Hypergraph;
import induction.Options;

/**
 *
 * @author konstas
 */
public abstract class AHypergraphInferState//<Example extends AExample>
                                            <Widget extends AWidget,
                                            Example extends AExample<Widget>,
                                            Params extends AParams>
                      extends AInferState//<Example>
                      <Widget, Example, Params>
{
    protected Options opts;
    protected AModel model;
    protected final Hypergraph hypergraph = new Hypergraph<>();
    //protected final double logZ, elogZ, entropy, logVZ;    

    public AHypergraphInferState(AModel model, Example ex, Params params,
                                 Params counts, InferSpec ispec)
    {
        super(ex, params, counts, ispec);
        this.opts = model.opts;
        this.model = model;               
        
    }

    @Override
    public void createHypergraph(int textLength)
    {
        initInferState(model, textLength);
        StopWatchSet.begin("createHypergraph");
        createHypergraph(hypergraph);
        StopWatchSet.end();
    }    

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
        

    // Main functions to override: specifies the entire model
    protected abstract void createHypergraph(Hypergraph<Widget> hypergraph);
    protected abstract AWidget newWidget();    
    protected abstract void initInferState(AModel model, int textLength);
    public int getComplexity()
    {
        return hypergraph.numNodes();
    }

}
