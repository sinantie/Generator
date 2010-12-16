package edu.berkeley.nlp.mt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Ioannis Konstas
 */
public class BleuModifiedNgram implements Comparable<BleuModifiedNgram>
{
    private String pattern = "";
    private List<Integer> numbers;


    public BleuModifiedNgram(List<String> ngram)
    {        
        this.numbers = new ArrayList(ngram.size());
        for(String s: ngram)
        {
            // make the distinction between text and numbers
            if(s.matches("\\p{Digit}+"))
            {
                this.numbers.add(Integer.valueOf(s));
                pattern += "# ";
            }
            else
            {
                pattern += s + " ";
            }
        }
        pattern = pattern.trim();
    }

//    @Override
//    public boolean equals(Object obj)
//    {
//        assert obj instanceof BleuModifiedNgram;
//        BleuModifiedNgram b = (BleuModifiedNgram)obj;
//        boolean equal = true;
//
//        String[] thisPattern = pattern.split(" ");
//        String[] otherPattern = b.pattern.split(" ");
//        for(int i = 0; i < thisPattern.length; i++)
//        {
//            if(thisPattern[i].matches("\\p{Digit}+") && otherPattern[i].matches("\\p{Digit}+"))
//                equal = equal && Math.abs(Integer.valueOf(thisPattern[i]) - Integer.valueOf(otherPattern[i])) <= 5;
//            else
//                equal = equal && thisPattern[i].equals(otherPattern[i]);
//        }
//        return equal;
//
//    }
    @Override
    public boolean equals(Object obj)
    {
        assert obj instanceof BleuModifiedNgram;
        BleuModifiedNgram b = (BleuModifiedNgram)obj;

        if(!pattern.equals(b.pattern))
            return false;
        for(int i = 0; i < numbers.size(); i++)
        {
            if(Math.abs(numbers.get(i) - b.numbers.get(i)) > 6)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
//        int hash = 5;
//        hash = 61 * hash + (this.pattern != null ? this.pattern.hashCode() : 0);
//        hash = 61 * hash + (this.numbers != null ? this.numbers.hashCode() : 0);
//        return hash;
        return 0;
    }

    @Override
    public String toString()
    {        
        String[] ar = pattern.split(" ");
        String out = "";
        int i = 0;
        for(String s : ar)
        {
            if(s.equals("#"))
                out += numbers.get(i++) + " ";
            else
                out += s + " ";
        }
        return out.trim();
    }
  

    public int compareTo(BleuModifiedNgram o)
    {
        if(this.equals(o))
                return 0;
        return -1;
    }
}
