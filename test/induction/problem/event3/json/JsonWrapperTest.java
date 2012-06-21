/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import fig.basic.Indexer;
import induction.Options.JsonFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sinantie
 */
public class JsonWrapperTest
{
    
    public JsonWrapperTest()
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

    @Test
    public void testLowJetJson()
    {
        String trueExample = ".id:0	.type:flight	@aircraft_code:--	@airline:--	@class_type:--	@direction:round_trip	@engine:--	@fare:--	@flight_number:--	@from:atlanta	@manufacturer:--	@price:--	@stop:--	@to:boston	@year:--\n"+
                             ".id:1	.type:search	@of:flight	@typed:max	@what:fare\n"+
                             ".id:2	.type:search	@of:departure_time	@typed:argmin	@what:flight";
        String example = "{\"flight\":{\"class_type\":\"--\",\"direction\":\"round_trip\",\"from\":\"atlanta\",\"stop\":\"--\",\"to\":\"boston\"},\"search\":[{\"of\":\"flight\",\"typed\":\"max\",\"what\":\"fare\"},{\"of\":\"departure_time\",\"typed\":\"argmin\",\"what\":\"flight\"}]}";
//        String example = "{\"flight\":{\"class_type\":\"--\",\"direction\":\"round_trip\",\"from\":\"atlanta\",\"stop\":\"--\",\"to\":\"boston\"},\"search\":[{\"of\":\"--\",\"typed\":\"lambda\",\"what\":\"flight\"}]}";
        JsonWrapper wrapper = new JsonWrapper(example, JsonFormat.lowjet, new Indexer<String>());
        assertEquals(trueExample, wrapper.getEventsString()[0]);
    }
}
