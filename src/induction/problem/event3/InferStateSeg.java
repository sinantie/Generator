package induction.problem.event3;

import induction.Hypergraph;
import induction.NgramModel;
import induction.problem.AHypergraphInferState;
import induction.problem.AModel;
import induction.problem.InferSpec;

/**
 *
 * @author konstas
 */
public class InferStateSeg extends AHypergraphInferState<Widget, Example, Params>
{

    protected Event3Model model;
    protected int[] words, nums;
    protected int[] labels;
    protected int L, N, wildcard_pc;

    public InferStateSeg(Event3Model model, Example ex, Params params,
            Params counts, InferSpec ispec, NgramModel ngramModel)
    {
        super(model, ex, params, counts, ispec, ngramModel);
    }

    @Override
    protected void initInferState(AModel model)
    {
        
    }

    @Override
    protected void createHypergraph(Hypergraph<Widget> hypergraph)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Widget newWidget()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
