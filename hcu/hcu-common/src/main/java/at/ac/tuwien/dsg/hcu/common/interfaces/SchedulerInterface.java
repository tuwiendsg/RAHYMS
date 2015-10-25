package at.ac.tuwien.dsg.hcu.common.interfaces;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface SchedulerInterface {

    public void setCloudUserInterface(CloudUserInterface cloudUserInterface);
    public void setMonitorInterface(MonitorInterface monitorInterface);
    
    public void submitTask(Task task);
    public void notifyExecutionResult(Assignment assignment, Assignment.Status status);
    public void notifyReservationResult(Assignment assignment, Assignment.Status status);

    // this is internal method for waking up scheduler
    public void wake();
}