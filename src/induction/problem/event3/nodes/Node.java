package induction.problem.event3.nodes;

/**
 *
 * @author konstas
 */
public class Node {    
    int i, j;
    Node(int i) {this.i = this.j = i;}
    Node(int i, int j) {this.i = i; this.j = j;}

    public double getLMScore(int rank)
    {
        return 1.0d;
    }

    public int getI()
    {
        return i;
    }

    public String debug(String simpleName, Object... args)
    {
        String s = "";
        for(int k = 0; k < args.length; k++)
        {
            s += "%s,";
        }
        return String.format(simpleName + "(" + s.substring(0, s.length() - 1) + ")", args);
    }
}