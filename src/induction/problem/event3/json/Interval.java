/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

/**
 *
 * @author sinantie
 */
public class Interval
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
