package induction.utils;

import java.util.HashMap;

/**
 *
 * @author konstas
 */
public class HistMap<T> 
{
    HashMap<T, Hist> map = new HashMap<T, Hist>();

    public void add(T word)
    {
        Hist current = new Hist(word);
        if(map.containsKey(word))
        {
            map.get(word).count++;
        }
        else
            map.put(word, current);
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for(Hist h : map.values())
        {
            str.append(h).append(", ");
        }
        str.delete(str.lastIndexOf(","), str.length());
        return str.toString();
    }

    class Hist<T>
    {
        T key;
        int count;

        public Hist(T word)
        {
            this.key = word;
            this.count = 1;
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof Hist;
            Hist h = (Hist)obj;
            return h.key.equals(key);
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 11 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString()
        {
            return key.toString() + " : " + count;
        }
    }    
}
