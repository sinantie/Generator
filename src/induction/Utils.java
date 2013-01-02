package induction;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import edu.berkeley.nlp.ling.Trees.StandardTreeNormalizer;
import fig.basic.IOUtils;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.problem.event3.Constants;
import induction.problem.event3.Constants.TypeAdd;
import induction.problem.event3.Event3Example;
import induction.utils.StringWithEmbeddedInt;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.RealVector;

/**
 *
 * @author konstas
 */
public class Utils
{

    public static double[] add(double[] a, double b)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] += b;
        }
        return a;
    }

    public static double[] add(double[] a, double scale, double[] b)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] += scale * b[i];
        }
        return a;
    }

    public static void begin_track(String format, Object... args)
    {
        LogInfo.track(fmts(format, args));
    }

    public static double[] div(double[] a, double x)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] /= x;
        }
        return a;
    }

    public static boolean isEmpty(String s)
    {
        return s == null || s.length() == 0;
    }

    public static int[] fill(boolean ascending, int fromIndex, int length)
    {
        int[] out = new int[length];
        for(int i = 0; i < length; i++)
        {
            out[i] = fromIndex + i;
        }
        return out;
    }

    public static int[] fill(boolean ascending, int length)
    {
        return fill(ascending, 0, length);
    }

    public static int find(int a, int b, boolean[] ar)
    {
        int i = a;
        while (i < b && !ar[i-1]) i += 1;
        return i;
    }

    public static String fmt(int x)
    {
        return x + "";
    }

    public static String fmt(double x)
    {
        if (Math.abs(x - (int)x) < 1e-40) // An integer (probably)
        {
            return String.valueOf((int) x);
        }
        else if(Math.abs(x) < 1e-3) // Scientific notation (close to 0)
        {
            return String.format("%.2e", x);
        }
        else if(Math.abs(x) > 1e3)
        {
            return String.format("%.2e", x);
        }
        else
        {
            return String.format("%.4f", x);
        }
    }

    public static String fmt(Object x)
    {
        if(x instanceof Double)
        {
            return fmt( ((Double)x).doubleValue() );
        }
        else if(x instanceof Integer)
        {
            return fmt(((Integer)x).intValue());
        }
        else
        {
            return x.toString();
        }
    }

    public static String fmts(String format, Object... args)
    {
        Object[] formattedArgs = new String[args.length];
        for(int i = 0; i < args.length; i++)
        {
            formattedArgs[i] = fmt(args[i]);
        }

        return String.format(format, formattedArgs);
    }

    public static String formatTable(String[][] table, Constants.Justify justify)
    {
        String out = "";
        final int numOfRows = table.length;
        final int numOfCols = table[0].length;

        // find widths of columns
        int[] widths = new int[numOfCols];
        for(int c = 0; c < numOfCols; c++)
        {
            for(int r = 0; r < numOfRows; r++)
            {
                widths[c] = Math.max(widths[c], table[r][c].length());
            }
        }
        int padding = 0;
        for(int r = 0; r < numOfRows; r++)
        {
            for(int c = 0; c < numOfCols; c++)
            {
                padding = widths[c] - table[r][c].length();
                switch(justify)
                {
                    case LEFT: out += table[r][c] + spaces(padding); break;
                    case CENTRE: out += spaces(padding/2) + table[r][c] +
                                               spaces((padding+1)/2); break;
                    case RIGHT: out+= spaces(padding) + table[r][c];
                }
                out += " ";
            }
            out += "\n";
        }
        return out;
    }

    private static String spaces(int n)
    {
        char[] out = new char[n];
        Arrays.fill(out, ' ');
        return String.valueOf(out);
    }

    private static String get(String[][] table, int row, int col)
    {
        return (col < table[row].length) ? table[row][col] : "";
    }
    public static RuntimeException impossible()
    {
        return new RuntimeException("Internal error: this shouldn't happen");
    }
    
    public static Integer[] int2Integer(int[] in)
    {
        Integer[] out = new Integer[in.length];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = in[i];
        }
        return out;
    }

    public static int[] integer2Int(Integer[] in)
    {
        int[] out = new int[in.length];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = in[i];
        }
        return out;
    }
    
    public static List<Integer> asList(int[] ar)
    {
        List<Integer> list = new ArrayList<Integer>(ar.length);
        for(int a : ar)
            list.add(a);
        return list;
    }
    
    public static void log(Object obj)
    {
        LogInfo.logs(obj);
    }

    public static void logs(String format, Object... args)
    {
        LogInfo.logs("%s", fmts(format, args));
    }

    public static void logss(String format, Object... args)
    {
        LogInfo.logss("%s", fmts(format, args));
    }

