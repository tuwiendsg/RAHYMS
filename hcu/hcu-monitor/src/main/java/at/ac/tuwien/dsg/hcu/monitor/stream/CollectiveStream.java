package at.ac.tuwien.dsg.hcu.monitor.stream;

import at.ac.tuwien.dsg.hcu.common.model.SCU;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public class CollectiveStream extends BaseStream {

    protected Task task;
    protected SCU collective;

    public CollectiveStream() {
    }

    public CollectiveStream(EventType type, double timestamp, SCU collective) {
        super(type, timestamp);
        this.setCollective(collective);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public SCU getCollective() {
        return collective;
    }

    public void setCollective(SCU collective) {
        this.collective = collective;
        this.task = collective.getTask();
    }

    @Override
    public String toString() {
        return "CollectiveStream [type=" + type + ", metric(" + metricName + ")=" + metricValue + ", collective=" + collective + "]";
    }

    
    

}
