package induction.problem.event3.discriminative.optimizer;

import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.discriminative.Feature;
import java.util.HashMap;
import java.util.Iterator;

/*Zhifei Li, <zhifei.work@gmail.com>
 * Johns Hopkins University
 */

/*Cleasses extend this should include
 * (1) process_one_sent: get the reranked 1-best; get the feature counts
 * (2) rerank the hypothesis
 * (3) feature extraction from 1-best and oracle
 * */
public class DefaultPerceptron extends GradientBasedOptimizer
{

    // key: feature string; value: model paramemter
    HashMap<Feature, Double> globalTableSumModel = null; 
    //key: feature string; val: (1) last avg-model parameter, 
    // (2) last iter-id; (3) the last sum-model parammter 
    HashMap<Feature, Double[]> globalTableAverageModel = null;

    private double gradientNorm;
    
    public DefaultPerceptron(HashMap<Feature, Double> sumModel, 
                             HashMap<Feature, Double[]> averageModel, int trainSize, 
                             int batchUpdateSize)
    {
        super(trainSize, batchUpdateSize);
        globalTableSumModel = sumModel;
        globalTableAverageModel = averageModel;
        if (globalTableSumModel == null || globalTableAverageModel == null)
        {
            LogInfo.error("model table is null");
            System.exit(0);
        }
    }
    
    public DefaultPerceptron(HashMap<Feature, Double> sumModel, 
                             HashMap<Feature, Double[]> averageModel, int trainSize, 
                             int batchUpdateSize, int convergePass, 
                             double initGain)
    {
        super(trainSize, batchUpdateSize, convergePass, initGain);
        globalTableSumModel = sumModel;
        globalTableAverageModel = averageModel;
        if (globalTableSumModel == null || globalTableAverageModel == null)
        {
            LogInfo.error("model table is null");
            System.exit(0);
        }
    }
    
    public DefaultPerceptron(HashMap<Feature, Double> sumModel, 
                             HashMap<Feature, Double[]> averageModel, int trainSize, 
                             int batchUpdateSize, int convergePass, double coolingSchedule, 
                             double initGain)
    {
        super(trainSize, batchUpdateSize, convergePass, coolingSchedule, initGain);
        globalTableSumModel = sumModel;
        globalTableAverageModel = averageModel;
        if (globalTableSumModel == null || globalTableAverageModel == null)
        {
            LogInfo.error("model table is null");
            System.exit(0);
        }
    }
    public DefaultPerceptron(HashMap<Feature, Double> sumModel, 
                             HashMap<Feature, Double[]> averageModel, int trainSize, 
                             int batchUpdateSize, int convergePass, double initGain, 
                             double sigma, double coolingSchedule, boolean isMinimizeScore)
    {
        super(trainSize, batchUpdateSize, convergePass, initGain, sigma, coolingSchedule, isMinimizeScore);
        globalTableSumModel = sumModel;
        globalTableAverageModel = averageModel;
        if (globalTableSumModel == null || globalTableAverageModel == null)
        {
            LogInfo.error("model table is null");
            System.exit(0);
        }
    }

    public void initModel(double minValue, double maxValue)
    {
        //TODO do nothing
    }

    //	update tbl_sum_model and tbl_avg_model inside
    public void updateModel(HashMap<Feature, Double> oracleFeatures, HashMap<Feature, Double> modelFeatures)
    {
        numModelChanges++;
//        Utils.logs("Update the perceptron model " + numModelChanges);
        HashMap<Feature, Double> gradient = getGradient(oracleFeatures, modelFeatures);
        //Support.print_hash_tbl(gradient);
        double updateGain = computeGain(numModelChanges);
//        Utils.logs("Update gain is " + updateGain + "; gradient table size " + gradient.size());
        gradientNorm = gradient.size();
        updateSumModel(globalTableSumModel, gradient, updateGain);
        updateAverageModel(globalTableSumModel, globalTableAverageModel, gradient, numModelChanges);
    }

    //update tbl_sum_model inside
    protected void updateSumModel(HashMap<Feature, Double> tableSumModel, 
                                  HashMap<Feature, Double> gradient, double updateGain)
    {
        for (Iterator<Feature> it = gradient.keySet().iterator(); it.hasNext();)
        {
            Feature key = it.next();
            Double oldValue = tableSumModel.get(key);
            Double update;
            if (oldValue != null)
            {
                update = oldValue + updateGain * gradient.get(key);
                tableSumModel.put(key, update);                
            }
            else
            {
                update = updateGain * gradient.get(key);
                tableSumModel.put(key, update); // incrementally add feature
            }
            key.setValue(update); // propagate change to the ProbVecs
        }
    }

    //	key: feat str; val: (1) last avg-model paramemter, (2) last iter-id; (3) the last sum-model paramemter
    //update tbl_avg_model inside
    protected void updateAverageModel(HashMap<Feature, Double> tableSumModel, 
                                      HashMap<Feature, Double[]> tableAverageModel, 
                                      HashMap featureSet, int curIterId)
    {//feature_set: the features need to be updated
        for (Iterator<Feature> it = featureSet.keySet().iterator(); it.hasNext();)
        {
            Feature key = it.next();
            updateAverageModelOneFeature(tableSumModel, tableAverageModel, key, curIterId);
        }
    }

    //tbl_sum_model has already been updated	
    //	key: feat str; val: (1) last avg-model paramemter, (2) last iter-id; (3) the last sum-model paramemter
    //	update tbl_avg_model inside
    protected void updateAverageModelOneFeature(HashMap<Feature, Double> tableSumModel, 
                                                HashMap<Feature, Double[]> tableAverageModel, 
                                                Feature featureKey, int curIterId)
    {
        Double[] oldValues = tableAverageModel.get(featureKey);
        Double[] newValues = new Double[3];
        newValues[1] = new Double(curIterId);//iter id 
        newValues[2] = tableSumModel.get(featureKey);//sum model para
        if (oldValues != null)
        {
            newValues[0] = (oldValues[0] * oldValues[1] + oldValues[2] * (curIterId - oldValues[1] - 1) + newValues[2]) / curIterId;//avg
        }
        else//incrementally add feature
        {
            newValues[0] = newValues[2] / curIterId;//avg			
        }
        tableAverageModel.put(featureKey, newValues);
    }

    /*
     * Force update the whole avg model. 
     * For each feature, it will automatically handle cases where the feature is 
     * already updated.
     */
    public void forceUpdateAverageModel()
    {
//        Utils.logs("force average update is called");
        updateAverageModel(globalTableSumModel, globalTableAverageModel, 
                           globalTableSumModel, numModelChanges); // update all features

    }

    public HashMap getAvgModel()
    {
        return globalTableAverageModel;
    }

    public void updateParamsWithAvgWeights()
    {
        for(Feature f : globalTableAverageModel.keySet())
        {
            f.setValue(globalTableAverageModel.get(f)[0]);
        }
    }
    
    public HashMap getSumModel()
    {
        return globalTableSumModel;
    }

    @Override
    public double getGradientNorm()
    {
        return gradientNorm;
    }

    public void setFeatureWeight(Feature feat, double weight)
    {
        globalTableSumModel.put(feat, weight);
        Double[] vals = new Double[3];
        vals[0] = weight;
        vals[1] = 1.0;//TODO
        vals[2] = 0.0;//TODO
        globalTableAverageModel.put(feat, vals);

    }
}
