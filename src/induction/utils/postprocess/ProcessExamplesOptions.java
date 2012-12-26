package induction.utils.postprocess;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class ProcessExamplesOptions
{ 
    public enum ActionType {averageAlignmentsPerExample, averageFieldsWithNoValuePerRecord,
                            averageWordsPerSentence, averageWordsPerDocument, 
                            averageSentencesPerDocument, maxDocLength, maxValueLength, splitDocToSentences,
                            exportExamplesAsSentences};
    
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();    
    @Option(required=true) public ActionType actionType;
    @Option(gloss="Name of record to extract average fields with no value") public String record;
    @Option(gloss="Total number of fields per record") public int totalNumberOfFields;
    @Option(gloss="Split sentences") public boolean splitSentences;
    @Option(gloss="The number of <s> to put in front of a sentence") public int lmOrder = 3;
    
}
