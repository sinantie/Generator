package induction.utils;

import junit.framework.TestCase;

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

    public void testPosTagger()
    {        
//        assertEquals("./.", posTagger.tag(".").trim());
//        assertEquals(",/,", posTagger.tag(",").trim());
//        assertEquals("-LRB-/-LRB-", posTagger.tag("(").trim());
//        assertEquals("-RRB-/-RRB-", posTagger.tag(")").trim());
//        assertEquals("--/:", posTagger.tag("--").trim());
//        assertEquals("and/CC", posTagger.tag("and").trim());
        
        posTagger = new PosTagger("data/atis/train/atis5000.sents.full", 
                                  PosTagger.TypeOfPath.file, 
                                  PosTagger.TypeOfInput.events, 
                                 "/home/konstas/EDI/candc/candc-1.00/atis_tagged_manual_disambiguated.out_sorted",
                                 false,
                                 "");
        posTagger.execute();
    }
}
