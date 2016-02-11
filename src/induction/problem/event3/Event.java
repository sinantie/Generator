package induction.problem.event3;

import induction.Utils;
import java.io.File;
import java.util.List;

/**
 *
 * @author konstas
 */

// An event fills in the values for the event type.
// It is represented by an array:
//   str: field f -> value at field f (an index into the field's indexer)
//   num: numeric representation of the fields
public class Event
{
    private EventType eventType;
    private Field[] fields;
    int id, F;
    private List<Integer> values;

    public Event(int id, EventType eventType, List<Integer> values)
    {        
        this.id = id;
        this.eventType = eventType;
        fields = eventType.fields;
        F = fields.length;
        this.values = values;
    } 

    public String fieldToString(int i)
    {
        return eventType.fieldToString(i);
    }
    public int getEventTypeIndex()
    {
        return eventType.getEventTypeIndex();
    }

    public String getEventTypeName()
    {
        return eventType.getName();
    }

    public Field[] getFields()
    {
        return fields;
    }

    public int getF()
    {       
        return F;
    }

    public int getId()
    {
        return id;
    }

    public List<Integer> getValues()
    {
        return values;
    }

    public boolean containsEmptyValues()
    {
        int offset = 1;
        // in case event contains only categorical fields
        if(allCatFields())
        {
            for(int i = offset; i < values.size(); i++)
            {
                if(fields[i].valueToString(values.get(i)).equals("--"))
                    return true;
            }
            return false;
        }
        // in case event contains only numberical fields
        else if(allNumFields())
        {
            for(int i = offset; i < values.size(); i++)
            {
                if(fields[i].valueToString(values.get(i)).equals("0"))
                    return true;
            }
            return false;
        }
        else
        {
            boolean allEmpty = true;
            for(int i = offset; i < values.size(); i++)
            {
                if(!(fields[i].valueToString(values.get(i)).equals("0") ||
                     fields[i].valueToString(values.get(i)).equals("--"))
                  )
                {
                    allEmpty = false;
                    break;
                }
            }
            return allEmpty;
        }
    }

    private boolean allCatFields()
    {
        for(int i = 0; i < fields.length; i++)
            if(!(fields[i] instanceof CatField))
                return false;
        return true;
    }

    private boolean allNumFields()
    {
        for(int i = 0; i < fields.length; i++)
            if(!(fields[i] instanceof NumField))
                return false;
        return true;
    }

    public boolean fieldContainsEmptyValue(int field)
    {
        // check if none_field
        if(field == F + 1)
            return true;
        boolean isNumType = fields[field] instanceof NumField;
        return fields[field].valueToString(values.get(field)).equals(isNumType ? "0" : "--");
    }
    
    public String fieldValueToString(int field)
    {
        return fields[field].valueToString(values.get(field));
    }
    
    @Override
    public String toString()
    {
        String[] out = new String[F];
        for(int i = 0; i < F; i++)
        {
            out[i] = fieldToString(i) + "=" + fields[i].valueToString(values.get(i));
        }
//        return model.eventTypeStr(eventTypeIndex) + ":" + Utils.mkString(out, ",");
        return eventType +"("+ id +"):" + Utils.mkString(out, ",");
    }
}
