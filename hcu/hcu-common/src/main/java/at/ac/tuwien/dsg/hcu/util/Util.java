package at.ac.tuwien.dsg.hcu.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gridsim.parallel.log.LogFormatter;

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
        //log.setLevel(Level.INFO);
        return log;
    }

    public static Object eval(String expression, Object... arguments) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        for (int i=0; i<arguments.length; i++) {
            expression = expression.replaceAll("%" + (i+1), arguments[i].toString());
        }
        return engine.eval(expression);
    }

    public static Method getMethod(String function) {

        // get class name and method name
        int dotPos = function.lastIndexOf(".");
        String className = function.substring(0, dotPos);
        String methodName = function.substring(dotPos+1);

        Method method = null;
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();
            for (Method m: methods) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method==null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            //method = clazz.getMethod(methodName, String.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static String dumpObjectToJson(Object obj) {
        String jsonInString = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonInString = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonInString;
    }
    
    public static Map jsonObjectToMap(JSONObject json) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }}
