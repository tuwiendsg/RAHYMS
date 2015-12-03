package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.HashMap;
import java.util.Map;

public class Quality {
    
    public static String RATE = "rate";
    public static String ACCURACY = "accuracy";
    public static String FRESHNESS = "freshness";
    
    private Map<String, Double> quality;
    
    public Quality() {
        quality = new HashMap<String, Double>();
    }
    
    public Quality(double rate, double accuracy, double freshness) {
        this();
        set(RATE, rate);
        set(ACCURACY, accuracy);
        set(FRESHNESS, freshness);
    }
    
    public Quality(Map<String, Object> params) {
        this();
        for (String key: params.keySet()) {
            Double value = (Double)params.get(key);
            set(key, value);
        }
    }

    public void set(String name, Double value) {
        quality.put(name, value);
    }
    
    public Double get(String name) {
        return quality.get(name);
    }
}
