/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import weka.core.Utils;

/**
 *
 * @author sinantie
 */
public class EventType<T>
{

    String type;
    Map<Integer, T> hourlyValues;    

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
        for (Map.Entry<Integer, T> entry : hourlyValues.entrySet()) {
            Integer key = entry.getKey();
            if (interval.begin < interval.end && (key >= interval.begin && key <= interval.end)
                    || interval.begin > interval.end && (key >= interval.begin || key <= interval.end)) {
                Integer value = (Integer) entry.getValue();
                if (findMin && value < v || !findMin && value > v) {
                    v = value;
                }
            }
        } // for
        return v < -999 ? 0 : v; // Null or N/A numbers
    }

    public int getMean(Interval interval)
    {
        float total = 0.0f;

        for (Map.Entry<Integer, T> entry : hourlyValues.entrySet()) {
            Integer key = entry.getKey();
            if (interval.begin < interval.end && (key >= interval.begin && key <= interval.end)
                    || interval.begin > interval.end && (key >= interval.begin || key <= interval.end)) {
                total += (Integer) entry.getValue();
            }
        } // for
        int res = Math.round(total / (float) hourlyValues.size());
        return res < - 999 ? 0 : res; // Null or N/A numbers
    }

    /**
     * Find mode of the given distribution of values. In case of a dictionary
     * search for particular terms for the specified eventType.
     * @param interval
     * @param dictionary
     * @return 
     */
    public T getMode(Interval interval, Properties dictionary)
    {
        boolean found = false;
        Map<T, Integer> hist = new HashMap<T, Integer>();
        List dictValues = dictionary == null ? null : Arrays.asList(dictionary.getProperty(type).split(","));
        for (Map.Entry<Integer, T> entry : hourlyValues.entrySet()) {
            Integer key = entry.getKey();
            if (interval.begin < interval.end && (key >= interval.begin && key <= interval.end)
                    || interval.begin > interval.end && (key >= interval.begin || key <= interval.end)) {
                found = true;
                T origValue = entry.getValue();
                if (dictionary == null) {
                    Integer i = hist.get(origValue);
                    hist.put(origValue, i == null ? 1 : i + 1);
                }
                else {
                    // works only for string values
                    String targetValue = dictValues.contains((String) origValue) ? (String) origValue : "--";
                    Integer i = hist.get(targetValue);
                    hist.put((T) targetValue, i == null ? 1 : i + 1);
                }
            }
        }
        // assume a single mode
        int max = Integer.MIN_VALUE;
        T mode = null;
        for (Map.Entry<T, Integer> entry : hist.entrySet()) {
            T key = entry.getKey();
            Integer value = entry.getValue();
            if (value > max) {
                max = value;
                mode = key;
            }
        }
        // sanity check: in case we are out of bounds output "--"
        return found ? mode : (T) "--";
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
        boolean found = false;
        final int bucketSize = (max - min) / size;
        int[] counts = new int[size];
        Interval[] buckets = new Interval[size];
        for (int i = 0; i < size; i++) {
            buckets[i] = new Interval(min + bucketSize * i, min + bucketSize * (i + 1));
        }
        for (Map.Entry<Integer, T> entry : hourlyValues.entrySet()) {
            found = true;
            Integer key = entry.getKey();
            T value = entry.getValue();
            if (interval.begin < interval.end && (key >= interval.begin && key <= interval.end)
                    || interval.begin > interval.end && (key >= interval.begin || key <= interval.end)) {
                counts[bucketIndex(buckets, (Integer) value)]++;
            }
        }
        // sanity check: in case we are out of bounds choose the smallest bucket
        return found ? buckets[Utils.maxIndex(counts)].toString() : buckets[0].toString();
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
        for (int i = 0; i < buckets.length - 1; i++) {
            if (value >= buckets[i].begin && value < buckets[i].end) {
                index = i;
                break;
            }
        }
        return index;
    }
}
