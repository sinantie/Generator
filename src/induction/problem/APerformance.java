package induction.problem;

import fig.exec.Execution;
import fig.record.Record;
import induction.MyList;
import induction.Utils;

/**
 *
 * @author konstas
 */
public abstract class APerformance<Widget>
{
    protected ProbStats stats = new ProbStats(0, 0, 0, 0, 0, 0, 0);
    protected void add(ProbStats newStats)
    {
        stats.add(newStats);
    }

    protected MyList<String> foreachStat()
    {
        MyList<String> list = new MyList();
        list.add( "logZ", Utils.fmt(stats.getAvg_logZ()) );
        list.add( "logVZ", Utils.fmt(stats.getAvg_logVZ()) );
        list.add( "logCZ", Utils.fmt(stats.getAvg_logCZ()) );
        list.add( "elogZ", Utils.fmt(stats.getAvg_elogZ()) );
        list.add( "entropy", Utils.fmt(stats.getAvg_entropy()) );
        list.add( "objective", Utils.fmt(stats.getAvg_objective()));
        list.add( "accuracy", Utils.fmt(getAccuracy()));
        return list;
    }

    protected boolean isEmpty()
    {
        return stats.getN() == 0;
    }

    protected String summary()
    {
        return foreachStat().toString(" = ", ", ");
    }

    public void record(String name)
    {
        Utils.logs(name + ": " + summary());
        Record.begin(name);
        for(String[] el : foreachStat())
        {
            Execution.putOutput(name + "." + el[0], el[1]);
            Record.add(el[0], el[1]);
        }
        Record.end();
    }

    protected abstract double getAccuracy();
    protected abstract void add(Widget trueWidget, Widget predWidget);
    protected abstract void add(AExample trueWidget, Widget predWidget);

    protected abstract String output();

    protected void output(String path)
    {
        String out = foreachStat().toString("\t", "\n");
        out += output();
        Utils.write(path, out);
    }
}
