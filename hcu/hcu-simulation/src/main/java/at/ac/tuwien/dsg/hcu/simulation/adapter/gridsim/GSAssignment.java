package at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim;

import gridsim.Gridlet;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;

public class GSAssignment extends Gridlet {

    protected Assignment assignment;
    
    public GSAssignment(Assignment assignment) {
        this((int)assignment.getId(), assignment.getRole().getLoad());
        this.assignment = assignment;
    }
    
    private GSAssignment(int taskId, double taskLength) {
        super(taskId, taskLength, 1, 1);
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    @Override
    public String toString() {
        return assignment.toString();
    }
    
}
