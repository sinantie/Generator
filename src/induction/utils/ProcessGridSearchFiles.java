package induction.utils;

import induction.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author konstas
 */
public class ProcessGridSearchFiles
{
    final String path, formattedFolder, outputFile;
    Map<Double, BlockOfResults> blocksMap;
    int[] posOfParams;
    String[] formattedStringFragments;
    enum Type {GENERATION, ALIGN};
    Type type;
    
    private ProcessGridSearchFiles(String path, String formattedFolder, String outputFile, Type type)
    {
        this.path = path;
        this.formattedFolder = formattedFolder;
        this.outputFile = outputFile;
        this.type = type;
        blocksMap = new TreeMap<Double, BlockOfResults>();
        parseFormattedString();
    }
    public void execute()
    {
        File pathFile = new File(path);
        if(pathFile.exists())
        {            
            for(File f : pathFile.listFiles())
            {
                String[] res = parseDirName(f, posOfParams);
                if(res != null)
                    addResult(res[0], new Result(res));
            } // for
            saveOutputToFile();
        } // if
    }
    
    private void saveOutputToFile()
    {
        StringBuilder str = new StringBuilder();
        for(BlockOfResults block : blocksMap.values())
        {
            str.append(block).append("\n");
        }
        Utils.write(outputFile, str.toString());
    }
    
    private void addResult(String param, Result res)
    {
        Double paramDouble = Double.valueOf(param);
        if(blocksMap.containsKey(paramDouble))
            blocksMap.get(paramDouble).addResult(res);
        else
        {
            blocksMap.put(paramDouble, new BlockOfResults(param, res));
        }
    }
    
    private String[] parseDirName(File f, int[] posOfParams)
    {
        if(!f.isDirectory())
            return null;        
        String[] res = new String[3];
        String dirName = f.getName();
        // do sanity check: input file matches the formatted string path. Record
        // the position of each string fragment in the original dirname
        int index = 0;
        int[] pos = new int[formattedStringFragments.length];
        for(int i = 0; i < formattedStringFragments.length; i++)
        {            
            if(!(i == posOfParams[0] || i == posOfParams[1]))
            {
                if((index = dirName.indexOf(formattedStringFragments[i])) != -1)
                {
                    pos[i] = index;
                }
                else
                    return null; // path not matching our pattern
            }                        
        } // for
        index = posOfParams[0]; // position of fragment containing the actual parameter
        res[0] = dirName.substring( index > 0 ? pos[index - 1] + formattedStringFragments[index - 1].length() : formattedStringFragments[0].length(), 
                                    index < pos.length - 1 ? pos[index + 1] : dirName.length());
        index = posOfParams[1];
        res[1] = dirName.substring( index > 0 ? pos[index - 1] + formattedStringFragments[index - 1].length() : formattedStringFragments[0].length(), 
                                    index < pos.length - 1 ? pos[index + 1] : dirName.length());
        
        
        res[2] = dirName;
        return res;
    }
    
    private void parseFormattedString()
    {
        posOfParams = new int[2];        
        formattedStringFragments = formattedFolder.split("[$]");
        for(int i = 0; i < formattedStringFragments.length; i++)
        {            
            if(formattedStringFragments[i].equals("param1"))
              posOfParams[0] = i;  
            else if(formattedStringFragments[i].equals("param2"))
                posOfParams[1] = i;
        }               
    }
    
    public static void main(String[] args)
    {
        String path = "results/output/atis/generation/dependencies_uniformZ/grid/";
        String formattedString = "model_3_$param1$-best_0.01_STOP_inter$param2$_condLM_hypRecomb_lmLEX_POS_predLength";
        String outputFile = "results/output/atis/generation/dependencies_uniformZ/grid/grid.results";        
        
        if(args.length == 2)
        {
            path = args[0];            
            outputFile = args[1];
        }
        if(args.length == 3)
        {
            path = args[0];
            formattedString = args[1];
            outputFile = args[2];
        }
        Type type = Type.GENERATION;
        ProcessGridSearchFiles pgsf = new ProcessGridSearchFiles(path, formattedString, outputFile, type);
        pgsf.execute();
    }
    
    class BlockOfResults implements Comparable<BlockOfResults>
    {
        Set<Result> block;
        String param;
        
        public BlockOfResults(String param)
        {
            this.param = param;
            block = new TreeSet<Result>();
        }
        
        /**
         * 
         * @param param
         * @param res the first result to add
         */
        public BlockOfResults(String param, Result res)
        {
            this(param);
            addResult(res);
        }
        
        public void addResult(Result res)
        {
            block.add(res);
        }
        
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(Result res : block)
                str.append(res).append("\n");
            return str.toString();
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof BlockOfResults;
            BlockOfResults bor = (BlockOfResults)obj;
            return this.param.equals(bor.param);
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 47 * hash + (this.param != null ? this.param.hashCode() : 0);
            return hash;
        }

        
        @Override
        public int compareTo(BlockOfResults o)
        {
            return Double.valueOf(param) - Double.valueOf(o.param) > 0 ? 1 : -1;
        }
        
    }
    class Result implements Comparable<Result>
    {
        String param1, param2, dirName;
        List<String> results;

        public Result(String param1, String param2, String dirName)
        {
            this.param1 = param1;
            this.param2 = param2;
            this.dirName = dirName;
            results = new ArrayList<String>();
            try
            {
                parseResults(path + dirName + "/stage1.test.performance.results");
            }
            catch(Exception e)
            {
                System.err.println("Error in directory " + dirName);
            }
        }
        
        public Result(String[] res)
        {
            this(res[0], res[1], res[2]);
        }
        
        private void parseResults(String filename)
        {
            String lines[] = Utils.readLines(filename);
            if (type == Type.GENERATION)
            {
                boolean foundBleu = false, foundMeteor = false;
                for(String line : lines)
                {
                    if(line.equals("BLEU scores"))
                        foundBleu = true;
                    if(foundBleu && line.contains("Averaged Bleu scores:"))
                    {
                        results.add(parseResult(line));
                        foundBleu = false;
                    } // if BLEU
                    if(line.equals("METEOR scores"))
                        foundMeteor = true;
                    if(foundMeteor && line.contains("Final score:"))
                    {
                        results.add(parseResult(line));
                        foundMeteor = false;
                    } // if METEOR
                    if(line.contains("Total Recall:"))
                        results.add(parseResult(line));
                } // for
            } // if
        }
        
        private String parseResult(String in)
        {
            return Utils.fmt(Double.valueOf(in.split(":")[1].trim()));
        }
        
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            str.append(param1).append("\t").append(param2).append(":\t");
            for(String res : results)
                str.append(res).append("\t");
            return str.toString();
        }

        @Override
        public int compareTo(Result r)
        {            
            return (Double.valueOf(param1) + Double.valueOf(param2)) -
                   (Double.valueOf(r.param1) + Double.valueOf(r.param2)) >= 0 ? 1 : -1; 
        }
    }
}
