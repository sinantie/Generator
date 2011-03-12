package induction.utils;

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
    private String path;
    /**
     * a map of systems and their corresponding text
     */
    private HashMap<String, String> systemsTextMap;
    /**
     * a map of systems and their corresponding TreeSet of event id's.
     * We use TreeSets so as to maintain the same order in the events
     */
    private HashMap<String, TreeSet<Integer>> systemsEventMap;
    
    private Scenario(String path)
    {
        this.path = path;
        this.systemsTextMap = new HashMap<String, String>();
        this.systemsEventMap = new HashMap<String, TreeSet<Integer>>();            
        this.eventTypeNames = new HashSet<String>();
    }

    public Scenario(String path, Map<Integer, Event> events)
    {
        this(path);
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

    public void setText(String system, String text)
    {
        systemsTextMap.put(system, text);
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
