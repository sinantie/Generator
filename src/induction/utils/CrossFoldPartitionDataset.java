package induction.utils;

import fig.basic.IOUtils;
import induction.Utils;
import java.io.IOException;
import java.io.PrintWriter;
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
    private String source, destPath, prefixName;
    private int folds;

    private CrossFoldPartitionDataset(String source, String destPath, String prefixName, int folds)
    {
        this.source = source;
        this.destPath = destPath;
        this.prefixName = prefixName;
        this.folds = folds;
    }
   
    public void execute()
    {
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(Utils.readLines(source)));
        Collections.shuffle(list); // reshuffle list
        // partition
        Utils.writePartitions(Utils.partitionList(list, folds), folds, destPath, prefixName);        
    }
    

    public static void main(String[] args)
    {
        String source = "robocupLists/robocupAllPathsTrain";
        String destPath = "robocupLists/randomFolds";
        String prefixName = "robocup";
        int folds = 4;
        CrossFoldPartitionDataset cfp = new CrossFoldPartitionDataset(source, destPath, prefixName, folds);
        cfp.execute();
    }
}
