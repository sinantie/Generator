/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.runtime;

import induction.Utils;
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
     public void computeWERTest1()
     {
         System.out.println("test 1: equal");
         String pred = "I write computer programmes";
         String tr = "I write computer programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.0));
     }
     @Test
     public void computeWERTest2()
     {
         System.out.println("test 2: one missing word");
         String tr = "I write computer programmes";
         String pred = "I write programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
     }
     @Test
     public void computeWERTest3()
     {
         System.out.println("test 3: one missing word, one extra word");
         String tr = "I write computer programmes";
         String pred = "I write nice programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
     }
     @Test
     public void computeWERTest4()
     {
         System.out.println("test 4: one extra word");
         String tr = "I write computer programmes";
         String pred = "I write nice computer programmes";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.25));
     }
     @Test
     public void computeWERTest5()
     {
         System.out.println("test 5: one extra word, one swapping word");
         String tr = "I write computer programmes";
         String pred = "I write nice computer software";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.5));
     }
     @Test
     public void computeWERTest6()
     {
         System.out.println("test 6: no words");
         String tr = "I write computer programmes";
         String pred = "";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(1.0));
     }

     @Test
     public void computeWERTest7()
     {
         System.out.println("test 7: extra field-value");
         String pred = "min[5_v1] mode-bucket-0-20-2[10-20_v] max[7_v1]";
         String tr = "min[5_v1] max[7_v1]";
         assertEquals(new Float(Utils.computeWER(pred, tr)), new Float(0.5));
     }      
     
     @Test
     public void detokeniseTest()
     {
         String input = "mostly sunny , cloudy , with a high near 64 . west wind between 19 and 20 mph .";
         System.out.println(Utils.deTokenize(input));
     }                                   

}