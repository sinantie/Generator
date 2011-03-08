package induction.utils;

import fig.basic.LogInfo;
import induction.Utils;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class SemanticLMPreprocessor extends LMPreprocessor
{
    private final boolean includeEvents, includeFieldNames;
    public SemanticLMPreprocessor(String targetFile, String sourceDir, int ngramSize,
                          SourceType type, String fileExtension, boolean includeEvents,
                          boolean includeValues)
    {
        super(targetFile, sourceDir, ngramSize, type, fileExtension);
        this.includeEvents = includeEvents;
        this.includeFieldNames = includeValues;
    }

    /**
     * Process an alignment file with full predictions. Take into account only
     * the lines that have a correct alignment. WARNING, it only words for
     * one event per prediction at the moment
     * @param path
     */
    @Override
    protected void processFile(String path)
    {
        try
        {
            String textOut;
            int counter = 0;
            for(String line : Utils.readLines(path))
            {
                // parse only the correctly aligned lines
                if(!line.contains("*"))
                {
                    textOut = HEADER;
                    String []chunks = line.split("\t");
                    if(chunks[1].contains("Pred")) // deal with the predicted chunk 
                    {
                        String chunk = chunks[2].replaceAll("\\[TRACK0\\]", "").trim(); // preprocess a bit
                        int index1 = chunk.indexOf(":"), index2 = chunk.indexOf("[");
                        String event = chunk.substring(0, index1).trim();
                        HashMap<String, String> fieldsMap = fieldsToMap(chunk.substring(index1+1, index2));
                        if(includeEvents)
                            textOut += event + " ";
                        String []fields = chunk.substring(index2+1,
                                chunk.lastIndexOf("]")).split("\\]");
                        for(String field : fields)
                        {
                            String[] tokens = field.split("\\["); // fields and values                            
                            if(includeFieldNames)
                                textOut += tokens[0] + " ";
                            if(!tokens[0].contains("none")) // add value
                            {   // prediction might be noisy, so get the correct field value from map
//                                textOut += fieldsMap.get(tokens[0].trim()) + " ";
                                int index3 = tokens[1].trim().indexOf("_");
                                textOut += tokens[1].trim().substring(0, index3) + " ";
                            }
                            else // add the (none) token as many times as the number of words
                            {
                                for(int i = includeFieldNames ? 1 : 0; i < tokens[1].split(" ").length; i++)
                                {
                                    textOut += "(none) ";
                                }
                            }

                        }
                    }
                    bos.write((textOut.trim().toLowerCase() + " </s>\n").getBytes());
                    counter++;
                }
            }
            System.out.println(String.format("Processed %d lines", counter));            
        }
        catch(IOException ioe)
        {
            LogInfo.error("Error reading file " + path);
        }
    }

    /**
     * Convert a string that contains fields and their values into a map.
     * @param in the input string that has the following format: field1=value1,field2=value2,...
     * @return
     */
    private HashMap<String, String> fieldsToMap(String in)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        for(String s: in.split(","))
        {
            String[] token = s.split("=");
            map.put(token[0], token[1]);
        }
        return map;
    }

    public static void main(String[] args)
    {
        String source = "results/output/robocup/"
                + "model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField_gold/fold4/"
                + "stage1.train.full-pred.9";
        String target = "robocupLM/robocup-semantic-fold4-noisy-3-gram.sentences";
        String fileExtension = "9";
        boolean tokeniseOnly = false;
        int ngramSize = 3;
        boolean includeEvents = false;
        boolean includeFieldNames = false;
        LMPreprocessor lmp = new SemanticLMPreprocessor(target, source, ngramSize,
                                              SourceType.PATH, fileExtension, 
                                              includeEvents, includeFieldNames);
        lmp.execute(tokeniseOnly);
    }
}
