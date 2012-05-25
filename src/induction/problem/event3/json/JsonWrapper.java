/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import fig.basic.Indexer;
import induction.Options.JsonFormat;

/**
 *
 * @author konstas
 */
public class JsonWrapper
{
    public static final String ERROR_EVENTS = "error_events";
    
    private Indexer<String> wordIndexer;
    private String name;
    private String eventsString;
    private int []text;
    
    public JsonWrapper(String example, JsonFormat jsonFormat, Indexer<String> wordIndexer)
    {
        this.wordIndexer = wordIndexer;
        if(jsonFormat == JsonFormat.wunderground)
            processWundergroundJsonFile(example);
    }

    public String getEventsString()
    {
        return eventsString;
    }

    public boolean hasText()
    {
        return text != null;
    }

    public int[] getText()
    {
        return text;
    }

    public String getName()
    {
        return name;
    }

    private boolean processWundergroundJsonFile(String example)
    {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        try 
        {
            HourlyForecastWunder forecast = mapper.readValue(example, HourlyForecastWunder.class);
            System.out.println("");
            return true;
        }
        
        catch (Exception ex) 
        {
            System.out.println(ex);
        }               
        return false;
        // process text if it exists
//        text[i] = wordIndexer.getIndex(word);
    }
    
}
