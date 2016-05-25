package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.HashMap;
import java.util.Map;

public class MetaData implements Cloneable {
    Map<String, Object> metaData = new HashMap<String, Object>();
    
    public Object get(String key) {
        return metaData.get(key);
    }
    
    public Object getOrDefault(String key, Object defaultValue) {
        return metaData.getOrDefault(key, defaultValue);
    }

    public Double getDoubleOrDefault(String key, Double defaultValue) {
        Double value = null;
        try {
            Object origValue = metaData.get(key);
            if (origValue==null) {
                value = defaultValue;
            }
            else if (origValue instanceof String) {
                value = new Double((String)origValue);
            } else {
                value = (Double)metaData.getOrDefault(key, defaultValue);
            }
        } catch (Exception e) {
            System.err.println(metaData.get(key) + " cant be converted to Double");
            value = defaultValue;
        }
        return value;
    }

    public Integer getIntegerOrDefault(String key, Integer defaultValue) {
        Integer value = null;
        try {
            Object origValue = metaData.get(key);
            if (origValue==null) {
                value = defaultValue;
            }
            else if (origValue instanceof String) {
                value = new Integer((String)origValue);
            } else {
                value = (Integer)metaData.getOrDefault(key, defaultValue);
            }
        } catch (Exception e) {
            System.err.println(metaData.get(key) + " cant be converted to Integer");
            value = defaultValue;
        }
        return value;
    }

    public void set(String key, Object value) {
        metaData.put(key, value);
    }
    
    public void setAll(Map<String, Object> data) {
        metaData.putAll(data);
    }
    
    public String getId() {
        return (String)metaData.get("id");
    }

    public Double getTime() {
        return (Double)metaData.get("time");
    }

    @Override
    public String toString() {
        return metaData.toString();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MetaData result = (MetaData)super.clone();
        result.metaData = new HashMap<String, Object>(this.metaData);
        return result;
    }
}
