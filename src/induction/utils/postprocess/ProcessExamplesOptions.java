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
    public enum ActionType {averageAlignmentsPerExample, averageRecordsPerExample, averageFieldsWithNoValuePerRecord,
                            averageWordsPerSentence, averageWordsPerDocument, 
                            averageSentencesPerDocument, maxDocLength, maxValueLength, splitDocToSentences,
                            exportExamplesAsSentences, computePermMetrics, recordTypeStatistics};
    public enum PredFileType {alignment, generation}
    
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();    
    @Option(required=true) public ActionType actionType;
    @Option(gloss="Name of record to extract average fields with no value") public String record;
    @Option(gloss="Total number of fields per record") public int totalNumberOfFields;
    @Option(gloss="Split sentences") public boolean splitSentences;
    @Option(gloss="The number of <s> to put in front of a sentence") public int lmOrder = 3;
    @Option(gloss="The generation predicted output file that contains alignments") public String fullPredOutput;
    @Option(gloss="The input type of the predicted output file (for computing permutation metrics only)") public PredFileType predFileType;
    @Option(gloss="Exclude a field from the statistics counting (for record type statistics only)") public String excludeField;
    
}
