package scu.common.sla;

import java.util.Comparator;

public class Objective {

    protected String name;
    protected Object value;
    protected Comparator<Object> comparator;
    
    public Objective(String name, Object value, Comparator<Object> comparator) {
        this.name = name;
        this.value = value;
        this.comparator = comparator;
    } 
    
    public boolean comply(Object obj) {
        return comparator.compare(obj, value)>=0;
    }

    public String getName() {
        return name;
    }

    
}
