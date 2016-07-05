package at.ac.tuwien.dsg.hcu.common.interfaces;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface WorkerManagerInterface {

    public void reserveAssignment(Assignment assignment);
    public void commitAssignment(Assignment assignment);
    
    public void pauseAssignment(Assignment assignment);
    public void resumeAssignment(Assignment assignment);
    public Assignment.Status queryAssignment(Assignment assignment);
    
    public void notifyTaskResult(Task task);
    public void notifyFinalizeAssignment(Assignment assignment);
    
    public void setScheduler(SchedulerInterface scheduler);
    
    // this is internal method for waking up scheduler
    public void wakeSchedulerAt(long time);
    
}
