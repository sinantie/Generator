
package induction.utils;

import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import induction.Utils;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author konstas
 */
public class TestGenerationMetrics
{
    MeteorScorer meteorScorer;
    BatchBleuScorer bleuScorer;
    
    @Before
    public void setUp() 
    {
        meteorScorer = Meteor.MeteorScorerFactory();
        bleuScorer = new BatchBleuScorer();
    }
    
    @Test
    public void testMeteor()
    {
        String trueStr = "A 40 percent chance of showers before 10am . Mostly cloudy , with a high near 44 . East northeast wind around 7 mph .".toLowerCase().trim();
        String predStr = "A chance of showers . Patchy fog before noon . Mostly cloudy , with a high near 44 . East wind between 6 and 7 mph . Chance of precipitation is 35%".toLowerCase().trim();
        MeteorStats meteorWidgetStats = meteorScorer.getMeteorStats(
                                      predStr,
                                      trueStr);
        double bleuScore = bleuScorer.evaluateBleu(predStr, trueStr);
        
        System.out.println(String.format("METEOR = %s BLEU-4 = %s", 
                Utils.fmt(meteorWidgetStats.score), Utils.fmt(bleuScore)));
    }
}
