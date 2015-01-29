package at.ac.tuwien.dsg.hcu.util;

import gridsim.parallel.log.LogFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Util {

    private static Properties properties = null;
    private static String lastPropFile = "";

    private static Properties initProperties(String file) {
        if (properties==null || !lastPropFile.equals(file)) {
            properties = new Properties(); 
            try { 
                properties.load(new FileInputStream(file));  
                lastPropFile = file;
            } catch (IOException e) { 
                System.out.println(e);
            }
        }
        return properties;
    }

    public static String getProperty(String file, String key) {
        if (initProperties(file)==null) {
            return null;
        }
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
            } else if (o instanceof Integer) {
                x = (Integer)o * 1.0;
            } else  {
                x = (Double)o;
            }
        } catch (Exception e) {
        }
        return x;
    }

    public static double Double(Object o) {
        return Double(o, 0.0);
    }
    
    public static Logger log(String loggerName) {
    	Logger log = LogManager.getLogManager().getLogger(loggerName);
    	if (log==null) {
    		log = Logger.getLogger(loggerName);
    		// remove existing handler
    		log.setUseParentHandlers(false);
			for(Handler handler : log.getHandlers()){
				log.removeHandler(handler);
			}
			// add new handler
    		Handler hd = new ConsoleHandler();
			hd.setFormatter(new LogFormatter());
			log.addHandler(hd);
    		LogManager.getLogManager().addLogger(log);
    	}
        return log;
    }

    public static Logger log() {
    	Logger log = log(Util.class.getName());
    	log.setLevel(Level.WARNING);
    	return log;
    }
}
