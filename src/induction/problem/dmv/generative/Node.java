package induction.problem.dmv.generative;


/**
 *
 * @author konstas
 */
public class Node {    
    int i, j, zi, zj;

    public Node(int i, int j, int zi, int zj)
    {
        this.i = i;
        this.j = j;
        this.zi = zi;
        this.zj = zj;
    }        

    public double getLMScore(int rank)
    {
        return 1.0d;
    }

    public int getI()
    {
        return i;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Node))
            return false;
        Node node = (Node) obj;
        return i == node.i && j == node.j && zi == node.zi && zj == node.zj;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + this.i;
        hash = 37 * hash + this.j;
        hash = 37 * hash + this.zi;
        hash = 37 * hash + this.zj;
        return hash;
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
    
    @Override
    public String toString() {
        return debug(this.getClass().getSimpleName(), i, j, zi, zj);
    }
}