package induction.problem.event3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sinantie
 */
public class Event3Example
{
    private String[] records;

    public Event3Example(String[] records)
    {
        this.records = records;
    }
    
    public Event3Example(String name, String text, String events, String alignments)
    {
        records = new String[] {name, text, events, alignments,  null};        
    }
    
    public Map<String, String> getEventsMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        for(String line : getEvents().split("\n"))
        {
            String[] tokens = line.split("\t");
            String id = tokens[0].split(":")[1];
            map.put(id, packEvents(Arrays.copyOfRange(tokens, 1, tokens.length)));
        }
        return map;
    }
    
    public List<String> getEventsList()
    {
        return Arrays.asList(getEvents().split("\n"));
    }
    
    public String[] getEventsArray()
    {
        return getEvents().split("\n");
    }
    
    public static String packEvents(String[] tokens)
    {
        StringBuilder str = new StringBuilder();
        for(String token : tokens)
            str.append(token).append("\t");
        return str.deleteCharAt(str.length()-1).toString();
    }
    
    public static String packEvents(int id, String[] tokens)
    {
        String[] ar = new String[tokens.length+1];
        ar[0] = ".id:" + id;        
        for(int i = 0; i < tokens.length; i++)
            ar[i + 1] = tokens[i];
        return packEvents(ar);
    }
    
    public static Collection<Integer> getFlatAlignments(String alignments)
    {
        List<Integer> recordsList = new ArrayList<Integer>();        
        for(String line : alignments.split("\n"))
        {
            String[] tokens = line.split(" ");
            for(int i = 1; i < tokens.length; i++) // ignore first character which is the line number
            {
                Integer r = Integer.valueOf(tokens[i]);
                if(recordsList.isEmpty() || !recordsList.contains(r))
                    recordsList.add(r);
            }
        }
        return recordsList;
    }
    
    public static String packEvents(int id, String tokens)
    {        
        return String.format(".id:%s\t%s", id, tokens);
    }
            
    public String getName()
    {
        return records.length > 0 ? records[0] : "";
    }
    
    public String getText()
    {
        return records.length > 0 ? records[1] : "";
    }
    
    public void setText(String text)
    {
        if(records.length > 0)
            records[1] = text;
    }
    public String[] getTextArray()
    {
        return records.length > 0 ? records[1].split("\n") : new String[0];
    }
    
    public String getEvents()
    {
        return records.length > 0 ? records[2] : "";
    }
    
    public String getAlignments()
    {
        return hasAlignments() ? records[3] : "";
    }

    public String[] getAlignmentsArray()
    {
        return hasAlignments() ? records[3].split("\n") : new String[0];
    }
    
    public boolean hasAlignments()
    {
        return records[3] != null;
    }
    
    public String getTree()
    {
        return hasTree() ? records[4] : "";
    }

    public void setTree(String tree)
    {        
        records[4] = tree;
    }
    
    public boolean hasTree()
    {
        return records[4] != null;
    }
    
    public int getNumberOfRecords()
    {
        return records.length;
    }
    
    @Override
    public String toString()
    {
        if(hasTree())
            return String.format("$NAME\n%s\n$TEXT\n%s\n$EVENTS\n%s\n$RECORD_TREE\n%s\n$ALIGN\n%s", 
                getName(), getText(), getEvents(), getTree().trim(), getAlignments());
        if(hasAlignments())
            return String.format("$NAME\n%s\n$TEXT\n%s\n$EVENTS\n%s\n$ALIGN\n%s\n", 
                getName(), getText(), getEvents().trim(), getAlignments());
        return String.format("$NAME\n%s\n$TEXT\n%s\n$EVENTS\n%s", 
            getName(), getText(), getEvents());
    }        
}
