/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.utils.linearregression;

import fig.exec.Execution;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author konstas
 */
public class LinearRegressionExecutorTest
{
    LinearRegressionWekaWrapper lrw;
    
    public LinearRegressionExecutorTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

//    @Test
//    public void testTrainWinHelp()
//    {
//        int fold = 2;
//        String type = "values";
//        String args = "-mode train "
//                    + "-inputFeaturesFile data/branavan/winHelpHLA/winHelpRL.sents.all "
////                    + "-inputFeaturesFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train "
//                    + "-outputFeaturesFile data/branavan/winHelpHLA/winHelpRL.sents.all."+type+".features.csv "
////                    + "-outputFeaturesFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train."+type+".features.csv "
//                    + "-examplesInSingleFile "
//                    + "-paramsFile results/output/winHelp/alignments/model_3_no_null_pos_auto/all/stage1.params.obj.gz "
////                    + "-paramsFile results/output/winHelp/alignments/model_3_no_null_pos_auto/fold"+fold+"/stage1.params.obj.gz "
//                    + "-modelFile data/branavan/winHelpHLA/lengthPrediction."+type+".linear-reg.model "
////                    + "-modelFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train.lengthPrediction."+type+".linear-reg.model "
//                    + "-type "+type+ " "
//                    + "-startIndex 2 "
//                    + "-extractFeatures "
//                    + "-saveModel";
//        LinearRegressionOptions opts = new LinearRegressionOptions();
//        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
//        lrw = new LinearRegressionWekaWrapper(opts);
//        lrw.train(opts.outputFeaturesFile, opts.saveModel);
//        // original text: click start , point to settings , and then click control panel (12 words)
//        String events = ".id:0	.type:action	@envCmd:left click 	@objName:start	@objType:Button\n" 
//                        +".id:1	.type:action	@envCmd:left click 	@objName:Settings	@objType:Button\n"
//                        +".id:2	.type:action	@envCmd:left click 	@objName:Control Panel	@objType:Button\n";
//        assertEquals((int)lrw.predict(events), 14);
//    }
    
    @Test
    public void testTrainRobocup()
    {        
        String type = "values";
        String args = "-mode train "
                    + "-inputFeaturesFile robocupLists/robocupFold1PathsTrain "
                    + "-outputFeaturesFile robocupLists/robocupFold1PathsTrain."+type+".features.csv "
                    + "-paramsFile results/output/robocup/alignments/model_3_percy_oneEvent_unk_no_generic_newField_POS/fold1/stage1.params.obj.gz "
                    + "-modelFile robocupLists/robocupFold1PathsTrain.lengthPrediction."+type+".linear-reg.model "
//                    + "-modelFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train.lengthPrediction."+type+".linear-reg.model "
                    + "-type "+type+ " "
                    + "-startIndex 3 "
                    + "-extractFeatures ";
//                    + "-saveModel";
        LinearRegressionOptions opts = new LinearRegressionOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        lrw = new LinearRegressionWekaWrapper(opts);
        lrw.train(opts.outputFeaturesFile, opts.saveModel);
        // original text: click start , point to settings , and then click control panel (12 words)
        String events = ".id:0	.type:action	@envCmd:left click 	@objName:start	@objType:Button\n" 
                        +".id:1	.type:action	@envCmd:left click 	@objName:Settings	@objType:Button\n"
                        +".id:2	.type:action	@envCmd:left click 	@objName:Control Panel	@objType:Button\n";
        assertEquals((int)lrw.predict(events), 14);
    }
}
