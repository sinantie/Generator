package induction.utils.postprocess;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class ComputeAveragesOptions
{ 
    public enum ActionType {averageAlignmentsPerExample, averageFieldsWithNoValuePerRecord,
                            averageWordsPerSentence, averageWordsPerDocument, 
                            averageSentencesPerDocument};
    
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();    
    @Option(required=true) public ActionType actionType;
    @Option(gloss="Name of record to extract average fields with no value") public String record;
    @Option(gloss="Total number of fields per record") public int totalNumberOfFields;
    
}
