package induction.runtime;

import induction.Utils;
import induction.WekaWrapper;
import java.io.IOException;
import induction.utils.ExtractFeatures.FeatureType;
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
public class WekaWrapperTest {

    private String modelFilename, paramsFilename;

    public WekaWrapperTest() {
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

    @Test
    public void testTrainAtisWrapper()
    {
        boolean serialise = false;
        modelFilename = "data/atis/train/lengthPrediction.counts.linear-reg.model";
        paramsFilename = "results/output/atis/alignments/"
                    + "model_3/15_iter_no_null_no_smooth_STOP/stage1.params.obj";
        WekaWrapper lengthPredictor = new WekaWrapper(paramsFilename, modelFilename,
                                                      2, FeatureType.COUNTS, WekaWrapper.Mode.TRAIN);
        lengthPredictor.train("data/atis/train/atis5000.sents.full.counts.features.csv", serialise);
        // original text: i need an early flight from milwaukee to denver (9 words)
        String events = ".id:0	.type:flight	@aircraft_code:--	@airline:--	"
                        + "@class_type:--	@direction:--	@engine:--	@fare:--	"
                        + "@flight_number:--	@from:milwaukee	@manufacturer:--	"
                        + "@price:--	@stop:--	@to:denver	@year:--"
                        + ".id:1	.type:search	@of:--	@typed:lambda	@what:flight"
                        + ".id:2	.type:when	@dep-ar:departure	@when:early";
        assertEquals((int)lengthPredictor.predict(events), 13);
    }

    @Test
    public void testTrainWeatherGovWrapper()
    {
        boolean serialise = false;
        modelFilename = "gaborLists/lengthPrediction.values.linear-reg.model";
        paramsFilename = "results/output/weatherGov/alignments/"
                    + "model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj";
        WekaWrapper lengthPredictor = new WekaWrapper(paramsFilename, modelFilename,
                                                      4, FeatureType.VALUES, WekaWrapper.Mode.TRAIN);
        lengthPredictor.train("gaborLists/trainListPathsGabor.values.features.csv", serialise);
        // original text: Mostly cloudy , with a low around 39 . Southwest wind between 9 and 14 mph . (17 words)
        String events = "";
        try
        {
            events = Utils.readFileAsString(
                "data/weather-data-full/data/virginia/falls_church/2009-02-07-0.events");
        }
        catch(IOException ioe){}
        
        assertEquals((int)lengthPredictor.predict(events), 18);
    }

    @Test
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
        WekaWrapper lengthPredictor = new WekaWrapper(paramsFilename, modelFilename,
                                                      2, FeatureType.COUNTS, WekaWrapper.Mode.TEST);
        assertEquals((int)lengthPredictor.predict(events), 13);
    }

    @Test
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
        WekaWrapper lengthPredictor = new WekaWrapper(paramsFilename, modelFilename,
                                                      4, FeatureType.VALUES, WekaWrapper.Mode.TEST);
        assertEquals((int)lengthPredictor.predict(events), 18);
    }
}