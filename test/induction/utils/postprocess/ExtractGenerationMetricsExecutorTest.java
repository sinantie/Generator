/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.utils.postprocess;

import fig.exec.Execution;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class ExtractGenerationMetricsExecutorTest
{
 
    ExtractGenerationMetrics egm;
    
    public ExtractGenerationMetricsExecutorTest()
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
    
    /**
     * Test of main method, of class ExtractGenerationMetricsExecutor.
     */
    @Test
    public void testWeatherGov()
    {
        String args =
                  "-inputFile1 results/output/weatherGov/generation/1-best_reordered_eventTypes_linear_reg_cond_null/stage1.tst.xml "
                + "-inputFile2 results/output/weatherGov/generation/dependencies/model_3_15-best_0.01_NO_STOP_inter1_hypRecomb_lmLEX_allowNone_NO_STOP/stage1.tst.xml "
                + "-outputFile results/output/weatherGov/generation/stat_significance/baseline_vs_lm "
                + "-inputFile1Type percy "
                + "-inputFile2Type percy ";
        ExtractGenerationMetricsOptions opts = new ExtractGenerationMetricsOptions();
        Execution.init(args.split(" "), new Object[]{opts}); // parse input params
        egm = new ExtractGenerationMetrics(opts);
        egm.execute();   
    }
}
