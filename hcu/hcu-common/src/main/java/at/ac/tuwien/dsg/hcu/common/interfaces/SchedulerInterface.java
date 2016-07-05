package at.ac.tuwien.dsg.hcu.common.interfaces;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface SchedulerInterface {

    public void setWorkerManagerInterface(WorkerManagerInterface workerManager);
    public void setMonitorInterface(MonitorInterface monitor);
    public void setNegotiatorInterface(NegotiateInterface negotiator);
    
    public void submitTask(Task task);
    public void notifyExecutionResult(Assignment assignment, Assignment.Status status);
    public void notifyReservationResult(Assignment assignment, Assignment.Status status);

    // this is internal method for waking up scheduler
    public void wake();
}
