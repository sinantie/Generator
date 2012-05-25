package induction.problem.event3.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sinantie
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyForecastWunder
{    
    List<Hour> hourly_forecast;

    public List<Hour> getHourly_forecast()
    {
        return hourly_forecast;
    }

    public void setHourly_forecast(List<Hour> hourly_forecast)
    {
        this.hourly_forecast = hourly_forecast;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hour
    {
        Map FCTTIME;
        Measure temp;
        String condition;
        Integer sky;
        Measure wspd;
        Direction wdir;
        Measure windchill;
        Integer pop;

        public Map getFCTTIME()
        {
            return FCTTIME;
        }

        public void setFCTTIME(Map FCTTIME)
        {
            this.FCTTIME = FCTTIME;
        }
                        
        public Measure getTemp()
        {
            return temp;
        }
        
        public void setTemp(Measure temp)
        {
            this.temp = temp;
        }
        
        public String getCondition()
        {
            return condition;
        }
        
        public void setCondition(String condition)
        {
            this.condition = condition;
        }

        public Integer getSky()
        {
            return sky;
        }

        public void setSky(Integer sky)
        {
            this.sky = sky;
        }

        public Measure getWspd()
        {
            return wspd;
        }

        public void setWspd(Measure wspd)
        {
            this.wspd = wspd;
        }

        public Direction getWdir()
        {
            return wdir;
        }

        public void setWdir(Direction wdir)
        {
            this.wdir = wdir;
        }

        public Measure getWindchill()
        {
            return windchill;
        }

        public void setWindchill(Measure windchill)
        {
            this.windchill = windchill;
        }

        public Integer getPop()
        {
            return pop;
        }

        public void setPop(Integer pop)
        {
            this.pop = pop;
        }
    }
        
    public static class Measure
    {
        Integer english, metric;

        public Integer getEnglish()
        {
            return english;
        }

        public void setEnglish(Integer english)
        {
            this.english = english;
        }
        
        public Integer getMetric()
        {
            return metric;
        }

        public void setMetric(Integer metric)
        {
            this.metric = metric;
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

        public void setDir(String dir)
        {
            this.dir = dir;
        }

        public Integer getDegrees()
        {
            return degrees;
        }

        public void setDegrees(Integer degrees)
        {
            this.degrees = degrees;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Time
    {
        Integer hour;

        public Integer getHour()
        {
            return hour;
        }

        public void setHour(Integer hour)
        {
            this.hour = hour;
        }        
    }
}
