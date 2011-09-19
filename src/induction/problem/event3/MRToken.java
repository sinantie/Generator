package induction.problem.event3;

import induction.problem.event3.Event3Model;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author konstas
 */
public class MRToken
{
    private Event3Model model;
    private int event;
    private HashMap<Integer, ArrayList<MRField>> fields;
    public enum Type {cat, num};

    /**
     * Constructor used for Gold-standard MRs
     * @param event
     */
    public MRToken(int event)
    {
        this.event = event;
        this.fields = new HashMap<Integer, ArrayList<MRField>>();
    }

    /**
     * Constructor used for MRs generated by the model
     * @param model reference to the Event3Model.
     * Used only in order to extract none_f for each event, via fieldsMap
     * @param event
     */   
    public MRToken(Event3Model model, int event)
    {
        this(event);
        this.model = model;
    }

    public int getEvent()
    {
        return event;
    }

    public Collection<Integer> getFieldIds()
    {
        return fields.keySet();
    }

    public Collection<Integer> getValuesOfField(int id)
    {
        // a field should have a single value only. However, the generative
        // model may wrongly emit more than one value for a particular field
        ArrayList<MRField> mrFields = this.fields.get(id);
        Collection<Integer> values = new ArrayList<Integer>(1);
        for(MRField f : mrFields)
        {
            values.add(f.value);
        }
        return values;
    }

    public boolean isEmpty()
    {
        for(Integer fieldId : fields.keySet())
        {
            if(fieldId != model.getFieldsMap().get(event).get("none_f"))
            {
                return false;
            }
        }
        return true;
    }

    private void addField(int fieldId)
    {
        if(!fields.containsKey(fieldId))
            fields.put(fieldId, new ArrayList<MRField>());
    }

    private void addValue(int fieldId, Type type, int value)
    {
        fields.get(fieldId).add(new MRField(type, value));
    }

    public void parseMrToken(int curEvent, int curField, Type type, int curValue)
    {
//        if(curField < ex.events[curEvent].F)
        if(curField < model.getFieldsMap().get(curEvent).get("none_f"))
        {
            addField(curField);
            if(curValue > -1)
            {
                addValue(curField, type, curValue);
            }
        }
    }

    public void parseMrToken(int curField, Type type, int curValue)
    {
        addField(curField);
        addValue(curField, type, curValue);
    }

    @Override
    public boolean equals(Object obj)
    {
        assert obj instanceof MRToken;
        MRToken mr = (MRToken)obj;
        if(this.event != mr.event)
            return false;
        if(this.fields.size() != mr.fields.size())
            return false;
        for(Integer fieldId : this.fields.keySet())
        {
            if(!mr.fields.containsKey(fieldId))
                return false;
            // normally not a list
            ArrayList<MRField> thisFields = fields.get(fieldId);
            ArrayList<MRField> mrFields = mr.fields.get(fieldId);
            // change that, very naive
            if(thisFields.size() != mrFields.size())
                return false;
            for(MRField field : thisFields)
            {
                if(!mrFields.contains(field))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + this.event;
        hash = 79 * hash + (this.fields != null ? this.fields.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        String out = event + "[";
        for(Entry<Integer, ArrayList<MRField>> entry : fields.entrySet())
        {
            out += entry.getKey() + entry.getValue().toString();

        }
        return out + "]";
    }


    class MRField
    {
        Type type;
        int value;

        public MRField(Type type, int value)
        {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof MRField;
            MRField f = (MRField) obj;
            if(type == Type.num)
                return Math.abs(value - f.value) <= 10;
            return value == f.value;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 19 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 19 * hash + this.value;
            return hash;
        }

        @Override
        public String toString()
        {
            String out = "";
            if(type == Type.cat)
                out += "@" + value;
            else if(type == Type.num)
                out += "#" + value;
            return out;
        }


    }
}