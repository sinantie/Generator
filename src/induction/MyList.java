package induction;

import java.util.ArrayList;

/**
 *
 * @author konstas
 */
public class MyList<T> extends ArrayList<T[]>
{    

    @Override
    public boolean add(T... element)
    {
        return super.add(element);
    }

    public String toString(String tokenDelim, String elementDelim)
    {
        String out = "";
        for(T[] element : subList(0, size()))
        {
            for(int i = 0; i < element.length - 1; i++)
            {
                out += element[i] + tokenDelim;
            }
            out += element[element.length -1] + elementDelim;
        }
        return out.substring(0,out.length() - elementDelim.length());
    }
}
