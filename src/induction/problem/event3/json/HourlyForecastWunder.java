package induction.problem.event3.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author sinantie
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyForecastWunder
{
    JsonWrapper.MetricSystem system;
    List<Prediction> predictions;

    @JsonProperty("system")   
    public JsonWrapper.MetricSystem getSystem()
    {
        return system;
    }      
    
    @JsonProperty("hourly_forecast")
    public List<Prediction> getPredictions()
    {
        return predictions;
    }    
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prediction
    {
        Time time;
        Measure temperature;
        String condition;
        Integer skyCover;
        Measure windSpeed;
        Direction windDir;
        Measure windChill;
        Integer chanceOfPrecipitation;

        @JsonProperty("FCTTIME")
        public Time getTime()
        {
            return time;
        }
        
        @JsonProperty("temp")
        public Measure getTemperature()
        {
            return temperature;
        }
               
        public String getCondition()
        {
            return condition;
        }

        @JsonProperty("sky")
        public Integer getSkyCover()
        {
            return skyCover;
        }
        
        @JsonProperty("wspd")
        public Measure getWindSpeed()
        {
            return windSpeed;
        }

        @JsonProperty("wdir")
        public Direction getWindDir()
        {
            return windDir;
        }
        
        @JsonProperty("windchill")
        public Measure getWindChill()
        {
            return windChill;
        }

        @JsonProperty("pop")
        public Integer getChanceOfPrecipitation()
        {
            return chanceOfPrecipitation;
        }        
    }
        
    public static class Measure
    {
        Integer english, metric;

        public Integer getEnglish()
        {
            return english;
        }        
        
        public Integer getMetric()
        {
            return metric;
        }  
    }
        
    public static class Direction
    {
        String dir;
        Integer degrees;

        public String getDir()
        {
            return dir;
        }
    
        public Integer getDegrees()
        {
            return degrees;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Time
    {
        Integer hour;
        String day;
        
        public Integer getHour()
        {
            return hour;
        }
        
        @JsonProperty("weekday_name")
        public String getDay()
        {
            return day;
        }
    }
}
