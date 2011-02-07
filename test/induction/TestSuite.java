/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author konstas
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({induction.InductionTest.class,induction.StagedInductionTest.class,induction.GenerationTest.class})
public class TestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

}