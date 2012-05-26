package induction.problem.event3.json;

import fig.basic.LogInfo;
import induction.problem.event3.json.HourlyForecastWunder.Prediction;
import java.io.IOException;
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
                      NIGHT_END = 30;
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
       
        forecastEvents = str.toString();
    }
    
    private String createEvent(int id, EventType eventType, Interval interval, Field... fields)
    {
        StringBuilder str = new StringBuilder();
        str.append(period == PeriodOfDay.day ? 
                    new Event(id, eventType, fields) :
                    new Event(id, eventType, new Field("time", PERIOD_ALL_NIGHT), 
                                   new Field("min", temperature.getMin(PERIOD_ALL_NIGHT)),
                                   new Field("mean", temperature.getMean(PERIOD_ALL_NIGHT)),
                                   new Field("max", temperature.getMax(PERIOD_ALL_NIGHT)))
                
                  ).append("\n");
        return str.toString();
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

        public EventType(String type)
        {
            this.type = type;
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
        
        public int getMin(Interval interval)
        {            
            return getMinMax(interval, true);
        }
        public int getMax(Interval interval)
        {
            return getMinMax(interval, false);
        }
        
        private int getMinMax(Interval interval, boolean findMin)
        {
            // we assume hourlyValues are integers
            int v = findMin ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                Integer value = (Integer)entry.getValue();
                if(interval.begin < interval.end && (key >= interval.begin && key <= interval.end) ||
                   interval.begin > interval.end && (key >= interval.begin || key <= interval.end))
                {
                    if(findMin && value < v || !findMin && value > v)
                        v = value;                        
                }
            } // for
            return v < -999 ? 0 : v; // Null or N/A numbers
        }
        
        public int getMean(Interval interval)
        {
            float total = 0.0f;
            
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                Integer value = (Integer)entry.getValue();
                if(interval.begin < interval.end && (key >= interval.begin && key <= interval.end) ||
                   interval.begin > interval.end && (key >= interval.begin || key <= interval.end))
                {
                    total += value;                        
                }
            } // for
            int res = Math.round(total / (float)hourlyValues.size());
            return res < - 999 ? 0 : res; // Null or N/A numbers
        }
        
        public T getMode(Interval interval, Properties dictionary)
        {            
            Map<T, Integer> hist = new HashMap<T, Integer>();
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                T value = entry.getValue();
                if(interval.begin < interval.end && (key >= interval.begin && key <= interval.end) ||
                   interval.begin > interval.end && (key >= interval.begin || key <= interval.end))
                {
                    Integer i = hist.get(value);
                    hist.put(value, i == null ? 1 : i + 1);
                }
            }
            // assume a single mode
            int max = Integer.MIN_VALUE; T mode = null;
            for(Map.Entry<T, Integer> entry : hist.entrySet())
            {
                T key = entry.getKey();
                Integer value = entry.getValue();
                if(value > max)
                {
                    max = value;
                    mode = key;
                }
            }
            return mode;
        }
        
        /**
         * Input is an integer and we discretise it into buckets of size (max-min)/size.
         * Method returns the most frequent bucket in String format
         * @param min
         * @param max
         * @param size
         * @param interval
         * @return 
         */
        public String getModeBucket(int min, int max, int size, Interval interval)
        {
            final int bucketSize = (max - min) / size;
            int[] counts = new int[size];
            Interval[] buckets = new Interval[size];            
            for(int i = 0; i < size; i++)
            {
                buckets[i] = new Interval(min + bucketSize*i, min + bucketSize*(i+1));
            }
            for(Map.Entry<Integer, T> entry : hourlyValues.entrySet())
            {
                Integer key = entry.getKey();
                T value = entry.getValue();
                if(interval.begin < interval.end && (key >= interval.begin && key <= interval.end) ||
                   interval.begin > interval.end && (key >= interval.begin || key <= interval.end))
                {
                    counts[bucketIndex(buckets, (Integer)value)]++;
                }
            }            
            return buckets[Utils.maxIndex(counts)].toString();
        }
        
        /**
         * finds the bucket that the value belongs to. [bucket.begin, bucket.end),
         * Last bucket is bucket.end inclusive.
         * @param buckets
         * @param value
         * @return 
         */
        private int bucketIndex(Interval[] buckets, int value)
        {
            int index = buckets.length - 1;
            for(int i = 0; i < buckets.length - 1; i++)
            {
                if(value >= buckets[i].begin && value < buckets[i].end)
                {
                    index = i; break;
                }
            }
            return index;
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
        Properties dictionary;
        
        public Field(String name, T value)
        {
            this.name = name;
            this.value = value;
        }
        
        /*
         * Check whether value is in the dictionary and replace accordingly.
         * We assume T is of String type. 
         */
        public Field(String name, T value, Properties dictionary)
        {
            this(name, value);
            this.dictionary = dictionary;
        }

        @Override
        public String toString()
        {
            // Check whether value is in the dictionary and replace accordingly.
            // We assume T is of String type. 
            if(dictionary != null)
            {
                String dictValue = dictionary.getProperty((String) value);            
                return (value instanceof Integer ? "#" : "@") + 
                   String.format("%s:%s", name, dictValue == null ? value : dictValue);
            }
            else
                return (value instanceof Integer ? "#" : "@") + 
                   String.format("%s:%s", name, value);            
        }                
    }
    
    private class Interval
    {
        int begin, end;
        String outBegin, outEnd; // in case we need to print different/begin end times (e.g. 06:00 becomes 30)
        public Interval(int begin, int end)
        {            
            this.begin = begin;
            this.end = end;
        }
        
        public Interval(int begin, int end, String outBegin, String outEnd)
        {            
            this.begin = begin;
            this.end = end;
            this.outBegin = outBegin;
            this.outEnd = outEnd;
        }

        @Override
        public String toString()
        {
            return String.format("%s-%s", outBegin == null ? begin : outBegin, 
                                                outEnd == null ? end : outEnd);
        }        
    }
}