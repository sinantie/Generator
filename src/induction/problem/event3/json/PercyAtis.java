package induction.problem.event3.json;

import fig.basic.LogInfo;
import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author konstas
 */
public class PercyAtis
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
    private String atisEvents;
    
    public PercyAtis(List<Prediction> partlyForecast, PeriodOfDay period, JsonWrapper.MetricSystem system)
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
        atisEvents = str.toString();
    }

    public String getAtisEvents()
    {
        return atisEvents;
    }
        
    
    
    
    
    
    
}