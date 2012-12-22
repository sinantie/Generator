package induction.utils;

import fig.exec.Execution;
import induction.Utils;
import induction.problem.event3.Event3Example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author konstas
 */
public class CrossFoldPartitionDataset
{
    private String source;
    CrossFoldPartitionDatasetOptions opts;
    
    public CrossFoldPartitionDataset(CrossFoldPartitionDatasetOptions opts)
    {
        this.opts = opts;
        this.source = opts.modelOpts.inputLists.get(0);
    }
   
    public void execute()
    {
        List list = new ArrayList();
        switch(opts.inputType)
        {
            case raw : list.addAll(Arrays.asList(rawInput())); break;
            case event3 : list.addAll(event3Input());
        }
        
        Collections.shuffle(list); // reshuffle list
        // partition
        Utils.writePartitions(Utils.partitionList(list, opts.folds), opts.folds, Execution.execDir, opts.prefix);        
    }
    
    private String[] rawInput()
    {
        return Utils.readLines(source);
    }
    
    private List<Event3Example> event3Input()
    {
        return Utils.readEvent3Examples(source, opts.modelOpts.examplesInSingleFile);
    }

    void testExecute()
    {
        execute();
    }
    
    
}
