package induction.problem.event3.discriminative.optimizer;

import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.discriminative.Feature;
import java.util.HashMap;
import java.util.Map;

/*This class implements common functions:
 * (1) gradient computation
 * (2) batch update
 * (3) cooling schedule
 * (4) regularization
 * */
public abstract class GradientBasedOptimizer
{

    // update the perceptron after processing BATCH_SIZE
    private final int BATCH_UPDATE_SIZE; 
    // number of training examples
    private final int TRAIN_SIZE;
    // assume the model will converge after pass CONVERGE_PASS
    private final int CONVERGE_PASS;
    // where parameter t was adjusted such that the gain is halved after 
    // one pass through the data (610k*2/30)
    private final double COOLING_SCHEDULE_T;
    private final double INITIAL_GAIN;
    // the smaller SIGMA, the sharp the prior is; the more regularized of the model 
    // (meaning more feature weights goes close to zero)
    private final double SIGMA;
    private double REG_CONSTANT_RATIO = 0;
    private final boolean IS_MINIMIZE_SCORE;
    
    // how many times the model is changed
    protected int numModelChanges = 0;
    // default is with regularization; (perceptron does not use this)
    private boolean noRegularization = false;
    // default is with cooling
    protected boolean noCooling = false;

    public void setNoRegularization()
    {
        noRegularization = true;
    }

    public void setNoCooling()
    {
        noCooling = true;
    }

    /*
     * Default constructor with reasonable initial parameters. TODO optimise
     */
    public GradientBasedOptimizer(int trainSize, int batchUpdateSize)
    {
        this(trainSize, batchUpdateSize, 1, 0.1, 1.0, trainSize * 1 / batchUpdateSize, false);
    }
    
    public GradientBasedOptimizer(int trainSize, int batchUpdateSize, int convergePass, 
                                  double initGain)
    {
        this(trainSize, batchUpdateSize, convergePass, initGain, 1.0, 
             trainSize * convergePass * 1.0 / batchUpdateSize, false);
    }
    
    public GradientBasedOptimizer(int trainSize, int batchUpdateSize, int convergePass, double coolingSchedule,
                                  double initGain)
    {
        this(trainSize, batchUpdateSize, convergePass, initGain, 1.0, coolingSchedule, false);
    }

    public GradientBasedOptimizer(int trainSize, int batchUpdateSize, 
                                  int convergePass, double initGain, 
                                  double sigma, double coolingSchedule, 
                                  boolean isMinimizeScore)
    {
        TRAIN_SIZE = trainSize;
        BATCH_UPDATE_SIZE = batchUpdateSize;
        CONVERGE_PASS = convergePass;
        INITIAL_GAIN = initGain;
        COOLING_SCHEDULE_T = coolingSchedule;

        SIGMA = sigma;
        REG_CONSTANT_RATIO = BATCH_UPDATE_SIZE * 1.0 / (TRAIN_SIZE * SIGMA * SIGMA);

        IS_MINIMIZE_SCORE = isMinimizeScore;
        LogInfo.logsForce("TRAIN_SIZE: " + TRAIN_SIZE + "\n" + 
                   "BATCH_UPDATE_SIZE: " + BATCH_UPDATE_SIZE + "\n" + 
                   "CONVERGE_PASS: " + CONVERGE_PASS + "\n" + 
                   "INITIAL_GAIN: " + INITIAL_GAIN + "\n" + 
                   "COOLING_SCHEDULE_T: " + COOLING_SCHEDULE_T + "\n" + 
                   "SIGMA: " + SIGMA + "\n" + 
                   "REG_CONSTANT_RATIO: " + REG_CONSTANT_RATIO + "\n" + 
                   "IS_MINIMIZE_SCORE: " + IS_MINIMIZE_SCORE);
    }

    public abstract void initModel(double minValue, double maxValue);// random start

    public abstract void updateModel(HashMap<Feature, Double> oracleFeatures, 
                                     HashMap<Feature, Double> modelFeatures);

    public abstract HashMap getAvgModel();

    public abstract HashMap getSumModel();

    public abstract void setFeatureWeight(Feature feat, double weight);

    public abstract double getGradientNorm();
    
    public int getBatchSize()
    {
        return BATCH_UPDATE_SIZE;
    }

    protected HashMap<Feature, Double> getGradient(HashMap<Feature, Double> oracleFeatures, 
              HashMap<Feature, Double> modelFeatures)
    {
        HashMap<Feature, Double> res = new HashMap<Feature, Double>();
        // process tbl_feats_oracle
        for (Map.Entry<Feature, Double> entry : oracleFeatures.entrySet())
        {
            Feature key = entry.getKey();
            double gradient = entry.getValue();
            Double v_1best = modelFeatures.get(key);
            if (v_1best != null)
            {
                gradient -= v_1best; // v_oracle - v_1best
            }
            if (gradient != 0)//TODO
            {
                if (IS_MINIMIZE_SCORE)
                {
                    res.put(key, -gradient); // note: we are minimizing the cost
                }
                else
                {
                    res.put(key, gradient); // note: we are max the prob
                }
            }
        }
        // process tbl_feats_1best
        for (Map.Entry<Feature, Double> entry : modelFeatures.entrySet())
        {
            Feature key = entry.getKey();
            Double v_oracle = oracleFeatures.get(key);
            if (v_oracle == null) // this feat only activate in the 1best, not in oracle
            {
                if (IS_MINIMIZE_SCORE)
                {
                    res.put(key, entry.getValue()); // note: we are minizing the cost
                }
                else
                {
                    res.put(key, -entry.getValue()); // note: we are maximize the prob
                }
            }
        }
        // System.out.println("gradient size is: " + res.size());
        return res;
    }

    protected double computeGain(int iterNumber)
    {
        // the numbers of updating the model
        if (noCooling)
        {
            return 1.0;
        }
        else
        {
//            return INITIAL_GAIN * COOLING_SCHEDULE_T / (COOLING_SCHEDULE_T + iterNumber);
            return 1.0 / Math.pow(iterNumber + 2, COOLING_SCHEDULE_T);
        }
    }

    protected double computeRegularizationScale(double updateGain)
    {
        if (noRegularization)
        {
            return 1.0;
        }
        else
        {
            return 1.0 - REG_CONSTANT_RATIO * updateGain;
        }
    }
}
