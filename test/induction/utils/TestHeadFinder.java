/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package induction.utils;


import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class TestHeadFinder
{
    private static LexicalizedParser parser;

    public TestHeadFinder() 
    {        
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        parser = new LexicalizedParser("lib/models/englishPCFG.ser.gz");
        parser.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
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
    public void testParser()
    {
        String sent[] = "with a low".split(" ");
        Tree parse = (Tree) parser.apply(Arrays.asList(sent));

//        parse.pennPrint();
//        System.out.println();
//
//        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//        Collection tdl = gs.typedDependenciesCollapsed();
//        System.out.println(tdl);
//        System.out.println();

        TreePrint tp = new TreePrint("penn");
        tp.printTree(parse);
    }

    @Test
    public void testHeadFinder()
    {
        String sent[] = "mostly cloudy , with a low around".split(" ");
        Tree tree = (Tree) parser.apply(Arrays.asList(sent));
        CollinsHeadFinder headFinder = new CollinsHeadFinder();
        while (!tree.isLeaf()) {
            Tree head = headFinder.determineHead(tree);
            System.out.println("head "+head);
            tree=head;
        }
        System.out.println();
        tree.headTerminal(headFinder);
        tree.pennPrint();
    }
}