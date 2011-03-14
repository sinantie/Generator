package induction.problem.event3;

import induction.Utils;
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

    public List<Integer> getValues()
    {
        return values;
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
