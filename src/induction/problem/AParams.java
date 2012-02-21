package induction.problem;

import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.Constants.TypeAdd;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author konstas
 */
public abstract class AParams implements Serializable
{
    public static enum ParamsType {PROBS, COUNTS};
    static final long serialVersionUID = -8920104157808512229L;
    protected Map<String, Vec> vecsMap;
    protected AModel model;
    
    public AParams(AModel model)
    {
        this.model = model;
        vecsMap = new HashMap<String, Vec>();
    }   
    
    public void setUniform(double x)
    {
        for(Vec v: vecsMap.values())
        {
            v.set(x);
        }
    }

    public void randomise(Random random, double noise)
    {
        for(Vec v: vecsMap.values())
        {
            v.set(random, noise, TypeAdd.RANDOM);
        }
    }

    public void addNoise(Random random, double noise)
    {
        for(Vec v: vecsMap.values())
        {
            v.set(random, noise, TypeAdd.NOISE);
        }
    }

    public void optimiseIfTooBig(Vec[] parameters)
    {
        for(Vec v: parameters)
        {
            v.normalizeIfTooBig();
        }
    }

    public void optimise(double smoothing)
    {
        for(Vec v: vecsMap.values())
        {
            v.addCount(smoothing).normalise();
        }
    }

    public void optimiseVar(double smoothing)
    {
        for(Vec v: vecsMap.values())
        {
            v.addCount(smoothing).expDigamma();
        }
    }

    public void saveSum()
    {
        for(Vec v: vecsMap.values())
        {
            v.saveSum();
        }
    }

    public void div(double scale)
    {
        for(Vec v: vecsMap.values())
        {
            v.div(scale);
        }
    }

    public void add(double scale, AParams that)
    {
        final Map<String, Vec> thatVecsMap = that.vecsMap;
        for(Entry<String, Vec> entry: vecsMap.entrySet())
        {
            entry.getValue().addCount(thatVecsMap.get(entry.getKey()), scale);
        }
    }

    protected void addVec(String key, Vec vec)
    {
        vecsMap.put(key, vec);
    }

    protected void addVec(String[] keys, Vec[] vec)
    {
        for(int i = 0; i < keys.length; i++)
        {
            vecsMap.put(keys[i], vec[i]);
        }
    }
    
    protected void addVec(String[][] keys, Vec[][] vec)
    {
        for(int i = 0; i < keys.length; i++)
        {
            for(int j = 0; j < keys[0].length; j++)
                vecsMap.put(keys[i][j], vec[i][j]);
        }
    }

    protected void addVec(Map<String, Vec> vecsMap)
    {
        this.vecsMap.putAll(vecsMap);
    }

    public Map<String, Vec> getVecs()
    {
        return vecsMap;
    }

    public void setVecs(Map<String, Vec> vecsMap)
    {
        Vec vIn, v;
        for(Entry<String, Vec> entry: vecsMap.entrySet())
        {
            vIn = entry.getValue();
            if(this.vecsMap.containsKey(entry.getKey()))
            {
                v = this.vecsMap.get(entry.getKey());
                v.copyDataFrom(vIn);
                v.setProbSortedIndices();
            }            
        }
    }
    
    public abstract String output(ParamsType paramsType);
    public abstract String outputNonZero(ParamsType paramsType);

    public void output(String path, ParamsType paramsType)
    {
        Utils.begin_track("AParams.output(%s)", path);
        Utils.write(path, output(paramsType));
        LogInfo.end_track();
    }
    
    public void outputNonZero(String path, ParamsType paramsType)
    {
        Utils.begin_track("AParams.outputNonZero(%s)", path);
        Utils.write(path, outputNonZero(paramsType));
        LogInfo.end_track();
    }

    public String[] getLabels(int size, String prefix, String[] suffix)
    {
        String out[] = new String[size];
        for(int i = 0; i < size; i++)
        {
            out[i] = prefix + ((suffix != null) ? suffix[i] : "");
        }
        return out;
    }

    public String[][] getLabels(int n1, int n2, String prefix, String[] suffix1, String[] suffix2)
    {
        String out[][] = new String[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            for(int j = 0; j < n2; j++)
            {
                out[i][j] = prefix + ((suffix1 != null && suffix2 != null) ?
                    suffix1[i] + " " + suffix2[j] : "");
            }

        }
        return out;
    }
    
    public String[][][] getLabels(int n1, int n2, int n3, String prefix, String[] suffix1, String[] suffix2, String[] suffix3)
    {
        String out[][][] = new String[n1][n2][n3];
        for(int i = 0; i < n1; i++)
        {
            for(int j = 0; j < n2; j++)
            {
                for(int k = 0; k < n3; k++)
                    out[i][j][k] = prefix + ((suffix1 != null && suffix2 != null && suffix3 != null) ?
                        suffix1[i] + " " + suffix2[j] + " " + suffix3[k] : "");
            }
        }
        return out;
    }
    
    public List<String>[][] getLabels(int n1, int n2, int[] n3, String prefix, String[] suffix1, String[] suffix2, List<String>[] suffix3)
    {
        List<String> out[][] = new List[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            for(int j = 0; j < n2; j++)
            {
                out[i][j] = new ArrayList<String>(n3[i]);
                for(int k = 0; k < n3[i]; k++)
                    out[i][j].add(prefix + ((suffix1 != null && suffix2 != null && suffix3 != null) ?
                        suffix1[i] + " " + suffix2[j] + " " + suffix3[i].get(k) : ""));
            }
        }
        return out;
    }

    
    // Helper for output(): display probabilities in sorted order
    public String forEachProb(Vec v)
    {
        StringBuilder out = new StringBuilder();
        for(Pair p : v.getProbsSorted())
        {
            out.append(p.label).append("\t").append(Utils.fmt(p.value)).append("\n");
        }
        return out.toString();
    }

    public String forEachProb(Vec v, String[] labels)
    {
        StringBuilder out = new StringBuilder();
        for(Pair<Integer> p : v.getProbsSorted())
        {
            out.append(labels[p.label]).append("\t").append(Utils.fmt(p.value)).append("\n");
        }
        return out.toString();
    }
    
    public String forEachCount(Vec v, String[] labels)
    {
        StringBuilder out = new StringBuilder();
        for(Pair<Integer> p : v.getCountsSorted())
        {
            out.append(labels[p.label]).append("\t").append(Utils.fmt(p.value)).append("\n");
        }
        return out.toString();
    }
    
    public String forEachProbNonZero(Vec v, String[] labels)
    {
        StringBuilder out = new StringBuilder();
        for(Pair<Integer> p : v.getProbsSorted())
        {
            if(p.value != 0)
                out.append(labels[p.label]).append("\t").append(Utils.fmt(p.value)).append("\n");
        }
        return out.toString();
    }
    
    public String forEachCountNonZero(Vec v, String[] labels)
    {
        StringBuilder out = new StringBuilder();
        for(Pair<Integer> p : v.getCountsSorted())
        {
            if(p.value != 0)
                out.append(labels[p.label]).append("\t").append(Utils.fmt(p.value)).append("\n");
        }
        return out.toString();
    }
}