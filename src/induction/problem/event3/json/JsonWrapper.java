/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import induction.Options.JsonFormat;
import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.util.List;

/**
 *
 * @author konstas
 */
public class JsonWrapper
{
    public static final String ERROR_EVENTS = "error_events";
    public static enum MetricSystem {metric, english};
    
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
            List<Prediction> predictions = forecast.getPredictions();
            // we are going to grab 2 12-hour forecasts in total 
            Object[] forecasts = new Object[2];
            // get current hour to determine whether the first 12-hour part forecast is
            // going to be day or night (the boundary is 1700 hours)
            int currentHour = predictions.get(0).getTime().getHour();            
            if(currentHour >= PercyForecast.DAY_BEGIN && currentHour < PercyForecast.NIGHT_BEGIN)
            {
                // day period                
                int dayBeginIndex = 0;
                int dayEndIndex = PercyForecast.DAY_END - currentHour;
                forecasts[0] = new PercyForecast(predictions.subList(dayBeginIndex, dayEndIndex + 1), PercyForecast.PeriodOfDay.day, forecast.system);
                // night period
                int nightBeginIndex = dayEndIndex - (PercyForecast.DAY_END - PercyForecast.NIGHT_BEGIN);
                int nightEndIndex = nightBeginIndex + (PercyForecast.NIGHT_END - PercyForecast.NIGHT_BEGIN);
                forecasts[1] = new PercyForecast(predictions.subList(nightBeginIndex, nightEndIndex + 1), PercyForecast.PeriodOfDay.night, forecast.system);
            }
            else
            {
                // night period
                int nightBeginIndex = 0;
                int nightEndIndex = PercyForecast.NIGHT_END - currentHour;
                forecasts[0] = new PercyForecast(predictions.subList(nightBeginIndex, nightEndIndex + 1), PercyForecast.PeriodOfDay.night, forecast.system);
                // day period
                int dayBeginIndex = nightEndIndex;
                int dayEndIndex =dayBeginIndex + (PercyForecast.DAY_END - PercyForecast.DAY_BEGIN);
                forecasts[1] = new PercyForecast(predictions.subList(dayBeginIndex, dayEndIndex + 1), PercyForecast.PeriodOfDay.day, forecast.system);
            }
            
            System.out.println(((PercyForecast)forecasts[0]).getForecastEvents());
            System.out.println("----------");
            System.out.println(((PercyForecast)forecasts[1]).getForecastEvents());
            return true;
        }
        
        catch (Exception ex) 
        {
            LogInfo.error(ex);
        }               
        return false;
        // process text if it exists
//        text[i] = wordIndexer.getIndex(word);
    }
    
}
