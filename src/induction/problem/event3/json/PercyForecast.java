package induction.problem.event3.json;

import fig.basic.LogInfo;
import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import weka.core.Utils;

/**
 *
 * @author konstas
 */
public class PercyForecast
{        
    public static enum PeriodOfDay {day, night}; 
    public static int DAY_BEGIN = 6, 
                      DAY_END = 21, 
                      NIGHT_BEGIN = 17,
                      NIGHT_END = 30,
                      NIGHT_END_ALT = 6;
    private final Interval PERIOD_ALL_DAY = new Interval(6, 21),
                                 PERIOD_D_EARLY_MORNING = new Interval(6, 9),
                                 PERIOD_D_MORNING = new Interval(6, 13),
                                 PERIOD_D_MORNING_EVENING = new Interval(9, 21),
                                 PERIOD_D_AFTERNOON_EVENING = new Interval(13, 21),
            
                                 PERIOD_ALL_NIGHT = new Interval(17, 06, "17", "30"),
                                 PERIOD_N_EVENING = new Interval(17, 21),
                                 PERIOD_N_EVENING_NIGHT = new Interval(17, 02, "17", "26"),
                                 PERIOD_N_EVENING_DAWN = new Interval(21, 06, "21", "30"),
                                 PERIOD_N_DAWN = new Interval(02, 06, "26", "30");
    private final Interval[] ALL_DAY_PERIODS = {PERIOD_ALL_DAY, PERIOD_D_EARLY_MORNING,
                                                PERIOD_D_MORNING,PERIOD_D_MORNING_EVENING,
                                                PERIOD_D_AFTERNOON_EVENING};
    private final Interval[] ALL_NIGHT_PERIODS = {PERIOD_ALL_NIGHT,PERIOD_N_EVENING,
                                                  PERIOD_N_EVENING_NIGHT,PERIOD_N_EVENING_DAWN,
                                                  PERIOD_N_DAWN};
    
    private PeriodOfDay period;
    private List<Prediction> partlyForecast;
    private JsonWrapper.MetricSystem system;
    private EventType temperature = new EventType<Integer>("temperature"),
                      windChill = new EventType<Integer>("windChill"), 
                      windSpeed = new EventType<Integer>("windSpeed"), 
                      windDir = new EventType<String>("windDir"),
                      gust = new EventType<Integer>("gust"),
                      skyCover = new EventType<Integer>("skyCover"),
                      precipPotential = new EventType<Integer>("precipPotential"),
                      thunderChance = new EventType<Integer>("thunderChance"),
                      rainChance = new EventType<Integer>("rainChance"),
                      snowChance = new EventType<Integer>("snowChance"),
                      freezingRainChance = new EventType<Integer>("freezingRainChance"),
                      sleetChance = new EventType<Integer>("sleetChance");
    private Properties dictionary;
    private String forecastEvents;
    
    public PercyForecast(List<Prediction> partlyForecast, PeriodOfDay period, JsonWrapper.MetricSystem system)
    {
        this.partlyForecast = partlyForecast;
        this.period = period;
        this.system = system;
        dictionary = new Properties();
        try
        {
            dictionary.load(getClass().getResourceAsStream("wundertopercy.properties"));
        }
        catch(IOException ioe)
        {
            LogInfo.error(ioe);
        }
        parseJsonForecast();
        
    }
      
