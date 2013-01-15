package induction.utils.linearregression;

import induction.Utils;
import java.io.IOException;
import induction.utils.linearregression.LinearRegressionOptions.FeatureType;
import induction.utils.linearregression.LinearRegressionOptions.Mode;
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
public class LinearRegressionWekaWrapperTest {

    private String modelFilename, paramsFilename;

    public LinearRegressionWekaWrapperTest() {
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
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Test
    public void testTrainAtisWrapper()
    {
        boolean serialise = false;
        modelFilename = "data/atis/train/lengthPrediction.counts.linear-reg.model";
        paramsFilename = "results/output/atis/alignments/"
                    + "model_3/15_iter_no_null_no_smooth_STOP/stage1.params.obj";
        LinearRegressionWekaWrapper lengthPredictor = new LinearRegressionWekaWrapper(paramsFilename, modelFilename,
                                                      2, FeatureType.counts, Mode.train);
        lengthPredictor.train("data/atis/train/atis5000.sents.full.counts.features.csv", serialise);
        // original text: i need an early flight from milwaukee to denver (9 words)
        String events = ".id:0	.type:flight	@aircraft_code:--	@airline:--	"
                        + "@class_type:--	@direction:--	@engine:--	@fare:--	"
                        + "@flight_number:--	@from:milwaukee	@manufacturer:--	"
                        + "@price:--	@stop:--	@to:denver	@year:--"
                        + ".id:1	.type:search	@of:--	@typed:lambda	@what:flight"
                        + ".id:2	.type:when	@dep-ar:departure	@when:early";        
        try
        {
            int pred = (int)lengthPredictor.predict(events);
            System.out.println(pred);
            assertEquals(pred, 13);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
    @Test
    public void testTrainWinHelpWrapper()
    {
        boolean serialise = false;
        modelFilename = "data/branavan/data/branavan/winHelpHLA/lengthPrediction.counts.linear-reg.model";
        paramsFilename = "results/output/winHelp/alignments/model_3_no_null_pos_auto/all/stage1.params.obj.gz";
        LinearRegressionWekaWrapper lengthPredictor = new LinearRegressionWekaWrapper(paramsFilename, modelFilename,
                                                      2, FeatureType.counts, Mode.train);
        lengthPredictor.train("data/branavan/data/branavan/winHelpHLA/winHelpRL.sents.all.counts.features.csv", serialise);
        // original text: click start , point to settings , and then click control panel (12 words)
        String events = ".id:0	.type:action	@envCmd:left click 	@objName:start	@objType:Button " 
                        +".id:1	.type:action	@envCmd:left click 	@objName:Settings	@objType:Button "
                        +".id:2	.type:action	@envCmd:left click 	@objName:Control Panel	@objType:Button ";
        try
        {
            assertEquals((int)lengthPredictor.predict(events), 12);
        }
        catch(Exception e)
        {
            System.out.println("Error " + e.getMessage());
        }
    }

//    @Test
    public void testTrainWeatherGovWrapper()
    {
        boolean serialise = false;
        modelFilename = "gaborLists/lengthPrediction.values.linear-reg.model";
        paramsFilename = "results/output/weatherGov/alignments/"
                    + "model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj";
        LinearRegressionWekaWrapper lengthPredictor = new LinearRegressionWekaWrapper(paramsFilename, modelFilename,
                                                      4, FeatureType.values, Mode.train);
        lengthPredictor.train("gaborLists/trainListPathsGabor.values.features.csv", serialise);
        // original text: Mostly cloudy , with a low around 39 . Southwest wind between 9 and 14 mph . (17 words)
        String events = "";
        try
        {
            events = Utils.readFileAsString(
                "data/weather-data-full/data/virginia/falls_church/2009-02-07-0.events");
        }
        catch(IOException ioe){}
        try
        {
            int pred = (int)lengthPredictor.predict(events);
            System.out.println(pred);
            assertEquals(pred, 18);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }        
    }

//    @Test
    public void testPredictAtisWrapper()
    {
        modelFilename = "data/atis/train/lengthPrediction.counts.linear-reg.model";
        paramsFilename = "results/output/atis/alignments/"
                    + "model_3/15_iter_no_null_no_smooth_STOP/stage1.params.obj";
        // original text: i need an early flight from milwaukee to denver (9 words)
        String events = ".id:0	.type:flight	@aircraft_code:--	@airline:--	"
                        + "@class_type:--	@direction:--	@engine:--	@fare:--	"
                        + "@flight_number:--	@from:milwaukee	@manufacturer:--	"
                        + "@price:--	@stop:--	@to:denver	@year:--"
                        + ".id:1	.type:search	@of:--	@typed:lambda	@what:flight"
                        + ".id:2	.type:when	@dep-ar:departure	@when:early";
        LinearRegressionWekaWrapper lengthPredictor = new LinearRegressionWekaWrapper(paramsFilename, modelFilename,
                                                      2, FeatureType.counts, Mode.test);        
        try
        {
            int pred = (int)lengthPredictor.predict(events);
            System.out.println(pred);
            assertEquals(pred, 13);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

//    @Test
    public void testPredictWeatherGovWrapper()
    {
        modelFilename = "gaborLists/lengthPrediction.values.linear-reg.model";
        paramsFilename = "results/output/weatherGov/alignments/"
                    + "model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj";
        // original text: Mostly cloudy , with a low around 39 . Southwest wind between 9 and 14 mph . (17 words)
        String events = "";
        try
        {
            events = Utils.readFileAsString(
                "data/weather-data-full/data/virginia/falls_church/2009-02-07-0.events");
        }
        catch(IOException ioe){}
        LinearRegressionWekaWrapper lengthPredictor = new LinearRegressionWekaWrapper(paramsFilename, modelFilename,
                                                      4, FeatureType.values, Mode.test);
        try
        {
            int pred = (int)lengthPredictor.predict(events);
            System.out.println(pred);
            assertEquals(pred, 18);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}