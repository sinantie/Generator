package induction;

import induction.utils.ExtractFeatures;
import induction.utils.ExtractFeatures.Feature;
import induction.utils.ExtractFeatures.FeatureType;
import java.io.FileOutputStream;
import java.util.ArrayList;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;

/**
 *
 * @author sinantie
 */
public class WekaWrapper
{
    private String modelFilename;
    private Classifier model;
    private Instances dataset;
    private int numberOfAttributes;
    private ExtractFeatures featureExtractor;
    private FeatureType featureType;
    public enum Mode {TRAIN, TEST};
    private Mode mode;

    public WekaWrapper(String paramsFilename, String modelFilename, int startIndex,
            FeatureType featureType, Mode mode)
    {
        try
        {            
            // load featureExtractor
            featureExtractor = new ExtractFeatures(paramsFilename, featureType, startIndex);
            this.numberOfAttributes = featureExtractor.getVectorLength();
            this.featureType = featureType;
            this.mode = mode;
            this.modelFilename = modelFilename;
            // load weka model (test mode)
            if(mode == Mode.TEST)
                model = (Classifier) SerializationHelper.read(modelFilename);
            // create host dataset
            String[] header = featureExtractor.getHeader().split(",");
            ArrayList<Attribute> attrs = new ArrayList<Attribute>(header.length);
            for(Feature feature : featureExtractor.getFeatures())
            {
                switch(featureType)
                {
                    case VALUES: // treat nominal values (if they exist) differently
                    {
                        switch(feature.getType())
                        {
                            case NUM: attrs.add(new Attribute(feature.getName())); break;
                            case CAT: attrs.add(new Attribute(feature.getName(),
                                    feature.getValues())); break;
                        }

                    } break;
                    case COUNTS: case BINARY: default: attrs.add(new Attribute(feature.getName()));
                } // switch
            } // for
            dataset = new Instances("pred", attrs, 1);
            dataset.setClassIndex(numberOfAttributes);            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void train(String datasetFilename, boolean serialise)
    {
        String[] lines = Utils.readLines(datasetFilename);
        int i = 1; // skip legend
        try
        {            
            for(i = 1; i < lines.length; i++) // skip legend
            {
                dataset.add(createFeatureVector(lines[i].split(","), true));
            }
            model = (Classifier) new LinearRegression();
            ((LinearRegression)model).setRidge(1.0e-10);
            model.buildClassifier(dataset);
            if(serialise)
            {                
                SerializationHelper.write(new FileOutputStream(modelFilename), model);
            }
        }
        catch(Exception e)
        {
            System.err.println("Error in line " + i + ": " + lines[i]);
            e.printStackTrace();
        }
    }

    public double predict(String input)
    {                        
        try
        {            
            return model.classifyInstance(
                    createFeatureVector(
                        featureExtractor.extractFeatures(input).split(","), false));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return -1.0;
    }

    private Instance createFeatureVector(String[] extractedValues, boolean label)
    {
        Instance featureVector = new SparseInstance(label ? numberOfAttributes + 1 : numberOfAttributes);
        featureVector.setDataset(dataset);
        for(int i = 0; i < numberOfAttributes; i++)
        {
            String s = extractedValues[i];
            switch(featureType)
            {
                case COUNTS: featureVector.setValue(i, Integer.valueOf(s)); break;
                case VALUES: // treat nominal values (if they exist) differently
                {
                    switch(featureExtractor.getFeatures().get(i).getType())
                    {
                        case NUM: featureVector.setValue(i, Integer.valueOf(s)); break;
                        case CAT: featureVector.setValue(i, s); break;
                    }
                } break;
            }
        } // for
        if(label)
            featureVector.setValue(numberOfAttributes, Integer.valueOf(extractedValues[numberOfAttributes]));
        return featureVector;
    }
}
