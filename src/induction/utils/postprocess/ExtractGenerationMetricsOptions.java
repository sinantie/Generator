package induction.utils.postprocess;

import fig.basic.Option;

/**
 *
 * @author sinantie
 */
public class ExtractGenerationMetricsOptions
{ 
//    final static String[] METRICS = {"bleu", "bleu_modified", "meteor", "ter"};
    final static String[] METRICS = {"bleu", "meteor"};
    final static int PAIRS = 2;
    public enum TypeOfPath {list, file}
    public enum TypeOfInput {percy, gabor}
    
    @Option(required=true) public String inputFile1;
    @Option(required=true) public String inputFile2;    
    @Option(required=true) public String outputFile;
    @Option public TypeOfInput inputFile1Type = TypeOfInput.percy;
    @Option public TypeOfInput inputFile2Type = TypeOfInput.percy;
    @Option(gloss="Deletes examples from either model if they don't exist in both") public boolean trimSize = false;
    @Option(gloss="Execute wilcoxon signed test using gnu octave (works on linux only)") public boolean calculateStatSig = false;
}
