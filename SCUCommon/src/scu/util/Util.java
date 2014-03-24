package scu.util;

public class Util {

  public static String stringRepeat(String s, int repeat) {
    return new String(new char[repeat]).replace("\0", s);    
  }
  
  public static int Integer(Object o, int defaultValue) {
      int x = defaultValue;
      try {
          if (o instanceof String) {
              x = Integer.parseInt((String)o);
          } else {
              x = (Integer)o;
          }
      } catch (Exception e) {
      }
      return x;
  }

  public static int Integer(Object o) {
      return Integer(o, 0);
  }

  public static double Double(Object o, double defaultValue) {
      double x = defaultValue;
      try {
          if (o instanceof String) {
              x = Double.parseDouble((String)o);
              
          } else {
              x = (Double)o;
          }
      } catch (Exception e) {
      }
      return x;
  }

  public static double Double(Object o) {
      return Double(o, 0.0);
  }
}
