package induction.problem;

import org.apache.commons.math.linear.OpenMapRealVector;

/**
 *
 * @author sinantie
 */
public class VecFactory
{
    public static enum Type {DENSE, SPARSE};
    
    public static Vec zeros(Type type, int n)
    {
        return type == Type.DENSE ? new ProbVec(new double[n], 0, 0)
                                  : new SparseVec(new OpenMapRealVector(n), 0, 0);
    }   

    public static Vec[] zeros2(Type type, int n1, int n2)
    {
        Vec[] result = type == Type.DENSE ? new ProbVec[n1] : new SparseVec[n1];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros(type, n2);
        }
        return result;
    }    

    public static Vec[][] zeros3(Type type, int n1, int n2, int n3)
    {
        Vec[][] result = type == Type.DENSE ? new ProbVec[n1][n2] : new SparseVec[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            result[i] = zeros2(type, n2, n3);
        }
        return result;
    }
    
    public static Vec[][] zeros3(Type type, int n1, int n2, int[] n3)
    {
        Vec[][] result = type == Type.DENSE ? new ProbVec[n1][n2] : new SparseVec[n1][n2];
        for(int i = 0; i < n1; i++)
        {
            //result[i] = new Vec[n2];
            for(int j = 0; j < n2; j++)
            {
                result[i][j] = zeros(type, n3[i]);
            }
        }
        return result;
    }
    
    public static OpenMapRealVector copyFromArray(double[] values)
    {
        return new OpenMapRealVector(values);
    }
}
