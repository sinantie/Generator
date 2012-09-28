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
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();
    
    @Option(required=true) public Type exportType = Type.recordType;    
    @Option public boolean splitClauses = false;
    @Option public boolean writePermutations = false;
    @Option public boolean countRepeatedRecords = false;    
    @Option public boolean countSentenceNgrams = false;    
    @Option public boolean countDocumentNgrams = false;    
    @Option public boolean delimitSentences = false;    
}
