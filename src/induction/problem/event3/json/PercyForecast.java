package induction.problem.event3.json;

import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                      NIGHT_END = 30;
    private final PeriodInterval PERIOD_ALL_DAY = new PeriodInterval(6, 21),
                                 PERIOD_D_EARLY_MORNING = new PeriodInterval(6, 9),
                                 PERIOD_D_MORNING = new PeriodInterval(6, 13),
                                 PERIOD_D_MORNING_EVENING = new PeriodInterval(9, 21),
                                 PERIOD_D_AFTERNOON_EVENING = new PeriodInterval(13, 21),
            
                                 PERIOD_ALL_NIGHT = new PeriodInterval(17, 06, "17", "30"),
                                 PERIOD_N_EVENING = new PeriodInterval(17, 21),
                                 PERIOD_N_EVENING_NIGHT = new PeriodInterval(17, 02, "17", "26"),
                                 PERIOD_N_EVENING_DAWN = new PeriodInterval(21, 06, "21", "30"),
                                 PERIOD_N_DAWN = new PeriodInterval(02, 06, "26", "30");
    private PeriodOfDay period;
    private List<Prediction> partlyForecast;
    private JsonWrapper.MetricSystem system;
    private EventType temperature, windChill;
    
    private String forecastEvents;
    
    public PercyForecast(List<Prediction> partlyForecast, PeriodOfDay period, JsonWrapper.MetricSystem system)
    {
        this.partlyForecast = partlyForecast;
        this.period = period;
        this.system = system;
        temperature = new EventType<Integer>();
        windChill = new EventType<Integer>();
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
            
        }
        StringBuilder str = new StringBuilder();
        str.append(period == PeriodOfDay.day ? 
                    new Event(0, temperature, new Field("time", PERIOD_ALL_DAY), 
                                   new Field("min", temperature.getMin(PERIOD_ALL_DAY)),
                                   new Field("mean", temperature.getMean(PERIOD_ALL_DAY)),
                                   new Field("max", temperature.getMean(PERIOD_ALL_DAY))) :
                    new Event(0, temperature, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", temperature.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", temperature.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", temperature.getMean(PERIOD_ALL_NIGHT)))
                
                  ).append("\n");
        
        forecastEvents = str.toString();
    }

    public String getForecastEvents()
    {
        return forecastEvents;
    }
        
    private class EventType<T>
    {
        String type;
        Map<Integer, T> hourlyValues;
        Field[] fields;

        public EventType()
        {
            hourlyValues = new HashMap<Integer, T>();
        }        
        
        public void add(Integer hour, T value)
        {
            hourlyValues.put(hour, value);
        }

        public String getType()
        {
            return type;
        }
        
        public int getMin(PeriodInterval interval)
        {            
            return getMinMax(interval, true);
        }
        public int getMax(PeriodInterval interval)
        {
            return getMinMax(interval, false);
        }
        
        private int getMinMax(PeriodInterval interval, boolean findMin)
        {
            // we assume hourlyValues are integers
            int v = findMin ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                Integer value = (Integer)entry.getValue();
                if(key >= interval.begin && key <= interval.end)
                {
                    if(findMin && value < v || !findMin && value > v)
                        v = value;                        
                }
            } // for
            return v == -9999 || v == -999 ? 0 : v; // Null or N/A numbers
        }
        
        public int getMean(PeriodInterval interval)
        {
            float total = 0.0f;
            
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                Integer value = (Integer)entry.getValue();
                if(key >= interval.begin && key <= interval.end)
                {
                    total += value;                        
                }
            } // for
            return Math.round(total / (float)hourlyValues.size());
        }
    }
    
    private class Event
    {
        int id;
        EventType type;
        Field[] fields;

        public Event(int id, EventType type, Field... fields)
        {
            this.id = id;
            this.type = type;
            this.fields = fields;
        }

        @Override
        public String toString()
        {
            // preamble
            StringBuilder str = new StringBuilder(String.format(".id:%s\t.type:%s", id, type.getType()));
            for(Field f : fields)
                str.append("\t").append(f);
            return str.toString();
        }                
    }
    
    private class Field<T>
    {
        String name;
        T value;

        public Field(String name, T value)
        {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value instanceof Integer ? "#" : "@" + 
                   String.format("%s:%s", name, value);
        }
        
        
    }
    
    private class PeriodInterval
    {
        int begin, end;
        String outBegin, outEnd; // in case we need to print different/begin end times (e.g. 06:00 becomes 30)
        public PeriodInterval(int begin, int end)
        {            
            this.begin = begin;
            this.end = end;
        }
        
        public PeriodInterval(int begin, int end, String outBegin, String outEnd)
        {            
            this.begin = begin;
            this.end = end;
            this.outBegin = outBegin;
            this.outEnd = outEnd;
        }

        @Override
        public String toString()
        {
            return String.format("@time:%s-%s", outBegin == null ? begin : outBegin, 
                                                outEnd == null ? end : outEnd);
        }
        
    }
}
