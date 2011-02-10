package induction.problem;

import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.Constants.TypeAdd;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
    static final long serialVersionUID = -8920104157808512229L;
    protected List<ProbVec> vecs;
    protected Map<String, ProbVec> vecsMap;

    public AParams()
    {
        vecs = new ArrayList();
        vecsMap = new HashMap<String, ProbVec>();
    }

    public void setUniform(double x)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.set(x);
        }
    }

    public void randomise(Random random, double noise)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.set(random, noise, TypeAdd.RANDOM);
        }
    }

    public void addNoise(Random random, double noise)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.set(random, noise, TypeAdd.NOISE);
        }
    }

    public void optimiseIfTooBig(ProbVec[] parameters)
    {
        for(ProbVec v: parameters)
        {
            v.normalizeIfTooBig();
        }
    }

    public void optimise(double smoothing)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.addCount(smoothing).normalise();
        }
    }

    public void optimiseVar(double smoothing)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.addCount(smoothing).expDigamma();
        }
    }

    public void saveSum()
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.saveSum();
        }
    }

    public void div(double scale)
    {
//        for(ProbVec v: vecs)
        for(ProbVec v: vecsMap.values())
        {
            v.div(scale);
        }
    }

    public void add(double scale, AParams that)
    {
//        final List<ProbVec> thatVecs = that.vecs;
//        for(int i = 0; i < vecs.size(); i++)
//        {
//            vecs.get(i).addCount(thatVecs.get(i), scale);
//        }
        final Map<String, ProbVec> thatVecsMap = that.vecsMap;
        for(Entry<String, ProbVec> entry: vecsMap.entrySet())
        {
            entry.getValue().addCount(thatVecsMap.get(entry.getKey()), scale);
        }
    }

    protected void addVec(String key, ProbVec vec)
    {
        vecsMap.put(key, vec);
    }

    protected void addVec(ProbVec vec)
    {
        vecs.add(vec);
    }

    protected void addVec(String[] keys, ProbVec[] vec)
    {
        for(int i = 0; i < keys.length; i++)
        {
            vecsMap.put(keys[i], vec[i]);
        }
    }

    protected void addVec(ProbVec[] vec)
    {
        vecs.addAll(Arrays.asList(vec));
    }

    protected void addVec(Map<String, ProbVec> vecsMap)
    {
        vecsMap.putAll(vecsMap);
    }

    protected void addVec(List<ProbVec> vec)
    {
        vecs.addAll(vec);
    }

    public Map<String, ProbVec> getVecs()
    {
        return vecsMap;
    }

//    public List<ProbVec> getVecs()
//    {
//        return vecs;
//    }

    public void setVecs(Map<String, ProbVec> vecsMap)
    {
        ProbVec vIn, v;
        for(Entry<String, ProbVec> entry: vecsMap.entrySet())
        {
            vIn = entry.getValue();
            v = this.vecsMap.get(entry.getKey());
            v.setData(vIn.getCounts(), vIn.getSum(),
                                     vIn.getOldSum(), vIn.getLabels());
            v.setSortedIndices();
        }
        for(int i = 0; i < vecs.size(); i++)
        {
            vIn = vecs.get(i);
            this.vecs.get(i).setData(vIn.getCounts(), vIn.getSum(),
                                     vIn.getOldSum(), vIn.getLabels());
            this.vecs.get(i).setSortedIndices();
        }        
    }

    public void setVecs(List<ProbVec> vecs)
    {
        ProbVec v;
        for(int i = 0; i < vecs.size(); i++)
        {
            v = vecs.get(i);
            this.vecs.get(i).setData(v.getCounts(), v.getSum(),
                                     v.getOldSum(), v.getLabels());
            this.vecs.get(i).setSortedIndices();
        }
    }
    
    public abstract String output();

    public void output(String path)
    {
        Utils.begin_track("AParams.output(%s)", path);
        Utils.write(path, output());
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

    // Helper for output(): display probabilities in sorted order
    public String forEachProb(ProbVec v)
    {
        String out = "";
        for(Pair p : v.getProbsSorted())
        {
            out += p.label + "\t" + Utils.fmt(p.value) + "\t" +
                    Utils.fmt(v.getOldSum() * p.value) + "\n";
        }
        return out;
    }

    public String forEachProb(ProbVec v, String[] labels)
    {
        String out = "";
        for(Pair<Integer> p : v.getProbsSorted())
        {
            out += labels[p.label] + "\t" + Utils.fmt(p.value) + "\t" +
                    Utils.fmt(v.getOldSum() * p.value) + "\n";
        }
        return out;
    }
}
