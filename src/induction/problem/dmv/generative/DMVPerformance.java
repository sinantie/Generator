package induction.problem.dmv.generative;

import fig.basic.StatFig;
import induction.DepTree;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.APerformance;

/**
 *
 * @author konstas
 */
public class DMVPerformance extends APerformance<DepTree>
{
    StatFig directed = new StatFig();
    StatFig undirected = new StatFig();
        
    @Override
    public double getAccuracy()
    {
        return directed.mean();
    }

    @Override
    protected void add(DepTree trueTree, DepTree predTree)
    {
        if(trueTree != null)
        {
            for(int i = 0; i < Utils.same(trueTree.getN(), predTree.getN()); i++)
            {
                if(!trueTree.isRoot(i))
                {
                    int pi = trueTree.getParent()[i];
                    directed.add(predTree.getParent()[i] == pi);
                    undirected.add(predTree.getParent()[i] == pi ||
                                   predTree.getParent()[pi] == i);
                }
            }
        }
    }

    @Override
    public void add(AExample example, DepTree predWidget)
    {
        add((DepTree)example.getTrueWidget(), predWidget);
    }

    @Override
    public String output()
    {
        return "Directed: "   + directed + "\n" + 
               "Undirected: " + undirected;
    }
}