    private void parseJsonForecast()
    {
        for(Prediction p : partlyForecast)
        {
            int hour = p.getTime().getHour();
            temperature.add(hour, system == JsonWrapper.MetricSystem.metric ? 
                            p.getTemperature().getMetric() : p.getTemperature().getEnglish());
            windChill.add(hour, system == JsonWrapper.MetricSystem.metric ? 
                            p.getWindChill().getMetric() : p.getWindChill().getEnglish());
            windSpeed.add(hour, system == JsonWrapper.MetricSystem.metric ? 
                            p.getWindSpeed().getMetric() : p.getWindSpeed().getEnglish());
            windDir.add(hour, p.getWindDir().getDir());            
            skyCover.add(hour, p.getSkyCover());            
            precipPotential.add(hour, p.getChanceOfPrecipitation());            
            thunderChance.add(hour, p.getCondition());
            rainChance.add(hour, p.getCondition());
            snowChance.add(hour, p.getCondition());
            freezingRainChance.add(hour, p.getCondition());
            sleetChance.add(hour, p.getCondition());
            
        }
        StringBuilder str = new StringBuilder();
        // temperature
        str.append(period == PeriodOfDay.day ? 
                    new Event(0, temperature, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", temperature.getMin(PERIOD_ALL_DAY)),
                                   new Field("mean", temperature.getMean(PERIOD_ALL_DAY)),
                                   new Field("max", temperature.getMax(PERIOD_ALL_DAY))) :
                    new Event(0, temperature, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", temperature.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", temperature.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", temperature.getMax(PERIOD_ALL_NIGHT)))               
                  ).append("\n");
        // windChill
        str.append(period == PeriodOfDay.day ? 
                    new Event(1, windChill, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", windChill.getMin(PERIOD_ALL_DAY)),
                                   new Field("mean", windChill.getMean(PERIOD_ALL_DAY)),
                                   new Field("max", windChill.getMax(PERIOD_ALL_DAY))) :
                    new Event(1, windChill, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", windChill.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", windChill.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", windChill.getMax(PERIOD_ALL_NIGHT)))
                  ).append("\n");
        // windSpeed
        str.append(period == PeriodOfDay.day ? 
                    new Event(2, windSpeed, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", windSpeed.getMin(PERIOD_ALL_DAY)),
                                   new Field("mean", windSpeed.getMean(PERIOD_ALL_DAY)),
                                   new Field("max", windSpeed.getMax(PERIOD_ALL_DAY)),
                                   new Field("mode-bucket-0-20-2", windSpeed.getModeBucket(0, 20, 2, PERIOD_ALL_DAY))) :
                    new Event(2, windSpeed, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", windSpeed.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", windSpeed.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", windSpeed.getMax(PERIOD_ALL_NIGHT)),
                                   new Field("mode-bucket-0-20-2", windSpeed.getModeBucket(0, 20, 2, PERIOD_ALL_NIGHT)))                
                  ).append("\n");
        // windDir
        str.append(period == PeriodOfDay.day ? 
                    new Event(3, windDir, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("mode", windDir.getMode(PERIOD_ALL_DAY, null), dictionary)) :
                    new Event(3, windDir, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("mode", windDir.getMode(PERIOD_ALL_NIGHT, null), dictionary))                
                  ).append("\n");
        // gust
        str.append(period == PeriodOfDay.day ? 
                    new Event(4, gust, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", 0), new Field("mean", 0), new Field("max", 0)) :
                    new Event(4, gust, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", 0), new Field("mean", 0), new Field("max", 0))
                  ).append("\n");
        // skyCover
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                    new Event(5+i, skyCover, new Field("time", ALL_DAY_PERIODS[i]), 
                                   new Field("mode-bucket-0-100-4", skyCover.getModeBucket(0, 100, 4, ALL_DAY_PERIODS[i]))) :
                    new Event(5+i, skyCover, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                   new Field("mode-bucket-0-100-4", skyCover.getModeBucket(0, 100, 4, ALL_NIGHT_PERIODS[i])))                
                  ).append("\n");        
        // precipPotential
        str.append(period == PeriodOfDay.day ? 
                    new Event(10, precipPotential, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", precipPotential.getMin(PERIOD_ALL_DAY)),
                                   new Field("mean", precipPotential.getMean(PERIOD_ALL_DAY)),
                                   new Field("max", precipPotential.getMax(PERIOD_ALL_DAY))) :
                    new Event(10, precipPotential, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", precipPotential.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", precipPotential.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", precipPotential.getMax(PERIOD_ALL_NIGHT)))
                  ).append("\n");
        // thunderChance
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                        new Event(11+i, thunderChance, new Field("time", ALL_DAY_PERIODS[i]), 
                                       new Field("mode", thunderChance.getMode(ALL_DAY_PERIODS[i], dictionary), dictionary)) :
                        new Event(11+i, thunderChance, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                       new Field("mode", thunderChance.getMode(ALL_NIGHT_PERIODS[i], dictionary), dictionary))
                      ).append("\n");
        // rainChance
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                        new Event(16+i, rainChance, new Field("time", ALL_DAY_PERIODS[i]), 
                                       new Field("mode", rainChance.getMode(ALL_DAY_PERIODS[i], dictionary), dictionary)) :
                        new Event(16+i, rainChance, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                       new Field("mode", rainChance.getMode(ALL_NIGHT_PERIODS[i], dictionary), dictionary))
                      ).append("\n");
        // snowChance
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                        new Event(21+i, snowChance, new Field("time", ALL_DAY_PERIODS[i]), 
                                       new Field("mode", snowChance.getMode(ALL_DAY_PERIODS[i], dictionary), dictionary)) :
                        new Event(21+i, snowChance, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                       new Field("mode", snowChance.getMode(ALL_NIGHT_PERIODS[i], dictionary), dictionary))
                      ).append("\n");
        // freezingRainChance
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                        new Event(26+i, freezingRainChance, new Field("time", ALL_DAY_PERIODS[i]), 
                                       new Field("mode", freezingRainChance.getMode(ALL_DAY_PERIODS[i], dictionary), dictionary)) :
                        new Event(26+i, freezingRainChance, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                       new Field("mode", freezingRainChance.getMode(ALL_NIGHT_PERIODS[i], dictionary), dictionary))
                      ).append("\n");
        // sleetChance
        for(int i = 0; i < ALL_DAY_PERIODS.length; i++)
            str.append(period == PeriodOfDay.day ? 
                        new Event(31+i, sleetChance, new Field("time", ALL_DAY_PERIODS[i]), 
                                       new Field("mode", sleetChance.getMode(ALL_DAY_PERIODS[i], dictionary), dictionary)) :
                        new Event(31+i, sleetChance, new Field("time", ALL_NIGHT_PERIODS[i]), 
                                       new Field("mode", sleetChance.getMode(ALL_NIGHT_PERIODS[i], dictionary), dictionary))
                      ).append("\n");
       
        forecastEvents = str.toString();
    }

    public String getForecastEvents()
    {
        return forecastEvents;
    }
        
               
}