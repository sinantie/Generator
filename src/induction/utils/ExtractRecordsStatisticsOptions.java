package induction.utils;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class ExtractRecordsStatisticsOptions
{
    public enum Type {record, recordType};
    public enum Direction {left, right};
    
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();
    
    @Option(required=true) public Type exportType = Type.recordType;    
    @Option(gloss="Split at every punctuation or just at '.'") public boolean splitClauses = false;
    @Option(gloss="Export event type names instead of ids") public boolean useEventTypeNames = false;
    @Option(gloss="Export (none) event type") public boolean extractNoneEvent = false;
    @Option(gloss="Write record (type) assignments as a flat file") public boolean writePermutations = false;
    @Option(gloss="Count the number of times each record (type) gets repeated across clauses") public boolean countRepeatedRecords = false;    
    @Option(gloss="Count the number of record (type) ngrams per sentence") public boolean countSentenceNgrams = false;    
    @Option(gloss="Count the number of record (type) sentence ngrams per document") public boolean countDocumentNgrams = false;    
    @Option(gloss="Output a delimiter '|' between sentences") public boolean delimitSentences = false;
    @Option(gloss="Write record (type) assignments as an mrg tree") public boolean extractRecordTrees  = false;
    @Option(gloss="Left binarize or right binarize") public Direction binarize = Direction.left;
    @Option(gloss="Markovization order") public int markovOrder = -1;
    @Option(gloss="Use modified binarization. Intermediate labels are generated from children labels.") public boolean modifiedBinarization = false;
}