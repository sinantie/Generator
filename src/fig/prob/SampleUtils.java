package fig.prob;

import java.util.*;
import fig.basic.*;

public class SampleUtils {
  public static int[] samplePermutation(Random random, int n) {
    int[] perm = new int[n];
    for(int i = 0; i < n; i++) perm[i] = i;
    for(int i = 0; i < n-1; i++) {
      int j = i+random.nextInt(n-i);
      int tmp = perm[i]; perm[i] = perm[j]; perm[j] = tmp; // Swap
    }
    return perm;
  }
  public static int sampleMultinomial(Random random, double[] probs) {
    double v = random.nextDouble();
    double sum = 0;
    for(int i = 0; i < probs.length; i++) {
      sum += probs[i]; 
      if(v < sum) return i;
    }
    throw new RuntimeException(sum + " < " + v);
  }

  public static double[] sampleUnitVector(Random random, int n) {
    double[] x = new double[n];
    for(int i = 0; i < n; i++) x[i] = random.nextDouble()-0.5;
    double norm = NumUtils.l2Norm(x);
    for(int i = 0; i < n; i++) x[i] /= norm;
    return x;
  }

  public static double sampleGamma(Random random, double a, double rate) {
    // G. Marsaglia and W.W. Tsang, A simple method for generating gamma
    // variables, ACM Transactions on Mathematical Software, Vol. 26, No. 3,
    // Pages 363-372, September, 2000.
    // http://portal.acm.org/citation.cfm?id=358414
    double boost;
    if(a < 1) {
      // boost using Marsaglia's (1961) method: gam(a) = gam(a+1)*U^(1/a)
      boost = Math.exp(Math.log(random.nextDouble())/a);
      ++a;
    } 
    else {
      boost = 1;
    }

    double d = a-1.0/3, c = 1.0/Math.sqrt(9*d);
    double v;
    while(true) {
      double x;
      do {
        x = sampleGaussian(random);
        v = 1+c*x;
      } while(v <= 0);
      v = v*v*v;
      x = x*x;
      double u = random.nextDouble();
      if((u < 1-.0331*x*x) || (Math.log(u) < 0.5*x + d*(1-v+Math.log(v)))) {
        break;
      }
    }
    return boost*d*v / rate;
  }

  public static double sampleErlang(Random random, int ia, double rate) {
    int j;
    double am,e,s,v1,v2,x,y;

    assert ia >= 1;
    if (ia < 6) {
      x=1.0;
      for (j=1;j<=ia;j++) x *= random.nextDouble();
      x = -Math.log(x);
    } 
    else {
      do {
        do {
          do {
            v1=2.0*random.nextDouble()-1.0;
            v2=2.0*random.nextDouble()-1.0;
          } while (v1*v1+v2*v2 > 1.0);
          y=v2/v1;
          am=ia-1;
          s=Math.sqrt(2.0*am+1.0);
          x=s*y+am;
        } while (x <= 0.0);
        e=(1.0+y*y)*Math.exp(am*Math.log(x/am)-s*y);
      } while (random.nextDouble() > e);
    }
    return x / rate;
  }

  // Return Gaussian(0, 1)
  public static double sampleGaussian(Random random) {
    // Use the Box-Muller Transformation
    // if x_1 and x_2 are independent uniform [0, 1],
    // then sqrt(-2 ln x_1) * cos(2*pi*x_2) is Gaussian with mean 0 and variance 1
    double x1 = random.nextDouble(), x2 = random.nextDouble();
    double z = Math.sqrt(-2*Math.log(x1))*Math.cos(2*Math.PI*x2);
    return z;
  }

  // Copied from numerical recipes 
  private static double oldm = -1, g, sq, alxm;
  public static double samplePoisson(Random random, double rate) {
    double xm = rate;
    double em, t, y;

    if (xm < 12.0) {
      if (xm != oldm) {
        oldm=xm;
        g=Math.exp(-xm);
      }
      em = -1;
      t=1.0;
      do {
        em += 1.0;
        t *= random.nextDouble();
      } while (t > g);
    } 
    else {
      if (xm != oldm) {
        oldm=xm;
        sq=Math.sqrt(2.0*xm);
        alxm=Math.log(xm);
        g=xm*alxm-NumUtils.logGamma(xm+1.0);
      }
      do 
      {
        do 
        {
          y=Math.tan(Math.PI*random.nextDouble());
          em=sq*y+xm;
        } while (em < 0.0);
        em=Math.floor(em);
        t=0.9*(1.0+y*y)*Math.exp(em*alxm-NumUtils.logGamma(em+1.0)-g);
      } while (random.nextDouble() > t);
    }
    return (int)em;
  }

  public static void main(String[] args) {
    FullStatFig fig = new FullStatFig();
    Random random = new Random(1);
    for(int i = 0; i < 100000; i++)
      fig.add(samplePoisson(random, 4));
    System.out.println(fig);
  }
}
