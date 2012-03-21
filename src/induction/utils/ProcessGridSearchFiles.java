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
    double[] weights;
    boolean crossValidate;
    
    private ProcessGridSearchFiles(String path, String formattedFolder, String outputFile, Type type, boolean crossValidate)
    {
        this.path = path;
        this.formattedFolder = formattedFolder;
        this.outputFile = outputFile;
        this.type = type;
        this.crossValidate = crossValidate;
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
                {
                    Result result = crossValidate ? new CrossResult(res) : new Result(res);
                    result.parseResults();
                    addResult(res[0], result);
                }
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

    public void setWeights(double[] weights)
    {
        this.weights = weights;
    }
    
    public static void main(String[] args)
    {
//        String path = "results/output/atis/generation/dependencies_uniformZ/grid/";
//        String formattedString = "model_3_$param1$-best_0.01_STOP_inter$param2$_condLM_hypRecomb_lmLEX_POS_predLength";
//        String outputFile = "results/output/atis/generation/dependencies_uniformZ/grid/grid.results";        
        String path = "results/output/robocup/generation/dependencies/grid";        
        String formattedString = "model_3_$param1$-best_inter$param2$_new4";
        String outputFile = "results/output/robocup/generation/dependencies/grid/grid.results";        
        double[] weights = {513.0, 365.0, 214.0, 311.0};
        boolean crossValidate = true;
        
        if(args.length == 3)
        {
            path = args[0];
            outputFile = args[1];
            crossValidate = Boolean.valueOf(args[2]);
        }
        if(args.length > 3)
        {
            path = args[0];
            formattedString = args[1];
            outputFile = args[2];
            crossValidate = Boolean.valueOf(args[3]);
        }
        if(args.length == 5)
        {
            String[] ar = args[3].split("#");
            weights = new double[ar.length];
            for(int i = 0; i < ar.length; i++)
            {
                weights[i] = Double.valueOf(ar[i]);
            }
        }
        Type type = Type.GENERATION;
        ProcessGridSearchFiles pgsf = new ProcessGridSearchFiles(path, formattedString, outputFile, type, crossValidate);
        if(weights != null)
            pgsf.setWeights(weights);
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
        List<Double> results;

        public Result(String param1, String param2, String dirName)
        {
            this.param1 = param1;
            this.param2 = param2;
            this.dirName = dirName;                
        }
        
        public Result(String[] res)
        {
            this(res[0], res[1], res[2]);
        }
        
        public void parseResults()
        {
            try
            {
                results = parseResults(path + "/" + dirName);
            }
            catch(Exception e)
            {
                System.err.println("Error in directory " + dirName);
            }
        }
        
        protected List<Double> parseResults(String dirname)
        {
            String filename =  dirname + "/stage1.test.performance.results";
            List<Double> res = new ArrayList<Double>();
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
                        res.add(parseResult(line));
                        foundBleu = false;
                    } // if BLEU
                    if(line.equals("METEOR scores"))
                        foundMeteor = true;
                    if(foundMeteor && line.contains("Final score:"))
                    {
                        res.add(parseResult(line));
                        foundMeteor = false;
                    } // if METEOR
                    if(line.contains("Total Recall:"))
                        res.add(parseResult(line));
                } // for
            } // if
            return res;
        }
        
        private Double parseResult(String in)
        {
//            return Utils.fmt(Double.valueOf(in.split(":")[1].trim()));
            return Double.valueOf(in.split(":")[1].trim());
        }
         
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            str.append(param1).append("\t").append(param2).append(":\t");
            for(Double res : results)
                str.append(Utils.fmt(res)).append("\t");
            return str.toString();
        }

        @Override
        public int compareTo(Result r)
        {            
            return (Double.valueOf(param1) + Double.valueOf(param2)) -
                   (Double.valueOf(r.param1) + Double.valueOf(r.param2)) >= 0 ? 1 : -1; 
        }
    }
    
    class CrossResult extends Result 
    {
        double[] weights;
        
        public CrossResult(String[] res)
        {
            super(res);
        }
        
        public CrossResult(String[] res, double[] weights)
        {
            this(res);
            this.weights = weights;
        }
             
        @Override
        protected List<Double> parseResults(String dirname)
        {
            List<Double> res = new ArrayList<Double>();
            int totalFolds = 0;
            for(String foldDirname : new File(dirname).list())
            {
                if(foldDirname.matches("fold[0-9]+"))
                {
                    add(super.parseResults(dirName + "/" + foldDirname), res, 
                            Integer.valueOf(foldDirname.substring(foldDirname.indexOf("fold") + 4)));
                    totalFolds++;                    
                }
            } // for
            double normaliser = weights == null ? (double) totalFolds : Utils.sum(weights);
            for(int i = 0; i < res.size(); i++)
            {
                res.set(i, res.get(i) / normaliser);
            }            
            return res;
        }
        
        private void add(List<Double> from, List<Double> to, int fold)
        {
            if(from.isEmpty()) // first result set
                to.addAll(from);
            else                
                for(int i = 0; i < Utils.same(from.size(), to.size()); i++)
                {
                    to.set(i, to.get(i) + (weights != null ? weights[fold] * from.get(i) : from.get(i)));
                }
        }
    }
}
