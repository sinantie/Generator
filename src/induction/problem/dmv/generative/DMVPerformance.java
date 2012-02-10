package induction.problem.dmv.generative;

import fig.basic.StatFig;
import induction.DepTree;
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
    
    
//    def add(trueTree:DepTree, predTree:DepTree) {
//      if (trueTree != null) {
//        foreach(Utils.same(trueTree.N, predTree.N), { i:Int =>
//          if (!trueTree.isRoot(i)) {
//            val pi = trueTree.parent(i)
//            directed.add(predTree.parent(i) == pi)
//            undirected.add(predTree.parent(i) == pi || predTree.parent(pi) == i)
//          }
//        })
//      }

    @Override
    public double getAccuracy()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void add(DepTree trueTree, DepTree predTree)
    {
        if(trueTree != null)
        {
            //TO-DO
        }
    }

    @Override
    public void add(AExample trueWidget, DepTree predWidget)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String output()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
