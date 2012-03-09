package induction.utils;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class TestPosTagger extends TestCase
{
    PosTagger posTagger;

    public TestPosTagger(String name)
    {
        super(name);        
    }
    
//    @Test
//    public void testAtisPosTagger()
//    {               
//        posTagger = new PosTagger("data/atis/train/atis5000.sents.full", 
//                                  PosTagger.TypeOfPath.file, 
//                                  PosTagger.TypeOfInput.events, 
//                                 "/home/konstas/EDI/candc/candc-1.00/atis_tagged_manual_disambiguated.out_sorted",
//                                 false,
//                                 false,
//                                 "");
//        posTagger.execute();
//    }
    
    @Test
    public void testWeatherGovPosTagger()
    {               
//        posTagger = new PosTagger("gaborLists/genDevListPathsGabor", 
        posTagger = new PosTagger("gaborLists/trainListPathsGabor",
                                  PosTagger.TypeOfPath.list, 
                                  PosTagger.TypeOfInput.raw, 
                                  "",
                                  false,
                                  true,
                                  "text");
        posTagger.execute();
    }
}
