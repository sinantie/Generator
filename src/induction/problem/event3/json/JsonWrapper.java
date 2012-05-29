/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import induction.Options.JsonFormat;
import induction.Utils;
import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author konstas
 */
public class JsonWrapper
{
    public static final JsonResult ERROR_EVENTS = new JsonResult("Error", "Error reading events!");
    public static final JsonResult ERROR_EXPORT_JSON = new JsonResult("Error", "Error exporting json!");
    public static enum MetricSystem {metric, english};    
    private Indexer<String> wordIndexer;
    private final Indexer<String> days = new Indexer();
    private String[] name;
    private String[] eventsString;
    private List<int[]> text;
    private int numberOfOutputs;    
    public static ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
    private boolean processUrl = true;
    public JsonWrapper(String query, JsonFormat jsonFormat, Indexer<String> wordIndexer, String... args)
    {
        this.wordIndexer = wordIndexer;
        if(jsonFormat == JsonFormat.wunderground)
        {
            days.add("Monday");days.add("Tuesday");days.add("Wednesday");days.add("Thursday");days.add("Friday");days.add("Saturday");days.add("Sunday");
            // 2 12-hour forecasts            
            numberOfOutputs = 2;
            eventsString = new String[numberOfOutputs]; 
            text = new ArrayList<int[]>(numberOfOutputs);
            name = new String[numberOfOutputs];
            Properties prop = new Properties();
            try
            {
                prop.load(getClass().getResourceAsStream("wunderground.properties"));
                // construct url
                if(query.contains("/q/"))
                {
                    processUrl = true;
                    String apiKey = prop.getProperty("api.key");
                    String apiUrl = prop.getProperty("api.url");
                    String apiQueryUrl = prop.getProperty("api.queryUrl");
                    String url = apiUrl + apiKey + apiQueryUrl + query;                
                    processWundergroundJsonFile(url, args[0]);
                }
                else // query is a string                                
                {
                    processUrl = false;
                    processWundergroundJsonFile(Utils.readFileAsString(query), args[0]);
                }
                
                
            }
            catch(IOException ioe)
            {
                LogInfo.error(ioe);
            }            
        }
    }

    public String[] getEventsString()
    {
        return eventsString;
    }

    public int getNumberOfOutputs()
    {
        return numberOfOutputs;
    }

    public boolean hasText()
    {
        return !text.isEmpty();
    }

    public List<int[]> getText()
    {
        return text;
    }

    public String[] getName()
    {
        return name;
    }

    private boolean processWundergroundJsonFile(String exampleUrl, String system)
    {        
        try 
        {
            HourlyForecastWunder forecast;
            if(processUrl)
                forecast = mapper.readValue(new URL(exampleUrl), HourlyForecastWunder.class);
            else
                forecast = mapper.readValue(exampleUrl, HourlyForecastWunder.class);
            forecast.setSystem(system.equals("metric") ? MetricSystem.metric : MetricSystem.english);
            List<Prediction> predictions = forecast.getPredictions();
            // we are going to grab 2 12-hour forecasts in total 
            Object[] forecasts = new Object[2];
            // get current hour to determine whether the first 12-hour part forecast is
            // going to be day or night (the boundary is 1700 hours)
            int currentHour = predictions.get(0).getTime().getHour();            
            String dayName = predictions.get(0).getTime().getDay();
            if(currentHour >= PercyForecast.DAY_BEGIN && currentHour < PercyForecast.NIGHT_BEGIN)
            {
                // day period                
                int dayBeginIndex = 0;
                int dayEndIndex = PercyForecast.DAY_END - currentHour;
                forecasts[0] = new PercyForecast(predictions.subList(dayBeginIndex, dayEndIndex + 1), PercyForecast.PeriodOfDay.day, forecast.system);
                name[0] = dayName;
                // night period
                int nightBeginIndex = dayEndIndex - (PercyForecast.DAY_END - PercyForecast.NIGHT_BEGIN);
                int nightEndIndex = nightBeginIndex + (PercyForecast.NIGHT_END - PercyForecast.NIGHT_BEGIN);
                forecasts[1] = new PercyForecast(predictions.subList(nightBeginIndex, nightEndIndex + 1), PercyForecast.PeriodOfDay.night, forecast.system);
                name[1] = dayName + " Night";
            }
            else
            {
                // night period
                int nightBeginIndex = 0;
                int nightEndIndex = (currentHour < PercyForecast.DAY_BEGIN ? PercyForecast.NIGHT_END_ALT : PercyForecast.NIGHT_END) - currentHour;
                forecasts[0] = new PercyForecast(predictions.subList(nightBeginIndex, nightEndIndex + 1), PercyForecast.PeriodOfDay.night, forecast.system);
                name[0] = dayName + " Night";
                // day period
                int dayBeginIndex = nightEndIndex;
                int dayEndIndex =dayBeginIndex + (PercyForecast.DAY_END - PercyForecast.DAY_BEGIN);
                forecasts[1] = new PercyForecast(predictions.subList(dayBeginIndex, dayEndIndex + 1), PercyForecast.PeriodOfDay.day, forecast.system);
                // roll dayName
                name[1] = days.getObject((days.getIndex(dayName) + 1) % 7);
            }
            // copy event strings
            for(int i = 0; i < forecasts.length; i++)
                eventsString[i] = ((PercyForecast)forecasts[i]).getForecastEvents();
//            System.out.println(((PercyForecast)forecasts[0]).getForecastEvents());
//            System.out.println("----------");
//            System.out.println(((PercyForecast)forecasts[1]).getForecastEvents());
            return true;
        }
        
        catch (Exception ex) 
        {
            LogInfo.error(ex);
            ex.printStackTrace();
        }               
        return false;
        // process text if it exists
//        text[i] = wordIndexer.getIndex(word);
    }
    
}
