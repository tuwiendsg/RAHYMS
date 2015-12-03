package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.HashMap;
import java.util.Map;

public class Data implements Cloneable {
    
    String name = null;
    Object value = null;
    MetaData metaData = new MetaData();

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public MetaData getMetaData() {
        return metaData;
    }
    public Object getMetaData(String key) {
        return metaData.get(key);
    }
    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public void setMetaData(String key, Object value) {
        metaData.set(key, value);
    }
    @Override
    public String toString() {
        return "Data [name=" + name + ", value=" + value + ", metaData="
                + metaData + "]";
    }
    
    public int getSize() {
        return 8 + name.length();
    }
    
    public Double getDoubleValue() {
        Double doubleVal = null;
        try {
            doubleVal = (Double)value;
        } catch (ClassCastException e) {
            // try Integer
            try {
                Integer longVal = (Integer)value;
                doubleVal = longVal * 1.0;
            } catch (ClassCastException e2) {
                System.out.println(e2);
            }
        }
        return doubleVal;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Data result = (Data)super.clone();
        // TODO: check deep copying
        return result;
    }
    
}
