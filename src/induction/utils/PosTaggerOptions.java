package induction.utils;

import fig.basic.Option;

/**
 *
 * @author sinantie
 */
public class PosTaggerOptions
{
    public enum TypeOfPath {list, file}
    public enum TypeOfInput {events, raw}
    
    @Option(required=true) public String inputPath;
    @Option(required=true) public TypeOfPath typeOfPath;
    @Option(required=true) public TypeOfInput typeOfInput;
    @Option public String extension = "";
    @Option public String posDictionaryPath = "";
    @Option public boolean useUniversalTags = false;        
    @Option public boolean replaceNumbers = false;
    @Option public boolean verbose = false;
    @Option(gloss="Use POS tagger to resolve ambiguities in case we are using"
            + "a POS dictionary") public boolean forceTagger = false;
}
