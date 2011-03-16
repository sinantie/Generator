/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction;

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
public class UtilsTest {

    public UtilsTest() {
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
     public void computeWER()
     {
         System.out.println("test 1: equal");
         String pred = "I write computer programmes";
         String tr = "I write computer programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.0));
         System.out.println("test 2: one missing word");
         pred = "I write programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
         System.out.println("test 3: one missing word, one extra word");
         pred = "I write nice programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
         System.out.println("test 4: one extra word");
         pred = "I write nice computer programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
         System.out.println("test 5: one extra word, one swapping word");
         pred = "I write nice computer software";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.5));
         System.out.println("test 6: no words");
         pred = "";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(1));
     }

}