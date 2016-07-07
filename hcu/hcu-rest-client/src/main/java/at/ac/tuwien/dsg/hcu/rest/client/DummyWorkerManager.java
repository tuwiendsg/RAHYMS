package at.ac.tuwien.dsg.hcu.rest.client;

import java.util.Map;

import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.WorkerManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Assignment.Status;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public class DummyWorkerManager implements WorkerManagerInterface {

    SchedulerInterface scheduler;
    
    @Override
    public void reserveAssignment(Assignment assignment) {
        //System.out.println("Dummy got reserveAssignment");
    }

    @Override
    public void commitAssignment(Assignment assignment) {
        //System.out.println("Dummy got commitAssignment");
        // return success immediately
        assignment.setStatus(Status.SUCCESSFUL);
        scheduler.notifyExecutionResult(assignment, Status.SUCCESSFUL);
    }

    @Override
    public void pauseAssignment(Assignment assignment) {
    }

    @Override
    public void resumeAssignment(Assignment assignment) {
    }

    @Override
    public Status queryAssignment(Assignment assignment) {
        return null;
    }

    @Override
    public void notifyTaskResult(Task task) {
    }

    @Override
    public void notifyFinalizeAssignment(Assignment assignment) {
    }

    @Override
    public void setScheduler(SchedulerInterface scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void wakeSchedulerAt(long time) {
    }

    @Override
    public void setConfiguration(Map<String, Object> config) {
    }

}
