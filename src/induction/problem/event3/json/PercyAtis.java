package induction.problem.event3.json;

import induction.problem.event3.json.AtisLowJet.Date;
import induction.problem.event3.json.AtisLowJet.Search;

/**
 *
 * @author konstas
 */
public class PercyAtis
{            
    private AtisLowJet booking;    
    private EventType flight = new EventType<Integer>("flight"),
                      search = new EventType<String>("search"),
                      dayNumber = new EventType<String>("day_number"),
                      day = new EventType<String>("day"),
                      month = new EventType<String>("month"),
                      when = new EventType<String>("when");
//    private Properties dictionary;
    private String atisEvents;
    private int id = 0;
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
        str.append(new Event(id++, flight, new Field("aircraft_code", "--"), 
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
            str.append(new Event(id++, search, new Field("of", s.getOf()), 
                                       new Field("typed", s.getTyped()),
                                       new Field("what", s.getWhat())                                   
                                 )
                      ).append("\n");
        }
        // dates
        for(Date d : booking.getDates())
        {
            str.append(createDateEvents(d));
        }
        atisEvents = str.toString();
    }

    private String createDateEvents(Date d)
    {
        StringBuilder str = new StringBuilder();
        String depArRet = d.getDepArRet();
        if(!d.getDayNumber().equals("--"))
            str.append(new Event(id++, dayNumber, new Field("day_number", d.getDayNumber()), 
                                       new Field("dep_ar_ret", depArRet)
                                 )
                      ).append("\n");
        if(!d.getDay().equals("--"))
            str.append(new Event(id++, day, new Field("day", d.getDay()), 
                                       new Field("dep_ar_ret", depArRet)
                                 )
                      ).append("\n");
        if(!d.getMonth().equals("--"))
            str.append(new Event(id++, month, new Field("dep_ar_ret", depArRet), 
                                       new Field("month", d.getMonth())
                                 )
                      ).append("\n");
        if(!d.getWhen().equals("--"))
            str.append(new Event(id++, when, new Field("dep-ar", depArRet), 
                                       new Field("when", d.getWhen())
                                 )
                      ).append("\n");
        return str.toString();
    }
    
    public String getAtisEvents()
    {
        return atisEvents;
    }
}