package at.ac.tuwien.dsg.hcu.common.model;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Properties {
    
    protected Map<String, Object> valueSet;

    public Properties() {
        valueSet = new HashMap<String, Object>();
    }

    public String toString() {
      String result = "";
      for (String name : valueSet.keySet()) {
        if (!result.equals("")) result += ",";
        result += valueSet.get(name).toString();
      }
      result = "{" + result + "}";
      //return result;
      return valueSet.toString();
    }
    
    public Object getValue(String name, Object _default) {
        if (!valueSet.containsKey(name)) return _default;
        else return valueSet.get(name);
    }
    
    public void setValue(String name, Object value) {
        valueSet.put(name, value);
    }
    
    public Map<String, Object> getValueSet() {
        return valueSet;
    }
    
    public void setValueSet(Map<String, Object> valueSet) {
        this.valueSet = valueSet;
    }

    public boolean has(String name) {
        return valueSet.containsKey(name);
    }
}
