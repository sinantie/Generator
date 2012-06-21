/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.problem.event3.json;

import java.util.Properties;

/**
 *
 * @author sinantie
 */
public class Field<T>
{

    String name;
    T value;
    Properties dictionary;

    public Field(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    /*
     * Check whether value is in the dictionary and replace accordingly.
     * We assume T is of String type. 
     */
    public Field(String name, T value, Properties dictionary)
    {
        this(name, value);
        this.dictionary = dictionary;
    }

    @Override
    public String toString()
    {
        // Check whether value is in the dictionary and replace accordingly.
        // We assume T is of String type. 
        if (dictionary != null) {
            String dictValue = dictionary.getProperty((String) value);
            return (value instanceof Integer ? "#" : "@")
                    + String.format("%s:%s", name, dictValue == null ? value : dictValue);
        }
        else {
            return (value instanceof Integer ? "#" : "@")
                    + String.format("%s:%s", name, value);
        }
    }
}
