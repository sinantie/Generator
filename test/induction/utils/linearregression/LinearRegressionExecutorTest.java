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
    public void testTrainA0()
    {
        String dataset = "GoldLogoAll";
        int fold = 2;
        String type = "values";
        String args = "-mode train "
                    + "-inputFeaturesFile datasets/"+dataset+"/Records.dev "
                    + "-outputFeaturesFile datasets/"+dataset+"/Dev.data."+type+".features.csv "
//                    + "-inputFeaturesFile datasets/"+dataset+"/Records.train "
//                    + "-outputFeaturesFile datasets/"+dataset+"/Train.data."+type+".features.csv "
                    + "-examplesInSingleFile "
                    + "-useMultipleReferences "
                    + "-paramsFile results/"+dataset+"/alignments/7.exec/stage1.params.obj.gz "
                    + "-modelFile results/"+dataset+"/lengthPrediction."+type+".linear-reg.model "
                    + "-type "+type+ " "
                    + "-startIndex 2 "
                    + "-extractFeatures "
                    + "-saveModel";
        LinearRegressionOptions opts = new LinearRegressionOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        lrw = new LinearRegressionWekaWrapper(opts);
        lrw.train(opts.outputFeaturesFile, opts.saveModel);
        /* original text: 
         * move the adidas block directly diagonally left and below the heineken block .*/
        String events = ".id:0	.type:move	@source:1	@target:6	@RP:5\n";
        try
        {
            int pred = (int)lrw.predict(events);
            System.out.println(pred);
            assertEquals(pred, 21);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testTestA0()
    {        
        String dataset = "GoldLogoAll";
        String type = "values";
        String args = "-mode test "
                    + "-inputFile datasets/"+dataset+"/Records.dev "
                    + "-examplesInSingleFile "
                    + "-paramsFile results/"+dataset+"/alignments/7.exec/stage1.params.obj.gz "
                    + "-modelFile datasets/"+dataset+"/train.linear.regression.model "
                    + "-type "+type+ " "
                    + "-startIndex 2 ";                    
//                    + "-saveModel";
        LinearRegressionOptions opts = new LinearRegressionOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        lrw = new LinearRegressionWekaWrapper(opts);        
        lrw.predict(opts.inputFile, opts.examplesInSingleFile);
    }

    
//    @Test
    public void testTrainWinHelp()
    {
        int fold = 2;
        String type = "counts";
        String args = "-mode train "
//                    + "-inputFeaturesFile data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation "
                    + "-inputFeaturesFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train "
                    + "-outputFeaturesFile data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation."+type+".features.csv "
//                    + "-outputFeaturesFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train."+type+".features.csv "
                    + "-examplesInSingleFile "
                    + "-paramsFile results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.params.obj.gz "
//                    + "-paramsFile results/output/winHelp/alignments/model_3_no_null_pos_auto/fold"+fold+"/stage1.params.obj.gz "
                    + "-modelFile data/branavan/winHelpHLA/lengthPrediction."+type+".linear-reg.model "
//                    + "-modelFile data/branavan/winHelpHLA/folds/winHelpFold"+fold+"Train.lengthPrediction."+type+".linear-reg.model "
                    + "-type "+type+ " "
                    + "-startIndex 2 "
                    + "-extractFeatures "
                    + "-saveModel";
        LinearRegressionOptions opts = new LinearRegressionOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        lrw = new LinearRegressionWekaWrapper(opts);
        lrw.train(opts.outputFeaturesFile, opts.saveModel);
        /* original text: 
         * click start , point to settings , and then click control panel . 
         * double-click power options . on the advanced tab , 
         * click to select the prompt for password when computer goes off standby check box . (37 words)*/
        String events = ".id:0	.type:navigate-desktop	@envCmd:left click	$objName:start	@objType:Button\n"
                + ".id:1	.type:navigate-start	@envCmd:left click	$objName:settings	@objType:Button\n"
                + ".id:2	.type:navigate-start-target	@envCmd:left click	$objName:control panel	@objType:Button\n"
                + ".id:3	.type:navigate-window-target	@envCmd:double click	$objName:power options	@objType:Item\n"
                + ".id:4	.type:navigate-contextMenu	@envCmd:left click	$objName:advanced	@objType:Tab\n"
                + ".id:5	.type:action-contextMenu	@envCmd:left click	$objName:prompt for password when computer goes off standby	$typeInto:--	@objType:checkbox\n";
        try
        {
            int pred = (int)lrw.predict(events);
            System.out.println(pred);
            assertEquals(pred, 37);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
//    @Test
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
        try
        {
            assertEquals((int)lrw.predict(events), 14);
        }
        catch(Exception e)
        {
            System.out.println("Error " + e.getMessage());
        }
    }
    
//    @Test
    public void testTestWeatherGov()
    {        
        String type = "values";
        String args = "-mode test "
                    + "-inputFile data/weatherGov/weatherGovGenEvalGabor.gz "
                    + "-examplesInSingleFile "
                    + "-paramsFile results/output/weatherGov/alignments/model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj "
                    + "-modelFile gaborLists/lengthPrediction."+type+".linear-reg.model "
                    + "-type "+type+ " "
                    + "-startIndex 4 ";                    
//                    + "-saveModel";
        LinearRegressionOptions opts = new LinearRegressionOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        lrw = new LinearRegressionWekaWrapper(opts);        
        lrw.predict(opts.inputFile, opts.examplesInSingleFile);
    }
    
}
