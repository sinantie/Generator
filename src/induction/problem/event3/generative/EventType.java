package induction.problem.event3.generative;

import induction.problem.event3.fields.Field;
import java.io.Serializable;

/**
 *
 * @author konstas
 */
public class EventType implements Serializable
{
    static final long serialVersionUID = -7943211932504200961L;
    private int eventTypeIndex;
    protected String name;
    protected Field[] fields;
    protected int F, none_f, boundary_f;
    protected boolean useFieldSets;

    public EventType() {}
    public EventType(String[] useFieldSetsOnEventTypes, int eventTypeIndex, String name, Field[] fields)
    {
        this.eventTypeIndex = eventTypeIndex;
        this.name = name;
        this.fields = fields;
        F = fields.length;
        none_f = F;
        boundary_f = F + 1;
        for(String el : useFieldSetsOnEventTypes)
        {
            if(el.equals("ALL") || el.equals(name))
            {
                useFieldSets = true;
                break;
            }
        } // for
    }

    public int getEventTypeIndex()
    {
        return eventTypeIndex;
    }

    public String getName()
    {
        return name;
    }

    public int getF()
    {
        return F;
    }

    public int getNone_f()
    {
        return none_f;
    }

    public int getBoundary_f()
    {
        return boundary_f;
    }

    public Field[] getFields()
    {
        return fields;
    }

    public String fieldToString(int f)
    {
        if (f == none_f)
        {
            return "(none)";
        }
        else if(f == boundary_f)
        {
            return "(boundary)";
        }
        return fields[f].getName();
    }

    @Override
    public String toString()
    {
        return name;
    }


}
