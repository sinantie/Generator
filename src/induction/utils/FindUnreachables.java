package induction.utils;

import induction.Utils;

/**
 *
 * @author konstas
 */
public class FindUnreachables
{
    public static void main(String[] args)
    {
        String path = "robocupLists/robocupAllPathsTrainAlign";
        for(String file : Utils.readLines(path))
        {
            for(String fileLine : Utils.readLines(file))
            {
                if(fileLine.contains("-1"))
                    System.out.println(file);
            }
        }
    }
}
