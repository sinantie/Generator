package induction.problem.event3.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author sinantie
 */
public class AtisLowJet
{
    Flight flight;
    Search[] search;
    Date[] dates;
    
    public Flight getFlight()
    {
        return flight;
    }

    public Search[] getSearch()
    {
        return search;
    }

    public Date[] getDates()
    {
        return dates;
    }
                    
    public static class Flight
    {
        String classType, direction, from, stop, to;

        @JsonProperty("class_type")
        public String getClassType()
        {
            return classType;
        }

        public String getDirection()
        {
            return direction;
        }

        public String getFrom()
        {
            return from;
        }

        public String getStop()
        {
            return stop;
        }

        public String getTo()
        {
            return to;
        }
        
    }    
    
    public static class Search //extends Record
    {
        String of, typed, what;

        public String getOf()
        {
            return of;
        }

        public String getTyped()
        {
            return typed;
        }

        public String getWhat()
        {
            return what;
        }                
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Date
    {
        String depArRet, dayNumber, day, month, when;

        public String getDepArRet()
        {
            return depArRet;
        }
        
        public String getDayNumber()
        {
            return dayNumber;
        }

        public String getDay()
        {
            return day;
        }

        public String getMonth()
        {
            return month;
        }

        public String getWhen()
        {
            return when;
        }                
    }

}
