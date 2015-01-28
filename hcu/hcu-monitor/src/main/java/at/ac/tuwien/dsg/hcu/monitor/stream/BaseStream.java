package at.ac.tuwien.dsg.hcu.monitor.stream;



public abstract class BaseStream {

    protected EventType type;
    protected double timestamp;
    
    // this can be used for metric event
    protected String metricName;
    protected Object metricValue;

    public BaseStream() {

    }

    public BaseStream(EventType type, double timestamp) {
        this.setType(type);
        this.setTimestamp(timestamp);
    }
    
    public EventType getType() {
        return type;
    }
    public void setType(EventType type) {
        this.type = type;
    }
    public double getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Object getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Object metricValue) {
        this.metricValue = metricValue;
    }

    
}
