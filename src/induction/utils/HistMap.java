package induction.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author konstas
 */
public class HistMap<T>
{
    private HashMap<T, Counter> map = new HashMap<T, Counter>();

    public void add(T word)
    {
        Counter counter = map.get(word);
        if (counter == null) 
        {
            counter = new Counter(word);
            map.put(word, counter);
        }
        counter.incr();
    }
   
    public Set<Entry<T, Counter>> getEntries()
    {
        return map.entrySet();
    }
    
    public Set<T> getKeys()
    {
        return map.keySet();
    }
   
    public int getFrequency(T key)
    {
        return map.get(key).value;
    }
    
    /**
     * Returns frequency map in decreasing order
     * @return 
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        List<Counter> list = new ArrayList<Counter>(map.values());
        Collections.sort(list);
        for(Counter c : list)
            str.append(c).append("\n");
//        for(Entry<T, Counter> e : map.entrySet())
//        {
//            str.append(String.format("%s : %s\n", e.getKey(), e.getValue()));
//        }
//        str.delete(str.lastIndexOf(","), str.length());
        return str.toString();
    }
    
    final class Counter implements Comparable
    {
        private T key;
        private int value;

        public Counter(T key)
        {
            this.key = key;
        }
        
        public int getValue()
        {
            return value;
        }

        public void incr()
        {
            value++;
        }

        @Override
        public String toString()
        {
//            return String.valueOf(value);
            return String.format("%s : %s", key, value);
        }

        @Override
        public int compareTo(Object o)
        {            
            return ((Counter)o).value - value;            
        }
    }         
}
