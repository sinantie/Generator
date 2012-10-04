/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction;

import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import edu.berkeley.nlp.ling.Trees.PennTreeRenderer;
import java.io.StringReader;
import edu.berkeley.nlp.ling.Tree;
import induction.TreeUtils.NodeFunc;
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
public class TreeUtilsTest
{
    
    public TreeUtilsTest()
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
     * Test of rightBinarize method, of class TreeUtils.
     */
    @Test
    public void testRightBinarize()
    {
        System.out.println("rightBinarize");
        int order = -1;
//        PennTreeReader reader = new PennTreeReader(new StringReader("(S (SENT1 (6 a) (8 b) (7 c) ) (SENT2 (5 d) (0 e) ) (SENT3 (3 f) (2 g) ) )"));
        PennTreeReader reader = new PennTreeReader(new StringReader("(S (6-8-7 (6 a) (8 b) (7 c) ) (5-0 (5 d) (0 e) ) (3-2 (3 f) (2 g) ) )"));
        Tree<String> tree = reader.next();
        System.out.println("Input:\n" + PennTreeRenderer.render(tree));   
        Tree<String> result =  TreeUtils.rightBinarize(tree, order);    
        System.out.println("Output:\n" + PennTreeRenderer.render(result));    
        
//        Tree expResult = null;        
//        assertEquals(expResult, result);        
    }
    
    /**
     * Test of rightBinarize method, of class TreeUtils.
     */
    @Test
    public void testLeftBinarize()
    {
        System.out.println("leftBinarize");
        int order = -1;
//        PennTreeReader reader = new PennTreeReader(new StringReader("(S (SENT1 (6 a) (8 b) (7 c) ) (SENT2 (5 d) (0 e) ) (SENT3 (3 f) (2 g) ) )"));
        PennTreeReader reader = new PennTreeReader(new StringReader("(S (6-8-7 (6 a) (8 b) (7 c) ) (5-0 (5 d) (0 e) ) (3-2 (3 f) (2 g) ) )"));
        Tree<String> tree = reader.next();
        System.out.println("Input:\n" + PennTreeRenderer.render(tree));   
        Tree<String> result =  TreeUtils.leftBinarize(tree, order);    
        System.out.println("Output:\n" + PennTreeRenderer.render(result));    
        
//        Tree expResult = null;        
//        assertEquals(expResult, result);        
    }

    /**
     * Test of headBinarize method, of class TreeUtils.
     */
//    @Test
    public void testHeadBinarize()
    {
        System.out.println("headBinarize");
        Tree<String> tree = null;
        int order = 0;
        Tree expResult = null;
        Tree result = TreeUtils.headBinarize(tree, order);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeUnaries method, of class TreeUtils.
     */
//    @Test
    public void testRemoveUnaries()
    {
        System.out.println("removeUnaries");
        Tree<String> tree = null;
        Tree expResult = null;
        Tree result = TreeUtils.removeUnaries(tree);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of replaceTerminalsWithPreterminals method, of class TreeUtils.
     */
//    @Test
    public void testReplaceTerminalsWithPreterminals()
    {
        System.out.println("replaceTerminalsWithPreterminals");
        Tree<String> tree = null;
        Tree expResult = null;
        Tree result = TreeUtils.replaceTerminalsWithPreterminals(tree);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transformNonterminals method, of class TreeUtils.
     */
//    @Test
    public void testTransformNonterminals()
    {
        System.out.println("transformNonterminals");
        Tree<String> tree = null;
        NodeFunc<String> func = null;
        Tree expResult = null;
        Tree result = TreeUtils.transformNonterminals(tree, func);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isPunctuationTag method, of class TreeUtils.
     */
    @Test
    public void testIsPunctuationTag()
    {
        System.out.println("isPunctuationTag");
        String tag = "./.";
        boolean expResult = false;
        boolean result = TreeUtils.isPunctuationTag(tag);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of removePunctuation method, of class TreeUtils.
     */
    @Test
    public void testRemovePunctuation()
    {
        System.out.println("removePunctuation");       
        Tree<String> tree = new PennTreeReader(new StringReader("(S (NP (PRP I)) (VP (VBD ate) (. .)) )")).next();
        Tree expResult = new PennTreeReader(new StringReader("(S (NP (PRP I)) (VP (VBD ate)) )")).next();
        Tree result = TreeUtils.removePunctuation(tree);
        assertEquals(expResult.toString(), result.toString());        
    }
}
