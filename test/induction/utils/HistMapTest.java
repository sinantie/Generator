/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.utils;

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
public class HistMapTest
{    
    static HistMap<String> myMap;
    
    public HistMapTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        myMap = new HistMap<String>();
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
    public void testOutput()
    {
        myMap.add("test");
        myMap.add("test");
        myMap.add("test");
        myMap.add("test");
        myMap.add("hello");
        myMap.add("world");
        myMap.add("hello");
        System.out.println(myMap.toString());
        assertEquals(myMap.toString(), "hello : 2\ntest : 4\nworld : 1\n");
    }
}
