package induction.utils;

import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
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
public class ExtractFeatures
{
    private String outputFilename, inputFilename;
    private Map<String, EventType> eventTypesMap;
    private String[] emptyVector;
    public enum FeatureType {BINARY, COUNTS, VALUES};
    private FeatureType type;

    public ExtractFeatures(String outputFilename, String inputFilename, 
            String paramsFilename, FeatureType type)
    {
        this.outputFilename = outputFilename;
        this.inputFilename = inputFilename;
        loadEventTypes(paramsFilename);
        emptyVector = createEmptyVector();
        this.type = type;
    }

    private void loadEventTypes(String paramsFilename)
    {
        try
        {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(paramsFilename));
            ois.readObject(); // wordIndexer, don't need it
            ois.readObject(); // labelIndexer, don't need it
            EventType[] eventTypes = (EventType[]) ois.readObject(); // we only need this one
            eventTypesMap = new HashMap<String, EventType>();
            for(EventType eventType: eventTypes)
                eventTypesMap.put(eventType.getName(), eventType);
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ paramsFilename);
            ioe.printStackTrace();
        }
    }

    public void execute()
    {
        try
        {
            FileWriter fos = new FileWriter(outputFilename);
            fos.append(header());
            String key = null;
            StringBuilder str = new StringBuilder();
            for(String line : Utils.readLines(inputFilename))
            {
                if(line.startsWith("Example_"))
                {
                    if(key != null) // only for the first example
                    {
                        // parse events part only from example and extract features from it
                        fos.append(extractFeatures(Event3Model.extractExampleFromString(str.toString())[2]));
                        str = new StringBuilder();
                    }
                    key = line;
                } // if
                str.append(line).append("\n");
            }  // for
            // don't forget last example
            fos.append(extractFeatures(Event3Model.extractExampleFromString(str.toString())[2]));
        }
        catch(IOException ioe) {}
    }

    /**
     * Extract features from events String and append them in csv format.
     * Each line of the feature vector corresponds to the fields of all the eventTypes.
     * Each element of the vector may take (1) a binary value if the particular field
     * of the event has a value other than '--' at least once. A variant (2) is
     * to put the number of times the particular field has occured in the dataset,
     * as a single eventType may be accounted in the input more than once.
     * Another option is (3) to put the value of the particular field (this cannot
     * take into account datasets which have more than one events of the same eventType)
     *
     * @param events
     * @return
     */
    private String extractFeatures(String events)
    {
//        StringBuilder str = new StringBuilder();
        String[] vector = Arrays.copyOf(emptyVector, emptyVector.length);
        for(String line : events.split("\n"))
        {
            fillVector(vector, line);
        }
        return Arrays.toString(vector);
        //return str.toString();
    }

    private void fillVector(String[] vector, String event)
    {

    }

    private String[] createEmptyVector()
    {
        // compute total number of elements: ~|eventTypes|*|fields_per_eventType|
        int numberOfElements = 0;
        for(EventType eventType : eventTypesMap.values())
        {
            numberOfElements += eventType.getF();
        }
        String[] out = new String[numberOfElements];
        // Variant (1) - binary values
        if(type==FeatureType.BINARY || type==FeatureType.COUNTS)
        {
            Arrays.fill(out, "0");
        }
        else if (type==FeatureType.VALUES)
        {
            Arrays.fill(out, "--");
        }
        return out;

    }
    private String header()
    {
        return "";
    }

    public static void main(String[] args)
    {
        String paramsFilename = "results/output/atis/alignments/model_3/"
                + "15_iter_no_null_no_smooth_STOP/stage1.params.obj";
        String outputFilename = "data/atis/train/atis5000.sents.full.features";
        String inputFilename = "data/atis/train/atis5000.sents.full";
        FeatureType type = FeatureType.BINARY;
//        String inputFilename = "../atis/lambda/percy/test/atis-test.txt";
        ExtractFeatures ef = new ExtractFeatures(outputFilename, inputFilename, paramsFilename, type);
        ef.execute();
    }
}
