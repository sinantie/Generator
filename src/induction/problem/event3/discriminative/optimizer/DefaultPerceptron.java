package induction.problem.event3.discriminative.optimizer;

import fig.basic.LogInfo;
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
    HashMap globalTableSumModel = null; 
    //key: feature string; val: (1) last avg-model parameter, 
    // (2) last iter-id; (3) the last sum-model parammter 
    HashMap globalTableAverageModel = null;

    public DefaultPerceptron(HashMap sumModel, HashMap averageModel, int trainSize, 
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
    public DefaultPerceptron(HashMap sumModel, HashMap averageModel, int trainSize, 
                             int batchUpdateSize, int convergePass, double initGain, 
                             double sigma, boolean isMinimizeScore)
    {
        super(trainSize, batchUpdateSize, convergePass, initGain, sigma, isMinimizeScore);
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
    public void updateModel(HashMap oracleFeatures, HashMap modelFeatures)
    {
        numModelChanges++;
        LogInfo.logs("Update the perceptron model " + numModelChanges);
        HashMap gradient = getGradient(oracleFeatures, modelFeatures);
        //Support.print_hash_tbl(gradient);
        double updateGain = computeGain(numModelChanges);
        LogInfo.logs("Update gain is " + updateGain + "; gradident table size " + gradient.size());
        updateSumModel(globalTableSumModel, gradient, updateGain);
        updateAverageModel(globalTableSumModel, globalTableAverageModel, gradient, numModelChanges);
    }

    //update tbl_sum_model inside
    protected void updateSumModel(HashMap tableSumModel, HashMap gradient, double updateGain)
    {
        for (Iterator it = gradient.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            Double oldValue = (Double) tableSumModel.get(key);
            if (oldValue != null)
            {
                tableSumModel.put(key, oldValue + updateGain * (Double) gradient.get(key));
            }
            else
            {
                tableSumModel.put(key, updateGain * (Double) gradient.get(key)); //incrementally add feature
            }
        }
    }

    //	key: feat str; val: (1) last avg-model paramemter, (2) last iter-id; (3) the last sum-model paramemter
    //update tbl_avg_model inside
    protected void updateAverageModel(HashMap tableSumModel, HashMap tableAverageModel, HashMap featureSet, int curIterId)
    {//feature_set: the features need to be updated
        for (Iterator it = featureSet.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            updateAverageModelOneFeature(tableSumModel, tableAverageModel, key, curIterId);
        }
    }

    //tbl_sum_model has already been updated	
    //	key: feat str; val: (1) last avg-model paramemter, (2) last iter-id; (3) the last sum-model paramemter
    //	update tbl_avg_model inside
    protected void updateAverageModelOneFeature(HashMap tableSumModel, HashMap tableAverageModel, String featureKey, int curIterId)
    {
        Double[] oldValues = (Double[]) tableAverageModel.get(featureKey);
        Double[] newValues = new Double[3];
        newValues[1] = new Double(curIterId);//iter id 
        newValues[2] = (Double) tableSumModel.get(featureKey);//sum model para
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
        LogInfo.logs("force avg update is called");
        updateAverageModel(globalTableSumModel, globalTableAverageModel, 
                           globalTableSumModel, numModelChanges); // update all features

    }

    public HashMap getAvgModel()
    {
        return globalTableAverageModel;
    }

    public HashMap getSumModel()
    {
        return globalTableSumModel;
    }

    public void setFeatureWeight(String feat, double weight)
    {
        globalTableSumModel.put(feat, weight);
        Double[] vals = new Double[3];
        vals[0] = weight;
        vals[1] = 1.0;//TODO
        vals[2] = 0.0;//TODO
        globalTableAverageModel.put(feat, vals);

    }
}
