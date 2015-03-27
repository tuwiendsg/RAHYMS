package at.ac.tuwien.dsg.hcu.common.model;


public class SCU {

    Batch batch;
    Metrics metrics;
    Properties properties;
    
    public SCU() {
        this(new Batch());
    }

    public SCU(Batch batch) {
        this.batch = batch;
        metrics = new Metrics();
        properties = new Properties();
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    public Object getProperty(String name) {
        return getProperty(name, null);
    }

    public Object getProperty(String name, Object _default) {
        return getProperties().getValue(name, _default);
    }

    public void setProperty(String name, Object value) {
        getProperties().setValue(name, value);
    }

    public int getId() {
        return this.batch.getId();
    }

    public Task getTask() {
        return this.batch.getTask();
    }
    
    public Task.Status getStatus() {
        return this.getTask().getStatus();
    };
    
    public String debugUtilization() {
        String result = "";
        for (Assignment a: this.batch.getAssignments()) {
            if (!result.equals("")) {
                result += ",";
            }
            ComputingElement unit = a.getAssignee().getProvider();
            String unitDesc = String.format("%s[%s:%s]", unit.getName(), a.getRole(), a.getStatus());
            if (unit instanceof HumanComputingElement) {
                //result += unit.getName() + "," + unit.getProperty("AssignmentCount")  + "," + unit.getProperty("FinishedCount");
                result += unitDesc + "," + unit.getAssignmentCount()  + "," + unit.getFinishedCount();
            } else {
                result += unitDesc + "," + unit.getMetric("utilization", 0.0);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((batch == null) ? 0 : batch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SCU other = (SCU) obj;
        if (batch == null) {
            if (other.batch != null)
                return false;
        } else if (!batch.equals(other.batch))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SCU [" + batch + "]";
    }
    
    
    
}
