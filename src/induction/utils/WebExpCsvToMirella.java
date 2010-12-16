package induction.utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author konstas
 */
public class WebExpCsvToMirella
{
    private boolean header = true;
    private BufferedReader inputReader;    
    private Map<String, FileOutputStream> filesMap = new HashMap<String, FileOutputStream>();
    private Map<String, CsvEntry> entriesMap = new HashMap<String, CsvEntry>();
    private Map<String,Integer> fieldsMap = new HashMap();

    public WebExpCsvToMirella(String inputFile, String outputPath, String[] measures, boolean append)
    {
        try
        {
            this.inputReader = new BufferedReader(new FileReader(inputFile));
            // create a file and an entry for each measure
            for(String measure : measures)
            {
                filesMap.put(measure, new FileOutputStream(outputPath + measure + ".csv", append));
                entriesMap.put(measure, new CsvEntry());
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
    }  

    public void setHeader(boolean header)
    {
        this.header = header;
    }

    public void execute()
    {
        try
        {
            if(header)
            {
                for(FileOutputStream fos : filesMap.values())
                    fos.write(writeLine(CsvEntry.Header()));
            }
            // parse fields names
            String[] inputFields = inputReader.readLine().split(";");
            for(int i = 0; i < inputFields.length; i++)
            {
                fieldsMap.put(readCsvValue(inputFields[i]), i);
            }
            String line = ""; String[] values;
            while( (line = inputReader.readLine()) != null )

            {
                values = line.split(";");
                if(values.length > 0) // avoid empty lines
                {
                    // n-lines for each measure and the last one and the last
                    // one contains the value 'target' for the field 'STIM_VALUE'
                    // this is the scenario id
                    String subjectId = readCsvValue(values[fieldsMap.get("SAVE_ID")]) + "_" +
                                       readCsvValue(values[fieldsMap.get("email")]);
                    String stimName = readCsvValue(values[fieldsMap.get("STIM_NAME")]);
                    // while we are in the measures lines
                    if(entriesMap.containsKey(stimName))
                    {
                        // set the subject id and rating for the corresponding
                        // entry
                        entriesMap.get(stimName).subjectId = subjectId;
                        entriesMap.get(stimName).rating = readCsvValue(values[fieldsMap.get("VALUE")]);
                    }
                    else // found the 'target' value
                    {
                        String scenarioCombo = readCsvValue(values[fieldsMap.get("IMPORT")]);
                        int splitIndex = scenarioCombo.lastIndexOf("_");
                        for(Entry<String, CsvEntry> entrySet : entriesMap.entrySet())
                        {
                            CsvEntry entry = entrySet.getValue();
                            entry.itemId = scenarioCombo.substring(0, splitIndex);
                            entry.systemId = scenarioCombo.substring(splitIndex + 1);
                            filesMap.get(entrySet.getKey()).write(writeLine(entry.toString()));
                        }
                    }
                }

            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private String readCsvValue(String value)
    {
        return value.substring(1, value.length() - 1);
    }

    private byte[] writeLine(String line)
    {
        return (line + "\n").getBytes();
    }
    public static void main(String[] args)
    {
        String inputFile = "../webexp2/mturk/robocup/csv/bgm-robocup4_clean.csv";
        String outputPath = "../webexp2/mturk/robocup/csv/bgm-robocup_";
        String[] measures = {"FluencyScore", "SemanticScore"};
        boolean append = true;
        WebExpCsvToMirella c = new WebExpCsvToMirella(inputFile, outputPath, measures, append);
        c.setHeader(false);
        c.execute();

    }

    
}

class CsvEntry
{
    String itemId, subjectId, systemId, rating;

    public CsvEntry()
    {
    }

    public CsvEntry(String itemId, String subjectId, String systemId, String rating)
    {
        this.itemId = itemId;
        this.subjectId = subjectId;
        this.systemId = systemId;
        this.rating = rating;
    }

    static String Header()
    {
        return "item-id,subject-id,system-id,rating";
    }

    @Override
    public String toString()
    {
        return String.format("%s,%s,%s,%s", itemId, subjectId, systemId, rating);
    }

}
