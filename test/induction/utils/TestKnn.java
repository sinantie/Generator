package induction.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

/**
 *
 * @author konstas
 */
public class TestKnn {

    private String modelFilename;
    private String testSetFilename;

    public TestKnn() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void testAtis()
     {
         modelFilename = "data/atis/train/atis5000.sents.full.knn-10.model";
         testSetFilename = "data/atis/test/atis-test.txt.counts.features.csv";
         try
         {
             String[] options = weka.core.Utils.splitOptions(String.format(
                     "java weka.classifiers.lazy.IBk -K 10 -W 0 -I "
                     + "-A \"weka.core.neighboursearch.LinearNNSearch -A "
                     + "\"weka.core.EuclideanDistance -R first-last\"\" "
                     + "-l '%s' -T '%s'", modelFilename, testSetFilename));
             IBk classifier = new IBk();
             classifier.setOptions(options);

         }
         catch(Exception e) {}
     }

}