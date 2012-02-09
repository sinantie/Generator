package induction.problem.dmv.params;

import induction.Options;
import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.VecFactory;
import induction.problem.dmv.Constants;
import induction.problem.dmv.generative.GenerativeDMVModel;
import induction.problem.wordproblem.WordModel;

/**
 *
 * @author konstas
 */
public class Params extends AParams
{
    public Vec starts;
    public Vec[][] continues, deps;
    private GenerativeDMVModel model;
    private Options opts;
    
    public Params(GenerativeDMVModel model, Options opts, VecFactory.Type vectorType)
    {
        super();
        this.model = model;
        this.opts = opts;
        int W = WordModel.W();
        starts = VecFactory.zeros(vectorType, W);
        addVec("S", starts);
        continues = VecFactory.zeros3(vectorType, W, Constants.R, Constants.F);
        // NEEDS TESTING!!!!!!!!
        addVec(getLabels(Constants.R, Constants.F, "C ", Constants.R_STR, Constants.F_STR), continues);
        deps = VecFactory.zeros3(vectorType, W, Constants.D, model.wordIndexerLength(W));
        
    }
    
    @Override
    public String output(ParamsType paramsType)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String outputNonZero(ParamsType paramsType)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

//def newParams = new Params(ProbVec.zeros(W), ProbVec.zeros3(W, R, F), ProbVec.zeros3(W, D, { (w:Int,d:Int) => WI(w) }))

//case class Params(starts:ProbVec, continues:Array[Array[ProbVec]], deps:Array[Array[ProbVec]]) extends AParams {
//    def foreachVec(f:(ProbVec => Any)) = {
//      f(starts)
//      continues.foreach { v => v.foreach(f(_)) }
//      deps.foreach { v => v.foreach(f(_)) }
//    }
//    def output(puts:(String => Any)) = {
//      Utils.foreachSorted(starts.getProbs, opts.numOutputParams, true, { (w:Int,v:Double) =>
//        puts(String.format("S %s\t%s", wstr(w), fmt(v)))
//      })
//      puts("")
//
//      Utils.foreachSorted(wordFreqs, true, { (w:Int,freq:Int) =>
//        foreach(R, { r:Int =>
//          Utils.foreachSorted(continues(w)(r).getProbs, true, { (f:Int,v:Double) =>
//            puts(String.format("C %s %s %s\t%s", wstr(w), rstr(r), fstr(f), fmt(v)))
//          })
//          puts("")
//        })
//      })
//      foreach(W, { w1:Int =>
//        foreach(D, { d:Int =>
//          Utils.foreachSorted(deps(w1)(d).getProbs, opts.numOutputParams, true, { (w2i:Int,v:Double) =>
//            puts(String.format("D %s %s %s\t%s", wstr(w1), dstr(d), wistr(w1, w2i), fmt(v)))
//          })
//          puts("")
//        })
//      })
//    }
//  } // Params