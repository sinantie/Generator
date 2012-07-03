package induction.utils.humanevaluation;

import induction.Utils;
import induction.problem.event3.Event;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author sinantie
 */
public class Scenario
{
    private Map<Integer, Event> events;
    private Set<String> eventTypeNames;
    private String path, tagDelimiter;
    /**
     * a map of systems and their corresponding text
     */
    private HashMap<String, String> systemsTextMap;
    /**
     * a map of systems and their corresponding TreeSet of event id's.
     * We use TreeSets so as to maintain the same order in the events
     */
    private HashMap<String, TreeSet<Integer>> systemsEventMap;
    
    private Scenario(String path, String tagDelimiter)
    {
        this.path = path;
        this.tagDelimiter = tagDelimiter;
        this.systemsTextMap = new HashMap<String, String>();
        this.systemsEventMap = new HashMap<String, TreeSet<Integer>>();            
        this.eventTypeNames = new HashSet<String>();
    }

    public Scenario(String path, Map<Integer, Event> events, String tagDelimiter)
    {
        this(path, tagDelimiter);
        this.events = events;
        for(Event event : events.values())
        {
            eventTypeNames.add(event.getEventTypeName());
        }
    }
    
    public Map<Integer, Event> getEvents()
    {
        return events;
    }

    /**
     * Returns the id of a particular event, matching on the name, i.e. eventType.
     * Performs a linear search on the set of keys, so use with caution
     * @param eventType
     * @return
     */
    public Integer getIdOfEvent(String eventType)
    {
        for(Integer id : events.keySet())
            if(events.get(id).getEventTypeName().equals(eventType))
                return id;
        return null;
    }

    public void setText(String system, String text)
    {        
        systemsTextMap.put(system, Utils.stripTags(text, tagDelimiter));
    }

    public String getText(String system)
    {
        return systemsTextMap.get(system);
    }

    public TreeSet<Integer> getEventIndices(String system)
    {
        if(!systemsEventMap.containsKey(system))
            systemsEventMap.put(system, new TreeSet<Integer>());
        return systemsEventMap.get(system);
    }

    public Set<String> getEventTypeNames()
    {
        return eventTypeNames;
    }

    public String getPath()
    {
        return path;
    }
    
}
