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
    private final boolean includeEvents, includeFieldNames, noise, useCorrectPredictionsOnly;
    public SemanticLMPreprocessor(String targetFile, String sourceDir, int ngramSize,
                          SourceType type, String fileExtension, boolean includeEvents,
                          boolean includeValues, boolean noise, boolean useCorrectPredictionsOnly)
    {
        super(targetFile, sourceDir, ngramSize, type, fileExtension);
        this.includeEvents = includeEvents;
        this.includeFieldNames = includeValues;
        this.noise = noise;
        this.useCorrectPredictionsOnly = useCorrectPredictionsOnly;
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
                if(!useCorrectPredictionsOnly || !line.contains("*"))
                {
                    textOut = HEADER;
                    String []chunks = line.split("\t");
                    int pos = 2;
                    if(chunks[1].contains("Pred")) // deal with the predicted chunk 
                    {
                        while(pos < chunks.length && !chunks[pos].contains("True"))
                        {
                            String chunk = chunks[pos].replaceAll("[\\*]?\\[TRACK0\\]", "").trim(); // preprocess a bit
                            int index1 = chunk.indexOf(":"), index2 = chunk.indexOf("[");
                            if(index1 == -1) // none event
                            {
                                if(includeEvents)
                                    textOut += "none_e ";
                                pos++;
                                continue;
                            }
                                
                            String event = chunk.substring(0, index1).trim();
                            HashMap<String, String> fieldsMap = fieldsToMap(chunk.substring(index1+1, index2));
                            if(includeEvents)
                                textOut += event.substring(0, event.indexOf("(")) + " ";
                            String []fields = chunk.substring(index2+1,
                                    chunk.lastIndexOf("]")).split("\\]");
                            for(String field : fields)
                            {
                                String[] tokens = field.split("\\["); // fields and values
                                if(includeFieldNames)
                                    textOut += tokens[0].contains("none") ? "none_f " : tokens[0] + " ";
                                if(!tokens[0].contains("none")) // add value
                                {   // prediction might be noisy, so get the correct field value from map
                                    if(!noise)
                                        textOut += fieldsMap.get(tokens[0].trim()) + " ";
                                    else
                                    {
                                        int index3 = tokens[1].trim().indexOf("_");
                                        textOut += tokens[1].trim().substring(0, index3) + " ";
                                    }
                                }
                                else if(!includeFieldNames)// add the (none) token as many times as the number of words
                                {
                                    for(int i = 0; i < tokens[1].split(" ").length; i++)
                                    {
                                        textOut += "(none) ";
                                    }
                                } // else
                            } // for
                            pos++;
                        } // while                        
                    } // if
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
            if(token.length == 2)
                map.put(token[0], isNumber(token[1]) ? "<num>" : token[1]);
        }
        return map;
    }

    private boolean isNumber(String s)
    {
        return s.matches("-\\p{Digit}+|" + // negative numbers
                             "-?\\p{Digit}+\\.\\p{Digit}+") || // decimals
                             (s.matches("\\p{Digit}+"));
    }
    public static void main(String[] args)
    {
        String source = "results/output/weatherGov/alignments/"
                + "/gold_staged/trainGabor_no_times/"
                + "stage1.test.full-pred.0";
        String target = "weatherGovLM/weather-semantic-full-no-times-proc-3-gram.sentences";
        String fileExtension = "0";
        boolean tokeniseOnly = false;
        int ngramSize = 3;
        boolean includeEvents = true;
        boolean includeFieldNames = true;
        boolean noise = false;
        boolean useCorrectPredictionsOnly = false;
        LMPreprocessor lmp = new SemanticLMPreprocessor(target, source, ngramSize,
                                              SourceType.PATH, fileExtension, 
                                              includeEvents, includeFieldNames, noise,
                                              useCorrectPredictionsOnly);
        lmp.execute(tokeniseOnly);
    }
}
