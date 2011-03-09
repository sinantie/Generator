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
public class CrossFoldPartition
{
    private String source, destPath, prefixName;
    private int folds;

    private CrossFoldPartition(String source, String destPath, String prefixName, int folds)
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
        List[] partitions = new List[folds];
        int partitionSize = list.size() / folds, index;
        for(int i = 0; i < folds; i++)
        {
            index = i*partitionSize;
            partitions[i] = list.subList(index,
                    i<folds-1 ? index + partitionSize : list.size());
        }

        for(int i = 0; i < folds; i++)
        {
            String trainFile = String.format("%s/%sFold%dPathsTrain",destPath, prefixName, i+1);
            List<List<String>> subs = new ArrayList<List<String>>(folds-1);
            for(int j = 0; j < folds; j++)
            {
                if(i !=j)
                    subs.add(partitions[j]);
            }
            writePaths(trainFile, subs);
            String evalFile = String.format("%s/%sFold%dPathsEval",destPath, prefixName, i+1);
            subs.clear();subs.add(partitions[i]);
            writePaths(evalFile, subs);
        }
    }

    private void writePaths(String filename, List<List<String>> lists)
    {
        try
        {
            PrintWriter out = IOUtils.openOut(filename);
            for(List<String> list : lists)
            {
                IOUtils.printLines(out, list);
            }
            out.close();
        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    public static void main(String[] args)
    {
        String source = "robocupLists/robocupAllPathsTrain";
        String destPath = "robocupLists/randomFolds";
        String prefixName = "robocup";
        int folds = 4;
        CrossFoldPartition cfp = new CrossFoldPartition(source, destPath, prefixName, folds);
        cfp.execute();
    }
}
