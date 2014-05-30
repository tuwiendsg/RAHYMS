package scu.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Util {

    private static Properties properties = null;
    private static String lastPropFile = "";

    private static void initProperties(String file) {
        if (properties==null || !lastPropFile.equals(file)) {
            properties = new Properties(); 
            try { 
                properties.load(new FileInputStream(file));  
                lastPropFile = file;
            } catch (IOException e) { 
                System.out.println(e);
            }
        }
    }

    public static String getProperty(String file, String key) {
        initProperties(file);
        return properties.getProperty(key);
    }

    public static String stringRepeat(String s, int repeat) {
        return new String(new char[(int) repeat]).replace("\0", s);    
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