//    public static <A extends Object> A[] map(int n, A f)
//    {
//        final A[] result = (A[]) new Object[n];
//        for(int i = 0; i < n; i++)
//        {
//            result[i] =
//        }
//    }

    public static <A extends Object> String mkString(A[] array, String delimiter)
    {
        String out = "";
        for(Object el : array)
        {
            out += el.toString() + delimiter;
        }
        return out.length() > 0 ?
            out.substring(0, out.length() - delimiter.length()) :
            " ";
    }

    public static <A extends MyCallable, B>
            void parallelForeach(int numOfThreads, Collection<A> actions)
    {
        if(actions.size() == 1)
        {
            try
            {
                A action = actions.iterator().next();
                action.setLog(true);
                action.call();
            }
            catch (Exception ex)
            {
                LogInfo.logs("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        else
        {
            ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
            Exception ex = null;
            Thread primaryThread = null;

            for(A action : actions)
            {
                if(!Execution.shouldBail())
                {
                    try
                    {
                        if(ex == null)
                        {
                            synchronized(executor)
                            {
                                if (primaryThread == null)
                                {
                                    primaryThread = Thread.currentThread();
                                }
                            }
                            action.setLog(primaryThread == Thread.currentThread());
                            executor.submit(action);
                        } // if - extends == null
                    } // try
                    catch(Exception e)
                    { 
                        ex = e;
                        LogInfo.logs(e.getMessage());
                        e.printStackTrace();
                    }
                } // if !Execution.shouldBail()
            } // for              
            executor.shutdown();
            try
            {
                while (!executor.awaitTermination(1, TimeUnit.SECONDS)) { }
            }
            catch(InterruptedException ie)
            {
                LogInfo.logs("Interrupted");
            }
        }
    }

    public static <A extends MyCallable, B>
            List<B> parallelForeachWithResults(int numOfThreads, Collection<A> actions)
    {        
        List<B> results = Collections.synchronizedList(new ArrayList<B>());
        if(actions.size() == 1)
        {
            try
            {
                A action = actions.iterator().next();
                action.setLog(true);
                action.call();
            }
            catch (Exception ex)
            {
                LogInfo.logs("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        else
        {
            ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
            Exception ex = null;
            Thread primaryThread = null;

            for(A action : actions)
            {
                if(!Execution.shouldBail())
                {
                    try
                    {
                        if(ex == null)
                        {
                            synchronized(executor)
                            {
                                if (primaryThread == null)
                                {
                                    primaryThread = Thread.currentThread();
                                }
                            }
                            action.setLog(primaryThread == Thread.currentThread());
                            results.add((B) executor.submit(action).get());
                        } // if - extends == null
                    } // try
                    catch(Exception e)
                    {
                        ex = e;
                        e.printStackTrace();
                    }
                } // if !Execution.shouldBail()
            } // for
            executor.shutdown();
            try
            {
                while (!executor.awaitTermination(1, TimeUnit.SECONDS)) { }
            }
            catch(InterruptedException ie)
            {
                LogInfo.logs("Interrupted");
            }
        }
        return results;
    }

    
    public static String[] readLines(String path, int maxLines)
    {
        ArrayList<String> linesList = null;
        final BufferedReader in = IOUtils.openInEasy(path);
        if(in != null)
        {
            linesList = new ArrayList<String>();
            String line = "";
            int i = 0;
            try
            {
                while( ((line = in.readLine()) != null) && i < maxLines )
                {
                    linesList.add(line);
                    i++;
                }
                in.close();
            }
            catch(IOException ioe)
            {
                LogInfo.logs("Error reading file %s", path);
            }
        }
        String[] out = new String[linesList.size()];
        return linesList.toArray(out);
    }
   
    public static String[] readLines(String path)
    {
        return readLines(path, Integer.MAX_VALUE);
    }

    public static String readFileAsString(String filePath) throws java.io.IOException
    {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try
        {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) try { f.close(); } catch (IOException ignored) { }
        }
        return new String(buffer);
    }

    public static String stripExtension(String name)
    {
        int index = name.lastIndexOf(".");
        return index == -1 ? name : name.substring(0, index);
    }
    
    // Detect overflow, then set to cap (MAX_VALUE or MIN_VALUE)
    public static int safeAdd(int x, int y)
    {
        if (x > 0 && y > 0)
        {
            int z = x + y;
            if (z > 0)
            {
                return z;
            }
            else
            {
                return Integer.MAX_VALUE;
            }
        }
        else if (x < 0 && y < 0)
        {
            int z = x + y;
            if (z < 0)
            {
                return z;
            }
            else
            {
                return Integer.MIN_VALUE;
            }
        }
        else
        { // No chance of overflow if both have different sign
            return x + y;
        }
    }

    public static <A> A same(A x, A y)
    {
        if (x != y)
        {
            throw new IllegalArgumentException(fmts("Different: %s %s",
                    x.toString(), y.toString()));
        }
        return x;
    }

    public static double[] set(double[] a, double b)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] = b;
        }
        return a;
    }

    public static double[] set(double[] a, Random random, double noise, Constants.TypeAdd type)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] = (type == TypeAdd.RANDOM) ? Math.pow(1 + random.nextDouble(), noise) :
                                             random.nextDouble() * noise;
        }
        return a;
    }

    public static int[] set(int[] a, int b)
    {
        for(int i = 0; i < a.length; i++)
        {
            a[i] = b;
        }
        return a;
    }

    public static String[] sortWithEmbeddedInt(String[] a)
    {
        StringWithEmbeddedInt[] swei = new StringWithEmbeddedInt[a.length];
        for(int i = 0; i < a.length; i++)
        {
            swei[i] = new StringWithEmbeddedInt(a[i]);
        }
        Arrays.sort(swei);
        String[] out = new String[swei.length];
        for(int i = 0; i < swei.length; i++)
        {
            out[i] = swei[i].getValue();
        }
        return out;
    }

    public static double sum(double[] a)
    {
        double result = 0.0;
        for(int i = 0; i < a.length; i++)
        {
            result += a[i];
        }
        return result;
    }

    // Assume the array is already sorted, just like the Unix command
    public static <A> A[] uniq(A[] a)
    {
        ArrayList<A> list = new ArrayList();
        for(int i = 0; i < a.length; i++)
        {
            if(i == 0 || !a[i].equals(a[i-1]))
                list.add(a[i]);
        }
        return (A[]) list.toArray();
    }

    public static boolean writeLines(String path, String[] lines)
    {
        PrintWriter out = IOUtils.openOutEasy(path);
        if(out != null)
        {
            for(String line: lines)
            {
                out.println(line);
            }
            out.close();
            return true;
        }
        return false;
    }

    public static boolean write(String path, String text)
    {
        PrintWriter out = IOUtils.openOutEasy(path);
        if(out != null)
        {
            out.println(text);
            out.close();
            return true;
        }
        return false;
    }

    /**
     * computes word error rate of current hypothesis against transcription
     * @param lineTrans the current hypothesis
     * @param trueTrans the transciption
     * @return word error rate for current hypothesis against transcription
     */
    public static float computeWER(String lineTrans, String trueTrans)
    {        
        return computeWER(lineTrans.toUpperCase().split("\\p{Space}"), trueTrans.toUpperCase().split("\\p{Space}"));
    }

    public static float computeWER(Object[] transArray, Object[] trueArray)
    {        
        int ld = 0;                
        if(transArray.length == 0)
        {
            transArray = new Object[1];
        }       
        // compute levenshtein distance
        ld = getLevenshteinDistance(trueArray, transArray);

        return (float) ld / (float) trueArray.length;
    }
    
    public static int getLevenshteinDistance (Object[] s, Object[] t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
        The difference between this impl. and the previous is that, rather
         than creating and retaining a matrix of size s.length()+1 by t.length()+1,
         we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
         is the 'current working' distance array that maintains the newest distance cost
         counts as we iterate through the characters of String s.  Each time we increment
         the index of String t we are comparing, d is copied to p, the second int[].  Doing so
         allows us to retain the previous cost counts as required by the algorithm (taking
         the minimum of the cost count to the left, up one, and diagonally up and to the left
         of the current cost count being calculated).  (Note that the arrays aren't really
         copied anymore, just switched...this is clearly much better than cloning an array
         or doing a System.arraycopy() each time  through the outer loop.)

         Effectively, the difference between the two implementations is this one does not
         cause an out of memory condition when calculating the LD over two very large strings.
        */

        int n = s.length; // length of s
        int m = t.length; // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n+1]; //'previous' cost array, horizontally
        int d[] = new int[n+1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        Object t_j; // jth character of t

        int cost; // cost

        for (i = 0; i<=n; i++) {
             p[i] = i;
        }

        for (j = 1; j<=m; j++) {
             t_j = t[j-1];
             d[0] = j;

             for (i=1; i<=n; i++) {
                cost = s[i-1].equals(t_j) ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
             }

             // copy current distance counts to 'previous row' distance counts
             _d = p;
             p = d;
             d = _d;
        }

            // our last action in the above loop was to switch d and p, so p now
            // actually has the most recent cost counts
            return p[n];
    }
    
    public static List<Tree<String>> loadTrees(String path,  
                                               boolean removePunctuation) throws IOException
    {
        return loadTrees(path, Integer.MAX_VALUE, removePunctuation);
    }
    
    public static List<Tree<String>> loadTrees(String path, int maxTrees, 
                                               boolean removePunctuation) throws IOException
    {
        List trees = new ArrayList<Tree<String>>();
        StandardTreeNormalizer treeTransformer = new Trees.StandardTreeNormalizer();
        BufferedReader in = IOUtils.openIn(path);
        PennTreeReader treeIterator = new Trees.PennTreeReader(in);        
        for(int n = 0; n < maxTrees && treeIterator.hasNext(); n++)
        {
            Tree<String> tree = null;
            try
            {
                tree = treeTransformer.transformTree(treeIterator.next());
                if(removePunctuation)
                    tree = TreeUtils.removePunctuation(tree);
                trees.add(tree);
            }
            catch(Exception e)
            {
                LogInfo.error("Error loading tree " + n);
                e.printStackTrace();
            }
        }
        in.close();
        return trees;
    }
    
    public static String stripTag(String word, String tagDelimiter)
    {
        return word.equals("</s>") || !word.contains(tagDelimiter) ? word : word.substring(0, word.lastIndexOf(tagDelimiter));
    }

    public static String[] stripTags(String[] words, String tagDelimiter)
    {
        String[] out = new String[words.length];
        for(int i = 0; i < out.length; i++)
            out[i] = stripTag(words[i], tagDelimiter);
        return out;
    }

    public static String stripTags(String sentence, String tagDelimiter)
    {
        StringBuilder str = new StringBuilder();
        for(String s : stripTags(sentence.split(" "), tagDelimiter))
        {
            str.append(s).append(" ");
        }
        return str.toString().trim();
    }
    
    public static String stripWord(String word, boolean strict, String tagDelimiter)
    {
        if(strict)            
            return word.equals("</s>") || !word.contains(tagDelimiter) ? null : word.substring(word.lastIndexOf(tagDelimiter) + 1);
        else
            return !word.contains(tagDelimiter) || word.equals("</s>") ? word : word.substring(word.lastIndexOf(tagDelimiter) + 1);
    }
    
    public static String[] stripWords(String[] words, String tagDelimiter)
    {
        String[] out = new String[words.length];
        for(int i = 0; i < out.length; i++)
            out[i] = stripWord(words[i], false, tagDelimiter);
        return out;
    }

    static int argmax(double[] weights)
    {
        if(weights == null)
            return -1;
        if(weights.length == 1)
            return 0;
        double max = weights[0];
        int maxI = 0;
        for(int i = 1; i < weights.length; i++)
            if(weights[i] > max)
                maxI = i;
        return maxI;
    }
    
    /**
     * Replace occurences of numbers in the input into the <num> tag
     * @param input
     * @return 
     */
    public static String replaceNumbers(String input)
    {
        StringBuilder str = new StringBuilder();
        input = input.replaceAll("\n", "\n ");
        for(String token : input.split(" "))
        {            
            str.append(replaceNumber(token, false, "")).append(" ");
        } // for
        return str.toString();
    }
    
    /**
     * Replaces the input string with the <num> tag if it is a number
     * @param input
     * @return 
     */
    public static String replaceNumber(String input, boolean posAtSurfaceLevel, String tagDelimiter)
    {
        if(posAtSurfaceLevel)
            input = stripTag(input, tagDelimiter);
        return input.matches("-\\p{Digit}+|" + // negative numbers
                                 "-?\\p{Digit}+\\.\\p{Digit}+|" + // decimals
                                 "\\p{Digit}+[^(am|pm)]|\\p{Digit}+") // numbers, but not hours!
                                 ? "<num>" : input;
    }
    
    public static String deTokenize(String input)
    {
        boolean capitalise = false;
        StringBuilder str = new StringBuilder();
        String[] ar = input.split(" ");
        // capitalise first word
        str.append(capitalise(ar[0])).append(" ");
        for(int i = 1; i < ar.length; i++)
        {
            String current = ar[i];
            // remove space before delimeter
            if(current.equals(",") || current.equals("?"))
            {
                str.deleteCharAt(str.length() - 1).append(current);
//                str.append(current);
            }
            // capitalise next word and delete space before a fullstop
            else if(current.equals("."))
            {
                str.deleteCharAt(str.length() - 1).append(current);
                capitalise = true;
            }
            else if(capitalise)
            {
                str.append(capitalise(current));
                capitalise = false;
            }
            else
                str.append(current);
            str.append(" ");
        }
        return str.toString().trim();
    }
    
    public static String[] tokenize(String input)
    {
        return input.toLowerCase().split("\\s");
    }
    
    public static String[] removeStopWords(String[] tokens, Set<String> stopWords)
    {
        List<String> res = new ArrayList<String>();
        for(String token : tokens)
        {
            if(!stopWords.contains(token))
                res.add(token);
        }
        return res.toArray(new String[0]);
    }
    
    public static String tokensToString(String[] tokens)
    {
        if(tokens.length == 0)
            return "";
        StringBuilder str = new StringBuilder(tokens[0]);              
        for(int i = 1; i < tokens.length; i++)
        {
            str.append(" ").append(tokens[i]);
        }
        return str.toString();
    }
    
    public static String applyDictionary(String input, Properties dictionary)
    {
        if (dictionary.isEmpty())
            return input;
        StringBuilder str = new StringBuilder();
        for(String token : input.split(" "))
        {
            str.append(dictionary.containsKey(token) ? dictionary.getProperty(token) : token).append(" ");
                
        }
        return str.toString().trim();
    }
    
    public static String capitalise(String input)
    {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    public static List[] partitionList(List list, int folds)
    {
        List[] partitions = new List[folds];
        int partitionSize = list.size() / folds, index;
        for(int i = 0; i < folds; i++)
        {
            index = i * partitionSize;
            partitions[i] = list.subList(index,
                    i < folds - 1 ? index + partitionSize : list.size());
        } // for        
        return partitions;
    }
    
    public static void writePartitions(List[] partitions, int folds, String destPath, String prefixName)
    {
        for(int i = 0; i < folds; i++)
        {
            String trainFile = String.format("%s/%sFold%dTrain", destPath, prefixName, i+1);
            List<List> subs = new ArrayList<List>(folds-1);
            for(int j = 0; j < folds; j++)
            {
                if(i !=j)
                    subs.add(partitions[j]);
            }
            writePaths(trainFile, subs);
            String evalFile = String.format("%s/%sFold%dEval", destPath, prefixName, i+1);
            subs.clear();subs.add(partitions[i]);
            writePaths(evalFile, subs);
        }
    }
    
    public static void writePaths(String filename, List<List> lists)
    {
        try
        {
            PrintWriter out = IOUtils.openOut(filename);
            for(List list : lists)
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
    
    public static String[] tokenizeToUnigrams(String sentence, Set<String> stopWords)
    {
        return stopWords == null ? Utils.tokenize(sentence) : Utils.removeStopWords(Utils.tokenize(sentence), stopWords);
    }
    
    public static String[] tokenizeToBigrams(String sentence, Set<String> stopWords)
    {
        List<String> bigrams = new ArrayList<String>();
        String[] unigrams = Utils.tokenize("<s> " + sentence.replaceAll(", ", "") + " </s>");
        for(int i = 1; i < unigrams.length; i++)
        {
            bigrams.add(unigrams[i-1] + "_" + unigrams[i]);
        }
        return bigrams.toArray(new String[0]);
    }
    
    public static RealVector[] extractFeatures(List<String[]> docs, Indexer<String> terms, boolean tfIdf)
    {
        RealVector[] vectors = new RealVector[docs.size()];        
        RealVector df = tfIdf ? new ArrayRealVector(terms.size()) : null;
        int i = 0;        
        for(String[] doc: docs)
        {
            RealVector v = new OpenMapRealVector(terms.size());            
            Set<Integer> uniqTokensPos = tfIdf ? new HashSet<Integer>() : null;
            for(String token : doc)
            {                
                int pos = terms.getIndex(token);
                v.setEntry(pos, v.getEntry(pos) + 1); // compute term count
                if(tfIdf)
                    uniqTokensPos.add(pos);
            } // for
            if(tfIdf)
            {
                v.mapDivideToSelf(doc.length); // compute tf of the vector
                for(int pos : uniqTokensPos) // compute df for each unique term in the document
                    df.setEntry(pos, df.getEntry(pos) + 1);
            } 
            vectors[i++] = v;
        }
        if(tfIdf)
        {
            try
            {
                // compute idf
//                df.mapToSelf(ComposableFunction.LOG).mapPowToSelf(-1).mapMultiplyToSelf(docs.size());
                df.mapPowToSelf(-1).mapMultiplyToSelf(docs.size()).mapToSelf(ComposableFunction.LOG10);
                // compute tf * idf
                for(RealVector v : vectors)
                {
                    Iterator<RealVector.Entry> it = v.sparseIterator();
                    while(it.hasNext())
                    {
                        RealVector.Entry entry = it.next();
                        entry.setValue(entry.getValue() * df.getEntry(entry.getIndex()));
                    } // while
                } // for
            } 
            catch (FunctionEvaluationException ex)
            {
                System.err.println(ex);
            }            
        }
            
        return vectors;
    }
    
    public static double cosine(RealVector vec1, RealVector vec2) throws Exception
    {
        final double norm1 = vec1.getNorm();
        final double norm2 = vec2.getNorm();

        if (norm1 == 0 || norm2 == 0) 
            throw new Exception("Division by zero");
        return vec1.dotProduct(vec2) / (norm1 * norm2);
    }
    
    public static boolean isSentencePunctuation(String s)
    {
        return s.equals("./.") || s.equals("--/:") || s.equals(".") ||  s.equals("--");
    }
    
    public static boolean isPunctuation(String s)
    {
        return s.equals("./.") || s.equals(",/,") || s.equals("--/:") ||
               s.equals("-LRB-/-LRB-") || s.equals("-RRB-/-RRB-") ||               
               s.equals(".") || s.equals(",") || s.equals("--") ||
               s.equals("(") || s.equals(")");
    }
    
    public static boolean countableRule(Tree<String> tree)
    {
        return !(tree.isLeaf() || tree.isPreTerminal());
    }
    
    /**
     * Input String has the following format:
     * Example_xxx (name) \n text (optional) \n events \n record_tree (optional) \n align (optional)
     * @param input
     * @return an array of Strings with name, text, events and align data in
     * each position
     */
    public static String[] extractExampleFromString(String input)
    {
        String[] res = new String[5];
        String ar[] = input.split("\n");
        StringBuilder str = new StringBuilder();
        if(ar[0].equals("$NAME")) // event3 v.2 format
        {
            res[0] = ar[1]; // name
            // parse text
            int i = 3; // 2nd line is the $TEXT tag
            while(i< ar.length && !ar[i].equals("$EVENTS")) 
            {
                str.append(ar[i++]).append("\n");                
            }            
            res[1] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            str = new StringBuilder();
            i++; // move past $EVENTS tag
            while(i< ar.length && !(ar[i].equals("$ALIGN") || ar[i].equals("$RECORD_TREE"))) 
            {
                str.append(ar[i++]).append("\n");                
            }
            if(ar.length > 4) // if the example contains events input
                res[2] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            if(i< ar.length && ar[i].equals("$RECORD_TREE"))
            {
                i++; // move past $RECORD_TREE tag
                str = new StringBuilder();
                while(i< ar.length && !ar[i].equals("$ALIGN")) 
                {
                    str.append(ar[i++]).append("\n");                
                }            
                res[4] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
            if(i < ar.length) // didn't reach the end of input, so there is align data
            {
                i++; // move past $ALIGN tag
                str = new StringBuilder();
                while(i< ar.length) 
                {
                    str.append(ar[i++]).append("\n");                
                }            
                res[3] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
        }
        else // event3 v.1 format
        {
            res[0] = ar[0]; // name
            if(!ar[1].startsWith(".id")) // text was found
                res[1] = ar[1];
            int i;            
            for(i = res[1] == null ? 1 : 2; i < ar.length; i++)
            {
                if(ar[i].startsWith(".id")) // event line
                    str.append(ar[i]).append("\n");
                else
                    break;
            } // for
            res[2] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            if(i < ar.length) // didn't reach the end of input, so there is align data
            {
                str = new StringBuilder();
                for(int j = i; j < ar.length; j++)
                    str.append(ar[j]).append("\n");
                res[3] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
        }                
        return res;
    }
    
    public static List<Event3Example> readEvent3Examples(String inputPath, boolean examplesInSingleFile)
    {
        List<String> list = new ArrayList<String>();
        list.add(inputPath);
        return readEvent3Examples(list, list, examplesInSingleFile);
    }
    
    public static List<Event3Example> readEvent3Examples(List<String> inputPaths, 
            List<String> inputLists, boolean examplesInSingleFile)
    {
        List<Event3Example> examples = new ArrayList<Event3Example>();
        try 
        {
            Event3Example example;
            if(examplesInSingleFile)
            {
                String key = null;
                StringBuilder str = new StringBuilder();
                for(String line : Utils.readLines(inputLists.get(0)))
                {
                    if(line.startsWith("Example_") || line.equals("$NAME"))
                    {
                        if(key != null) // only for the first example
                        {
                            example = new Event3Example(extractExampleFromString(str.toString()));
                            examples.add(example);
                            str = new StringBuilder();
                        }
                        key = line;
                    } // if
                    str.append(line).append("\n");
                }  // for
                // don't forget last example
                example = new Event3Example(extractExampleFromString(str.toString()));
                examples.add(example);
            } // if
            else
            {
                for(String line : Utils.readLines(inputPaths.get(0))) // contains list of .events files
                {
    //                    System.out.println(line);
                    String events = Utils.readFileAsString(line);
                    String key = Utils.stripExtension(line);
                    String text = Utils.readFileAsString(key+".text");
                    String align = null;
                    if(new File(key+".align").exists())
                    {
                        align = Utils.readFileAsString(key+".align");
                        String[] ex = {events, text, align};
                        examples.add(new Event3Example(ex));
                    }                                            
                } // for
            }            
        }
        catch(IOException ioe) {
            LogInfo.error(ioe);
        }
        return examples;
    }
}
