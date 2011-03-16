package induction;

import fig.basic.IOUtils;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.problem.event3.Constants;
import induction.problem.event3.Constants.TypeAdd;
import induction.utils.StringWithEmbeddedInt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            return String.format("%.3f", x);
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
            ArrayList<B> parallelForeachWithResults(int numOfThreads, Collection<A> actions)
    {
        ArrayList<B> results = new ArrayList<B>();
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

    public static void main(String[] args)
    {
        boolean bAr[] = {false, false, false, true, false, false, false, true};
        System.out.println("");

    }
    public static String[] readLines(String path)
    {
        return readLines(path, Integer.MAX_VALUE);
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
        int ld = 0;

        StringTokenizer transTok = new StringTokenizer(lineTrans.toUpperCase());
        String[] transArray = new String[transTok.countTokens()];
        for(int i = 0; i < transArray.length; i++)
        {
            transArray[i] = transTok.nextToken();
        }

        StringTokenizer trueTok = new StringTokenizer(trueTrans.toUpperCase());
        String[] trueArray = new String[trueTok.countTokens()];
        for(int i = 0; i < trueArray.length; i++)
        {
            trueArray[i] = trueTok.nextToken();
        }

//        if(trueTrans.equals(Constants.TRANS_SIL) && !lineTrans.equals(""))
//        {
//            return 1.0f;
//        }
//        if(trueTrans.equals(Constants.TRANS_SIL))
//        {
//            trueArray = new String[1];
//            trueArray[0] = " ";
//        }
        if(transArray.length == 0)
        {
            transArray = new String[1];
            transArray[0] = " ";
        }

        // compute levenshtein distance
        ld = levenshteinDistance(trueArray, transArray);

        return (float) ld / (float) trueArray.length;
    }

    /**
     * computes the levenshtein distance (number of insertions, deletions,
     * alterations) between two strings
     * @param s the first string to be compared
     * @param t the second string to be compared
     * @return the levenshtein distance
     */
    private static int levenshteinDistance(String[] s, String[] t)
    {
        int m = s.length, n = t.length, cost = 0;
        int d[][] = new int[m][t.length];

        for (int i = 0; i < m; i++)
        {
            d[i][0] = i;
        }
        for (int j = 0; j < n; j++)
        {
            d[0][j] = j;
        }
        for (int i = 1; i < m; i++)
        {
            for (int j = 1; j < n; j++)
            {
                if (s[i].equals(t[j]))
                {
                    cost = 0;
                }
                else
                {
                   cost = 1;
                }
                d[i][j] = minimum(   d[i - 1][j] + 1,     // deletion
                                     d[i][j - 1] + 1,     // insertion
                                     d[i - 1][j - 1] + cost   // substitution
                                 );
            }
        }
        return d[s.length - 1][t.length - 1];
    }

    /**
     * returns the minimum among three integers
     * @param a first integer
     * @param a second integer
     * @param a third integer
     * @return the minimum integer
     */
    private static int minimum (int a, int b, int c)
    {
        int min;

        min = a;
        if (b < min)
        {
          min = b;
        }
        if (c < min)
        {
          min = c;
        }
        return min;
    }
}
