package induction.problem.event3;

/**
 *
 * @author sinantie
 */
public class Constants
{
    public static final int NaN = Integer.MAX_VALUE;    
    
  // Fast conversion of string into integer (return NaN if fail)
  public static int str2num(String s)
  {
      if(s.matches("\\D*"))
          return NaN;
//      return Integer.parseInt(s);
      int n = s.length();
      int i = 0;
      int sign = 1;
      if (i < n && s.charAt(i) == '-')
      {
          i++;
          sign = -1;
      }
      if (i == n)
      {
          return NaN;
      }
      int x = 0;
      while (i < n)
      {
          int d = s.charAt(i) - '0';
          if (d >= 0 && d <= 9)
          {
              x = x * 10 + d;
          }
          else
          {
              return NaN;
          }
          i++;
      }
      return sign*x;
  }

  public static int str2numOrFail(String s)
  {
      int x = Constants.str2num(s);
      if (x == NaN)
      {
          throw new NumberFormatException("Not a number: "+s);
      }
      return x;
  }

  public static boolean setContains(int set, int i)
  {
      return (set & (1 << i)) != 0;
  }

  public static boolean setContainsSet(int set, int subset)
  {
      return (set & subset) == subset;
  }


  public static int setAdd(int set, int i)
  {
      return set | (1 << i);
  }

  public static int setSize(int _set)
  {
      int set = _set;
      int count = 0;
      while (set != 0)
      {
          if ((set & 1) != 0)
          {
              count++;
          }
          set >>= 1;
      }
      return count;
  }

  public static String setstr(int n , int set)
  {
      StringBuffer buf = new StringBuffer();
      for(int i = 0; i < n; i++)
      {
          if(setContains(set, i))
          {
              buf.append('*');
          }
          else
          {
              buf.append('.');
          }
      }
      return buf.toString();
  }

  public static enum Compare {VALUE, LABEL};
  public static enum Justify {LEFT, CENTRE, RIGHT};
  public static enum TypeAdd {RANDOM, NOISE};
}
