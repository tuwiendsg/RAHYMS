package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.HashMap;

public class Data {
    
    String name = null;
    Double value = null;
    HashMap<String, Object> metaData = null;

    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }
    public HashMap<String, Object> getMetaData() {
        return metaData;
    }
    public Object getMetaData(String key) {
        return metaData.get(key);
    }
    public void setMetaData(HashMap<String, Object> metaData) {
        this.metaData = metaData;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public void setMetaData(String key, Object value) {
        if (metaData==null) {
            metaData = new HashMap<String, Object>();
        }
        metaData.put(key, value);
    }
    @Override
    public String toString() {
        return "Data [name=" + name + ", value=" + value + ", metaData="
                + metaData + "]";
    }
    
    public int getSize() {
        return 8 + name.length();
    }
}
