package induction.utils;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 *
 * @author konstas
 */
public class TestKnn {

    private String modelFilename;

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
//         modelFilename = "data/atis/train/atis5000.sents.full.knn-10.model";
         modelFilename = "data/atis/train/atis5000.sents.full.linear-reg.model";
         try
         {             
//             IBk classifier = new IBk();
//             classifier.setOptions(options);
//             classifier.classifyInstance(new Instance());
             Classifier cls = (Classifier) weka.core.SerializationHelper.read(modelFilename);
             ArrayList<Attribute> attrs = new ArrayList<Attribute>();
             for(int i = 0; i < 57; i++)
                 attrs.add(new Attribute("attr_"+i));
             Instances i = new Instances("Test", attrs, 1);
             i.setClassIndex(56);
             double[] in = new double[56];
             int j = 0;
             for(String s : "0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0".split(","))
                 in[j++] = Double.valueOf(s);
             Instance di = new SparseInstance(1.0, in);
             di.setDataset(i);
             System.out.println(cls.classifyInstance(di));

         }
         catch(Exception e) {
            e.printStackTrace();
         }
     }

}