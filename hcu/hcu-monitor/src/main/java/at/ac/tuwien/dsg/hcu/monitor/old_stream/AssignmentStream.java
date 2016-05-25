package at.ac.tuwien.dsg.hcu.monitor.old_stream;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public class AssignmentStream extends BaseStream {

    protected Assignment assignment;
    protected Service service;
    protected ComputingElement unit;
    protected Task task;

    public AssignmentStream() {
    }

    public AssignmentStream(EventType type, double timestamp, Assignment assignment) {
        super(type, timestamp);
        this.setAssignment(assignment);
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
        this.setService(assignment.getAssignee());
        this.setTask(assignment.getTask());
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
        this.setUnit(service.getProvider());
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ComputingElement getUnit() {
        return unit;
    }

    public void setUnit(ComputingElement unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "AssignmentStream [type=" + type + ", task=" + task + ", assignment=" + assignment + "]";
    }



}
