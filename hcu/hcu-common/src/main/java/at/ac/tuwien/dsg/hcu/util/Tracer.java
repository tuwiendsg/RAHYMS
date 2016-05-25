package at.ac.tuwien.dsg.hcu.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Tracer {

    private FileWriter fstream;
    private BufferedWriter out;
    private String fileName;
    
    private boolean uniqueMode = false;
    private static List<String> cache; 
    
    private static ConcurrentMap<String, Tracer> tracers = new ConcurrentHashMap<String, Tracer>();
    
    public Tracer() {}
    
    public Tracer(String file) {
        try {
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            this.fileName = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void trace(String text) {
        try {
            out.write(text);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void traceln(String text) {
        if (uniqueMode) {
            String hash = DigestUtils.md5Hex(text);
            if (!cache.contains(hash)) {
                trace(text + "\n");
                cache.add(hash);
            }
        } else {
            trace(text + "\n");
        }
    }

    public String getTraceHeader() {
        return "";
    }

    public void close() {
        try {
            out.close();
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Tracer createTracer(String name, String filePrefix, Class clazz, String header, boolean uniqueMode) {
        
        // init tracer
        Util.log().info("Initiating " + name + " tracer");
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        Date now = new Date();
        String date = sdfDate.format(now);  
        Tracer tracer = null;
        try {
            // instantiate
            if (clazz==null) {
                tracer = new Tracer(filePrefix + date + ".csv");
            } else {
                tracer = (Tracer) clazz
                        .getConstructor(new Class[]{String.class})
                        .newInstance(filePrefix + date + ".csv");
            }
            tracer.setUniqueMode(uniqueMode);
            // trace header
            tracer.traceln(header!=null ? header : tracer.getTraceHeader());
            // add to tracer list
            if (tracer!=null) {
                tracers.put(name, tracer);
            }
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        
        return tracer;
    }
    
    public static void initFromConfig(ConfigJsonArray config) {
        JSONArray root = config.getRoot();
        for (int i=0; i<root.length(); i++) {
            
            // read config
            JSONObject tracerCfg = root.getJSONObject(i);
            String name = tracerCfg.has("name") ? tracerCfg.getString("name") : null;
            if (name==null) {
                continue;
            }
            String filePrefix = tracerCfg.getString("file_prefix");
            String clazz = tracerCfg.getString("class");
            String header = tracerCfg.has("header") ? tracerCfg.getString("header") : null;
            boolean uniqueMode = tracerCfg.has("unique") ? tracerCfg.getBoolean("unique") : false;
            
            // create tracer
            try {
                createTracer(name, filePrefix, Class.forName(clazz), header, uniqueMode);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }
    }

    public static void trace(String name, String text) {
        Tracer tracer = tracers.get(name);
        if (tracer!=null) {
            tracer.trace(text);
        }
    }
    
    public static void traceln(String name, String text) {
        Tracer tracer = tracers.get(name);
        if (tracer!=null) {
            tracer.traceln(text);
        }
    }
    
    public static Tracer getTracer(String name) {
        return tracers.get(name);
    }

    public boolean isUniqueMode() {
        return uniqueMode;
    }

    public void setUniqueMode(boolean uniqueMode) {
        this.uniqueMode = uniqueMode;
        cache = new LinkedList<String>();
    }

    public static List<String> getCache() {
        return cache;
    }

}
