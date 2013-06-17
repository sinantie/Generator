package induction.problem.event3;

import induction.Utils;
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
    
    public Event3Example(String name, Event3Example ex)
    {
        records = new String[] {name, ex.getText(), ex.getEvents(), 
                                ex.hasAlignments() ? ex.getAlignments() : null, 
                                ex.hasTree() ? ex.getTree() : null};
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
        
    /**
     * POS-tag the string values of the events of this example, if they exist.
     * @param taggedText the POS-tagged string to get the tags from. It should
     * be identical to the text within the example, less the tags.
     */
    public void tagStringValues(String taggedText)
    {
        if(containsStringValues())
        {
            StringBuilder str = new StringBuilder();            
            for(String events : getEventsArray())
            {
                String[] tokens = events.split("\t");
                for(int i = 0; i < tokens.length; i++)
                {
                    if(tokens[i].startsWith("$"))
                    {
                        String[] fieldValue = tokens[i].split(":");
                        String oldValue = fieldValue[1];
                        fieldValue[1] = tagSubstring(oldValue, getText(), taggedText);
                        // replace old token
                        tokens[i] = fieldValue[0] + ":" + fieldValue[1];
                    }
                }
                str.append(packEvents(tokens)).append("\n");
            }
            records[2] = str.deleteCharAt(str.length()-1).toString();
        }        
    }
    
    public boolean containsStringValues()
    {        
        for(String events : getEventsArray())
        {
            for(String fieldValue : events.split("\t"))
            {
                if(fieldValue.startsWith("$"))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * copy-paste the tagged portion from the <code>taggedText</code> string
     * that corresponds to <code>oldValue</code>.
     * @param oldValue the string we want to tag.
     * @param text the original un-tagged string. It should contain <code>oldValue</code>
     * @param taggedText the string with the tag annotation. It should be identical.
     * to <code>text</code> less the tags.
     * @return the <code>oldValue</code> pos-tagged.
     */
    private String tagSubstring(String oldValue, String text, String taggedText)
    {
        String[] textAr = text.split(" ");
        String[] taggedTextAr = taggedText.split(" ");
        String[] oldValueAr = oldValue.split(" ");
        StringBuilder str = new StringBuilder();
        int startIndex = 0;
        for(int i = 0; i < textAr.length; i++)
        {
            if(textAr[i].equals(oldValueAr[0]))
            {
                startIndex = i;                
                break;
            }
        }        
        for(int i = startIndex; i < startIndex + oldValueAr.length; i++)
        {
            str.append(taggedTextAr[i]).append(" ");
        }
        return str.deleteCharAt(str.length()-1).toString();
    }
        
    public static String packEvents(int id, String tokens)
    {        
        return String.format(".id:%s\t%s", id, tokens);
    }
    
    public void add(Event3Example example)
    {
        // append text as a new line
        int oldNumberOfLinesInText = getNumberOfTextLines();
        records[1] += "\n" + example.getText();
        // re assign event ids
        int numOfEvents = getEventsArray().length;
        int[] otherIds = example.getEventIds();
        Map<Integer, Integer> mapOfIds = new HashMap<Integer, Integer>();
        for(int i = 0; i < otherIds.length; i++)
        {
            int newId = otherIds[i] + numOfEvents;
            mapOfIds.put(otherIds[i], newId);
            otherIds[i] = newId;
        }
        StringBuilder str = new StringBuilder();
        int i = 0;
        // repack the events with the new ids
        for(String event : example.getEventsArray())
        {
            String[] tokens = event.split("\t");
            str.append("\n").append(packEvents(otherIds[i++], Arrays.copyOfRange(tokens, 1, tokens.length)));
        }
        // append events
        records[2] += str.toString();
        // remap alignment ids and append to to the list of existing alignments
        records[3] += "\n" + reMapAlignments(example.getAlignments(), mapOfIds, oldNumberOfLinesInText);
        
    }
    
    public String reMapAlignments(String otherAlignments, Map<Integer, Integer> map, int newIndexOfLine)
    {
        StringBuilder str = new StringBuilder();
        int l = newIndexOfLine;
        for(String line : otherAlignments.split("\n"))
        {
            String[] tokens = line.split(" ");
            str.append(l++);
            for(int i = 1; i < tokens.length; i++) // ignore first character which is the line number
            {                
                int oldId = Integer.valueOf(tokens[i]);
                // re-map ids
                str.append(" ").append(map.get(oldId));
            }
            str.append("\n");
        }
        return str.deleteCharAt(str.length()-1).toString();
    }
    
    public int[] getEventIds()
    {
        String[] events = getEventsArray();
        int[] ids = new int[events.length];
        int i = 0;
        for(String event : events)
        {
            String[] tokens = event.split("\t");
            // grab the id
            ids[i++] = Integer.valueOf(tokens[0].split(":")[1]);
        }
        return ids;
    }
    
    public String getName()
    {
        return records.length > 0 ? records[0] : "";
    }
    
    public String getText()
    {
        return records.length > 0 ? records[1] : "";
    }
    
    public int getNumberOfTextLines()
    {
        return records.length > 0 ? records[1].split("\n").length : 0;
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
    
    public String[] getTextSentences()
    {
        StringBuilder out = new StringBuilder();
        StringBuilder sentence = new StringBuilder();        
        for(String line : getTextArray())
        {
            sentence.append(line).append(" ");
            if(Utils.isSentencePunctuation(String.valueOf(line.charAt(line.length()-1))))
            {
                out.append(sentence.toString().trim()).append("\n");
                sentence = new StringBuilder();
            }
        }
        return out.deleteCharAt(out.length()-1).toString().split("\n");
    }
    
    public String[] getTextInOneLine()
    {
        StringBuilder str = new StringBuilder();
        for(String line : getTextArray())
            str.append(line).append(" ");
        return str.toString().trim().split(" ");
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
    
    /**
     * Exports an event3example into a string that contains the name, the text
     * split in sentences, and the elementary discourse units (EDUs), conforming
     * to the event3 ver.2 format.
     * 
     * @param alignments a string containing word-by-word record alignments 
     * @return a string in event3 ver.2 format containing the text and EDUs
     */
    public String exportToEdusFormat(String[] alignments) throws Exception
    {
        // compile text
        StringBuilder text = new StringBuilder();
        String[] sentences = getTextSentences();
        for(int i = 0; i < sentences.length -1; i++)
        {
            text.append(sentences[i]).append("<s>\n");
        }
        text.append(sentences[sentences.length-1]).append("<p>");
        // compile EDUS.
        // collapse text into a single line; number of words should match the number
        // of record alignments        
        String[] words = getTextInOneLine();
        // check that the number of words matches with the number of record alignments
        if(words.length != alignments.length)
            throw new Exception(getName() + ": Number of words does not match with the number of record alignments");
        // assign to each edu the substring of the text which corresponds to a 
        // sequence of consecutive record alignments
        StringBuilder edus = new StringBuilder();
        String prev = alignments[0];
        int prevIndex = 0;        
        for(int i = 1; i < alignments.length; i++)
        {
            if(!alignments[i].equals(prev))
            {
                edus.append(wordsToEdu(Arrays.copyOfRange(words, prevIndex, i))).append("\n");
                prevIndex = i;
            }
            prev = alignments[i];            
        }
        // don't forget the last edu
        edus.append(wordsToEdu(Arrays.copyOfRange(words, prevIndex, words.length))).append("\n");
        return String.format("$NAME\n%s\n$TEXT\n%s\n$EDUS\n%s", getName(), text, edus);
    }
    
    private String wordsToEdu(String[] words)
    {
        StringBuilder str = new StringBuilder("<edu>");
        for(int i = 0; i < words.length; i++)
        {
            String word = words[i];
            // in case we have two sentences spanned by the same record, we need to split.
            if(Utils.isSentencePunctuation(word) && i != words.length - 1)
                str.append(word).append("</edu>\n<edu>");   
            else
                str.append(word).append(" ");            
        }
        str.deleteCharAt(str.length()-1);
        str.append("</edu>");
        return str.toString();
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
