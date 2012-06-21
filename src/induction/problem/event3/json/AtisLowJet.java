package induction.problem.event3.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author sinantie
 */
public class AtisLowJet
{
    Flight flight;
    Search[] search;

    public Flight getFlight()
    {
        return flight;
    }

    public Search[] getSearch()
    {
        return search;
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
    
    
    //@JsonIgnoreProperties(ignoreUnknown = true)
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
    

}
