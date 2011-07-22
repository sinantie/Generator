package induction.utils;

import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.EventType;
import induction.problem.event3.Field;
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
    private String outputFilename, inputFilename, header;
    private Map<String, Integer> eventTypesIndex;
    private String[] emptyVector;    
    public enum FeatureType {BINARY, COUNTS, VALUES};
    private FeatureType type;

    public ExtractFeatures(String outputFilename, String inputFilename, 
            String paramsFilename, FeatureType type)
    {
        this.outputFilename = outputFilename;
        this.inputFilename = inputFilename;
        this.type = type;
        emptyVector = createEmptyVector(loadEventTypes(paramsFilename));
    }

    private int loadEventTypes(String paramsFilename)
    {
        int totalNumberOfFields = 0;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(paramsFilename));
            ois.readObject(); // wordIndexer, don't need it
            ois.readObject(); // labelIndexer, don't need it
            EventType[] eventTypes = (EventType[]) ois.readObject(); // we only need this one
            eventTypesIndex = new HashMap<String, Integer>();
            StringBuilder str = new StringBuilder(); // create header
            for(EventType eventType: eventTypes)
            {
                String eventName = eventType.getName();
                for(int i = 0; i < eventType.getF(); i++)
                    str.append(eventName).append("_").append(eventType.fieldToString(i)).append(",");
//                    str.append(eventName).append(",");
                    
                // compute total number of elements: ~|eventTypes|*|fields_per_eventType|
                eventTypesIndex.put(eventName, totalNumberOfFields);
                totalNumberOfFields += eventType.getF();
//                totalNumberOfFields++;
            }
            str.append("text_length").append("\n");
            header = str.toString();
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
            String key = null;
            String example[];
            StringBuilder str = new StringBuilder();
            for(String line : Utils.readLines(inputFilename))
            {
                if(line.startsWith("Example_"))
                {
                    if(key != null) // only for the first example
                    {                        
                        example = Event3Model.extractExampleFromString(str.toString());
                        fos.append(extractFeatures(example[1], example[2]) + "\n");
                        str = new StringBuilder();
                    }
                    key = line;
                } // if
                str.append(line).append("\n");
            }  // for
            // don't forget last example
            example = Event3Model.extractExampleFromString(str.toString());
            fos.append(extractFeatures(example[1], example[2]));
            fos.close();
        }
        catch(IOException ioe) {}
    }
    
    private String extractFeatures(String text, String events)
    {
        String[] vector = Arrays.copyOf(emptyVector, emptyVector.length);
        for(String line : events.split("\n"))
        {
            fillVector(vector, line);
        }
        // put the text length as label
        return Arrays.toString(vector).replaceAll("[\\[\\] ]", "") + "," + text.split(" ").length;
    }

    private void fillVector(String[] vector, String event)
    {
        String[] tokens = event.split("\t");
        int index = eventTypesIndex.get(tokens[1].split(":")[1]); // 2nd token holds the type (.type:xxx)
//        vector[index] = type == FeatureType.BINARY ? "1" : String.valueOf(Integer.valueOf(vector[index]) + 1);;

        for(int i = 2; i < tokens.length; i++)
        {
            int currentIndex = index + i - 2;
            String value = tokens[i].split(":")[1];
            if(!value.equals("--"))
            {
                if(type == FeatureType.BINARY)
                    vector[currentIndex] = "1";
                else if(type == FeatureType.COUNTS)
                    vector[currentIndex] = String.valueOf(Integer.valueOf(vector[currentIndex]) + 1);
                else if(type == FeatureType.VALUES)
                    // account only for the first event of this eventType
                    if(vector[currentIndex].equals("--"))
                    {
                        vector[currentIndex] = value;
                    }
            } // if
        } // for
    }

    private String[] createEmptyVector(int numberOfElements)
    {        
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

    private String header(EventType[] eventTypes)
    {
        return "";
    }
  
    public static void main(String[] args)
    {
        String paramsFilename = "results/output/atis/alignments/model_3/"
                + "15_iter_no_null_no_smooth_STOP/stage1.params.obj";
//        String outputFilename = "data/atis/train/atis5000.sents.full.counts.features.csv";
//        String inputFilename = "data/atis/train/atis5000.sents.full";
        String outputFilename = "data/atis/test/atis-test.txt.counts.features.csv";
        String inputFilename = "data/atis/test/atis-test.txt";
        FeatureType type = FeatureType.COUNTS;
        ExtractFeatures ef = new ExtractFeatures(outputFilename, inputFilename, paramsFilename, type);
        ef.execute();
    }
}
