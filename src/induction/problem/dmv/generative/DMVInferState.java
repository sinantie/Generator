package induction.problem.dmv.generative;

import induction.DepTree;
import induction.Hypergraph;
import induction.problem.AHypergraphInferState;
import induction.problem.AModel;
import induction.problem.AWidget;
import induction.problem.InferSpec;
import induction.problem.dmv.params.Params;
import induction.problem.wordproblem.Example;

/**
 *
 * @author konstas
 */
public class DMVInferState extends AHypergraphInferState<DepTree, Example<DepTree>, Params>
{
    
    boolean useHarmonicWeights;
    
    public DMVInferState(AModel model, Example ex, Params params,
                                 Params counts, InferSpec ispec, boolean useHarmonicWeights)
    {
        super(model, ex, params, counts, ispec);
        this.useHarmonicWeights = useHarmonicWeights;
    }
    
    @Override
    protected void createHypergraph(Hypergraph<DepTree> hypergraph)
    {
        throw new UnsupportedOperationException("Create Hypergraph Not supported yet.");
    }

    @Override
    protected AWidget newWidget()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void initInferState(AModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doInference()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCounts()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
