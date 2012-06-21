/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

/**
 *
 * @author sinantie
 */
public class Event
    {
        int id;
        EventType type;
        Field[] fields;

        public Event(int id, EventType type, Field... fields)
        {
            this.id = id;
            this.type = type;
            this.fields = fields;
        }

        @Override
        public String toString()
        {
            // preamble
            StringBuilder str = new StringBuilder(String.format(".id:%s\t.type:%s", id, type.getType()));
            for(Field f : fields)
                str.append("\t").append(f);
            return str.toString();
        }                
    }
