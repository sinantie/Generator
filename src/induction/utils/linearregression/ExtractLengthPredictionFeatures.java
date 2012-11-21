package induction.utils.linearregression;

import fig.basic.IOUtils;
import induction.Utils;
import induction.problem.event3.CatField;
import induction.problem.event3.EventType;
import induction.problem.event3.Field;
import induction.problem.event3.NumField;
import induction.utils.linearregression.LinearRegressionOptions.FeatureType;
import induction.utils.linearregression.LinearRegressionOptions.FieldType;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extract features from events String and append them in CSV format.
 * Each line of the feature vector corresponds to the fields of all the eventTypes.
 * Each element of the vector may take (1) a binary value if the particular field
 * of the event has a value other than '--' at least once. A variant (2) is
 * to put the number of times the particular field has occurred in the dataset,
 * as a single eventType may be accounted in the input more than once.
 * Another option is (3) to put the value of the particular field (this cannot
 * take into account datasets which have more than one events of the same eventType)
 * @author konstas
 */
public class ExtractLengthPredictionFeatures
{
    private String outputFilename, inputFilename, header;
    private Map<String, Integer> eventTypesIndex;
    private String[] emptyVector;    
    
    private FeatureType type;
    private boolean examplesInOneFile;
    private int startIndex, vectorLength;
    private List<Feature> features;

    /**
     * Constructor for stand-alone use. In short, copies parameters and
     * creates an empty vector of the correct size.
     * @param outputFilename
     * @param inputFilename
     * @param paramsFilename
     * @param type
     * @param examplesInOneFile
     * @param startIndex
     */
    public ExtractLengthPredictionFeatures(String outputFilename, String inputFilename, 
            String paramsFilename, FeatureType type, boolean examplesInOneFile, int startIndex)
    {
        this(paramsFilename, type, startIndex);
        this.outputFilename = outputFilename;
        this.inputFilename = inputFilename;
        this.examplesInOneFile = examplesInOneFile;        
    }

    public ExtractLengthPredictionFeatures(String paramsFilename, FeatureType type, int startIndex)
    {
        this.type = type;
        this.startIndex = startIndex;
        this.vectorLength = loadEventTypes(paramsFilename);
        emptyVector = createEmptyVector(vectorLength);
    }

    public int getVectorLength()
    {
        return vectorLength;
    }

