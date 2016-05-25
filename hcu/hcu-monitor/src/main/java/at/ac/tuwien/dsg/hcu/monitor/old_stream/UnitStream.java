package at.ac.tuwien.dsg.hcu.monitor.old_stream;

import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;

public class UnitStream extends BaseStream {

    protected ComputingElement unit;

    public UnitStream() {
    }
    
    public UnitStream(EventType type, double timestamp, ComputingElement unit) {
        super(type, timestamp);
        setUnit(unit);
    }

    public ComputingElement getUnit() {
        return unit;
    }

    public void setUnit(ComputingElement unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "UnitStream [unit=" + unit + ", type=" + type + ", timestamp="
                + timestamp + ", metricName=" + metricName + ", metricValue="
                + metricValue + "]";
    }
    
    

}
