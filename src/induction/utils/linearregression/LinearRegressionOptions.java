package induction.utils.linearregression;

import fig.basic.Option;

/**
 *
 * @author sinantie
 */
public class LinearRegressionOptions
{
    public enum Mode {train, test};
    public enum FeatureType {binary, counts, values};
    public enum FieldType {CAT, NUM, STR};
    
    @Option(gloss="File containing db input. Used only if we want to extract features") public String inputFeaturesFile;
    @Option(gloss="File containing features interface CSV format") public String outputFeaturesFile;
    @Option(gloss="File containing the parameters of the generation model", required=true) public String paramsFile;
    @Option(gloss="File containing the parameters of the linear regression model", required=true) public String modelFile;    
    @Option(gloss="File containing input to be predicted") public String inputFile;    
    @Option(required=true) public int startIndex;
    @Option(required=true) public Mode mode = Mode.test;
    @Option(required=true) public FeatureType type = FeatureType.counts;
    @Option(gloss="Examples in one file") public boolean examplesInSingleFile = false;
    @Option(gloss="Extract features and then train") public boolean extractFeatures = false;
    @Option(gloss="Write model file to disk after training") public boolean saveModel = false;
        
}