    private int loadEventTypes(String paramsFilename)
    {
        int totalNumberOfFields = 0;
        try
        {
            ObjectInputStream ois = IOUtils.openObjIn(paramsFilename);
            ois.readObject(); // wordIndexer, don't need it
            ois.readObject(); // labelIndexer, don't need it
            EventType[] eventTypes = (EventType[]) ois.readObject(); // we only need this one
            eventTypesIndex = new HashMap<String, Integer>();
            StringBuilder headStr = new StringBuilder(); // create header
            features = new ArrayList<Feature>();
            for(EventType eventType: eventTypes)
            {
                String eventName = eventType.getName();                
                for(int i = 0; i < eventType.getF(); i++)
                {
                    String attrName = eventName + "_" + eventType.fieldToString(i);
                    headStr.append(attrName).append(",");                    
                    features.add(new Feature(attrName, eventType.getFields()[i]));
                }                
                // compute total number of elements: ~|eventTypes|*|fields_per_eventType|
                eventTypesIndex.put(eventName, totalNumberOfFields);
                totalNumberOfFields += eventType.getF();
            }
            headStr.append("text_length").append("\n");
            header = headStr.toString();
            features.add(new Feature("text_length", FieldType.NUM));
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ paramsFilename);
            ioe.printStackTrace();
        }
        finally
        {
            return totalNumberOfFields;
        }
    }

    public void execute()
    {
        try
        {            
            FileWriter fos = new FileWriter(outputFilename);
            fos.append(header);
            List<String[]> examples = Utils.readEvent3Examples(inputFilename, examplesInOneFile);
            for(String[] example : examples)
            {
                fos.append(extractFeatures(example[1], example[2]));
            }                  
            fos.close();
        }
        catch(IOException ioe) {}
    }
    
    private String extractFeatures(String text, String events)
    {        
        // put the text length as label
        return extractFeatures(events) + "," + text.split("[ \n]").length;
    }

    public String extractFeatures(String events)
    {
        String[] vector = Arrays.copyOf(emptyVector, emptyVector.length);
        for(String line : events.split("\n"))
        {
            fillVector(vector, line);
        }
        return Arrays.toString(vector).replaceAll("[\\[\\] ]", "");
    }

    private void fillVector(String[] vector, String event)
    {
        String[] tokens = event.split("\t");
        int index = eventTypesIndex.get(tokens[findToken(tokens, "type")].split(":")[1]); // 2nd token holds the type (.type:xxx)
//        vector[index] = type == FeatureType.binary ? "1" : String.valueOf(Integer.valueOf(vector[index]) + 1);;

        for(int i = startIndex; i < tokens.length; i++)
        {
            int currentIndex = index + i - startIndex;
            String[] subTokens = tokens[i].split(":");
            String value = subTokens.length > 1 ? subTokens[1] : "--";
            if(!value.equals("--"))
            {
                if(type == FeatureType.binary)
                    vector[currentIndex] = "1";
                else if(type == FeatureType.counts)
                    vector[currentIndex] = String.valueOf(Integer.valueOf(vector[currentIndex]) + 1);
                else if(type == FeatureType.values)
                    // account only for the first event of this eventType
                    if(vector[currentIndex].equals("--"))
                    {
                        vector[currentIndex] = value;
                    }
            } // if
        } // for
    }

    private int findToken(String[] tokens, String key)
    {
        for(int i = 0; i < tokens.length; i++)
            if(tokens[i].contains(key))
                return i;
        return -1;
    }
    private String[] createEmptyVector(int numberOfElements)
    {        
        String[] out = new String[numberOfElements];
        // Variant (1) - binary values
        if(type==FeatureType.binary || type==FeatureType.counts)
        {
            Arrays.fill(out, "0");
        }
        else if (type==FeatureType.values)
        {
            Arrays.fill(out, "--");
        }
        return out;

    }

    public String getHeader()
    {
        return header;
    }

    public List<Feature> getFeatures()
    {
        return features;
    }

    public class Feature
    {
        String name;
        FieldType type;
        List<String> values = new ArrayList<String>();

        public Feature(String name, Field field)
        {
            this.name = name;
            addValues(field);
        }

        public Feature(String name, FieldType type)
        {
            this.name = name;
            this.type = type;            
        }

        private void addValues(Field field)
        {
            if (field instanceof NumField)
            {
                type = FieldType.NUM;
            }
            else // we currently support categorical fields only
            {
                type = FieldType.CAT;
                CatField catField = (CatField)field;
                for(String value : catField.getIndexer().getObjects())
                    values.add(!value.equals("") ? value : "--");
            }
        }

        public FieldType getType()
        {
            return type;
        }

        public String getName()
        {
            return name;
        }

        public List<String> getValues()
        {
            return values;
        }
    }

    public static void main(String[] args)
    {
        String paramsFilename, outputFilename, inputFilename;
        int startIndex;
        if(args.length > 0)
        {
            paramsFilename = args[0];
            inputFilename = args[1];
            outputFilename = args[2];
            startIndex = Integer.valueOf(args[3]);
        }
        else
        {
    //        paramsFilename = "results/output/atis/alignments/model_3/"
    //                + "15_iter_no_null_no_smooth_STOP/stage1.params.obj";
    //        outputFilename = "data/atis/train/atis5000.sents.full.counts.features.csv";
    //        inputFilename = "data/atis/train/atis5000.sents.full";
    //        outputFilename = "data/atis/test/atis-test.txt.counts.features.csv";
    //        inputFilename = "data/atis/test/atis-test.txt";
            paramsFilename = "results/output/weatherGov/alignments/"
                    + "model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj";
    //        String outputFilename = "gaborLists/trainListPathsGabor.values.features.csv";
    //        String inputFilename = "gaborLists/trainListPathsGabor";
            outputFilename = "gaborLists/genEvalListPathsGabor.values.features.csv";
            inputFilename = "gaborLists/genEvalListPathsGabor";
            startIndex = 4; // 4 for weatherGov, 2 for atis
        }
        FeatureType type = FeatureType.values;
        boolean examplesInOneFile = true;
        
        ExtractLengthPredictionFeatures ef = new ExtractLengthPredictionFeatures(outputFilename, inputFilename, 
                paramsFilename, type, examplesInOneFile, startIndex);
        ef.execute();
    }
}
