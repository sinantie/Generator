package induction.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author konstas
 */
public class ProcessPerformanceFiles
{
    private String dirPath;

    public ProcessPerformanceFiles(String dirPath)
    {
        this.dirPath = dirPath;
    }

    public void execute()
    {        
        HashMap<String, TreeSet<Performance>> mapOfSets = new HashMap();
        TreeSet<Performance> set;
        String[] tokens;
        Performance per;
        File dirFile = new File(dirPath);
        for(File f: dirFile.listFiles(new FilenameFilter()
           {
                public boolean accept(File dir, String name)
                {
                    return !name.contains(".performance.out") &&
                            name.contains(".performance.");
                }
           }))
        {
            /*filename has the format: stageN.train.performance.XX, where N is
              the stage number and XX is the current iteration*/
            String[] fileTokens = f.getName().split("\\.");
            String name = fileTokens[0];
            /*we store stages differently, as they correspond to different experiments*/
            if(mapOfSets.containsKey(name))
            {
                set = mapOfSets.get(name);
            }
            else
            {
                set = new TreeSet<Performance>();
                mapOfSets.put(name, set);
            }
            per = new Performance(Integer.valueOf(fileTokens[3]) + 1); // iterations are 0-based
            try
            {
                BufferedReader fin = new BufferedReader(new FileReader(f));
                // easy task since we know the format of the file!
                per.logZ = Double.valueOf(fin.readLine().split("\t")[1]); // logZ
                per.logVZ = Double.valueOf(fin.readLine().split("\t")[1]); // logVZ
                per.logCZ = Double.valueOf(fin.readLine().split("\t")[1]); // logCZ
                fin.readLine(); // elogZ, ignore
                fin.readLine(); // entropy, ignore
                fin.readLine(); // objective, ignore
                fin.readLine(); // accuracy, ignore
                tokens = fin.readLine().split(" "); // precision, recall and F1
                per.precision = Double.valueOf(tokens[2].substring(0,tokens[2].length() - 1)); //, in the end
                per.recall = Double.valueOf(tokens[5].substring(0,tokens[5].length() - 1)); //, in the end
                per.f1 = Double.valueOf(tokens[8]);
                fin.close();

                set.add(per);
            }
            catch(IOException ioe)
            {
                System.err.println("Could not load file " + f.getName());
            }
            printOutput(mapOfSets);
        }
    }

    private void printOutput(HashMap<String, TreeSet<Performance>> mapOfSets)
    {
        for(String name : mapOfSets.keySet())
        {
            TreeSet<Performance> set = mapOfSets.get(name);
            try
            {
                FileOutputStream fos = new FileOutputStream(dirPath +"/" + name + ".performance.out");
                // write header
                fos.write("iter\tPrecision\tRecall\tF1\tlogZ\tlogVZ\tlogCZ\n".getBytes());

                for(Performance per : set)
                {
                    fos.write(per.toString().getBytes());
                }
                fos.close();
            }
            catch(IOException ioe)
            {
                System.err.println("Cannot write " + name + ".performance.out");
            }
        }
    }

    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("You need to enter a valid directory path");
            System.exit(1);
        }
        ProcessPerformanceFiles ppf = new ProcessPerformanceFiles(args[0]);
        ppf.execute();
    }

    class Performance implements Comparable
    {
        double logZ, logVZ, logCZ, precision, recall, f1;
        int iter;

        Performance(int iter)
        {
            this.iter = iter;
        }

        public int compareTo(Object o)
        {
            assert o instanceof Performance;
            Performance p = (Performance)o;
            return iter - p.iter;
        }

        @Override
        public String toString()
        {
            return iter + "\t" + precision + "\t" + recall + "\t" + f1 + "\t" +
                    logZ + "\t" + logVZ + "\t" + logCZ + "\n";
        }
    }
}
