package edu.berkeley.nlp.mt;

import junit.framework.TestCase;

/**
 *
 * @author konstas
 */
public class TestBleuScores extends TestCase
{
    private BatchBleuScorer bleuScorer;
    private BatchBleuModifiedScorer bleuModifiedScorer;
    private String predStr = "mostly cloudy , then a chance of rain after 10am . " +
                             "otherwise , mostly cloudy , with a high near 70 . " +
                             "west wind 6 to 23 mph .";

    private String trueStr = "sunny , with a high near 71 . windy , with a southwest " +
                             "wind 20 to 25 mph becoming northwest 10 to 15 mph . " +
                             "winds could gust as high as 30 mph .";

    public TestBleuScores(String name)
    {
        super(name);
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(4);
    }

    public void testBleuScorer()
    {
        assertEquals(bleuScorer.evaluateBleu(predStr, trueStr), 0.1649989693043071);        
    }
    public void testBleuModifiedScorer()
    {
        assertEquals(bleuModifiedScorer.evaluateBleu(predStr, trueStr), 0.2704423778271136);
    }
}
