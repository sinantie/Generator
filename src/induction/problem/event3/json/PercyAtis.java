package induction.problem.event3.json;

import induction.problem.event3.json.AtisLowJet.Search;
import java.util.Properties;

/**
 *
 * @author konstas
 */
public class PercyAtis
{        
    
    private final Interval PERIOD_ALL_DAY = new Interval(6, 21);
        
    private AtisLowJet booking;    
    private EventType flight = new EventType<Integer>("flight"),
                      search = new EventType<String>("search"),
                      dayNumber = new EventType<String>("day_number"),
                      day = new EventType<String>("day"),
                      month = new EventType<String>("month"),
                      when = new EventType<String>("when");
//    private Properties dictionary;
    private String atisEvents;
    
    public PercyAtis(AtisLowJet booking)
    {
        this.booking = booking;        
//        dictionary = new Properties();
//        try
//        {
//            dictionary.load(getClass().getResourceAsStream("lowjettopercy.properties"));
//        }
//        catch(IOException ioe)
//        {
//            LogInfo.error(ioe);
//        }
        parseJsonForecast();
        
    }
      
    private void parseJsonForecast()
    {       
        StringBuilder str = new StringBuilder();
        // flight
        int id = 0;
        str.append(
                    new Event(id++, flight, new Field("aircraft_code", "--"), 
                                   new Field("airline", "--"),
                                   new Field("class_type", booking.getFlight().getClassType()),
                                   new Field("direction", booking.getFlight().getDirection()),
                                   new Field("engine", "--"),
                                   new Field("fare", "--"),
                                   new Field("flight_number", "--"),
                                   new Field("from", booking.getFlight().getFrom()),
                                   new Field("manufacturer", "--"),
                                   new Field("price", "--"),
                                   new Field("stop", booking.getFlight().getStop()),
                                   new Field("to", booking.getFlight().getTo()),
                                   new Field("year", "--")
                             )
                  ).append("\n");
        //search
        for(Search s : booking.getSearch())
        {
            str.append(
                        new Event(id++, search, new Field("of", s.getOf()), 
                                       new Field("typed", s.getTyped()),
                                       new Field("what", s.getWhat())                                   
                                 )
                      ).append("\n");
        }
        atisEvents = str.toString();
    }

    public String getAtisEvents()
    {
        return atisEvents;
    }
        
    
    
    
    
    
    
}