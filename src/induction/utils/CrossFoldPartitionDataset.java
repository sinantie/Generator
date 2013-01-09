package induction.utils;

import fig.exec.Execution;
import induction.Utils;
import induction.problem.event3.Event3Example;
import induction.utils.postprocess.ProcessExamples.SplitDocToSentences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
            case event3 : list.addAll(opts.splitDocToSentences ? splitEvent3DocToSentences() : event3Input());
        }
        
        Collections.shuffle(list, new Random(1L)); // reshuffle list always in the same way
        // partition
        Utils.writePartitions(Utils.partitionList(list, opts.folds), opts.folds, Execution.execDir, opts.prefix);        
    }
    
    private String[] rawInput()
    {
        String[] lines = Utils.readLines(source);
        for(int i = 0; i < lines.length; i++)
        {
            lines[i] = lines[i] + "\n";
        }
        return lines;
    }
    
    private List<Event3Example> event3Input()
    {
        
        return Utils.readEvent3Examples(source, opts.modelOpts.examplesInSingleFile);
    }

    private List<String> splitEvent3DocToSentences()
    {
        List<String> list = new ArrayList<String>();
        SplitDocToSentences splitter = new SplitDocToSentences();
        for(Event3Example ex : event3Input())
        {
            List<Event3Example> sents = (List<Event3Example>) splitter.act(ex);
            StringBuilder str = new StringBuilder();
            for(Event3Example sent : sents)
            {
                str.append(sent.toString());
            }
            list.add(str.toString());
        }
        return list;
    }
    
    void testExecute()
    {
        execute();
    }
    
    
}
