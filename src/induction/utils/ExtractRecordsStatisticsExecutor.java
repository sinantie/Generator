package induction.utils;

import fig.exec.Execution;
import induction.Options;

/**
 *
 * @author konstas
 */
public class ExtractRecordsStatisticsExecutor implements Runnable
{
    Options opts = new Options();
    
    @Override
    public void run()
    {
        ExtractRecordsStatistics ers = new ExtractRecordsStatistics(opts);
        ers.execute();
    }
    
    public static void main(String[] args)
    {
        ExtractRecordsStatisticsExecutor x = new ExtractRecordsStatisticsExecutor();
        Execution.run(args, x, x.opts);
    }
}
