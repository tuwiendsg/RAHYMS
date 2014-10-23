package at.ac.tuwien.dsg.salam.simulation.adapter.gridsim;

import gridsim.Gridlet;
import at.ac.tuwien.dsg.salam.common.model.Task;

public class GSTask extends Gridlet {

    protected Task task;
    
    public GSTask(Task task) {
        this((int)task.getId(), task.getLoad());
        this.task = task;
    }
    
    private GSTask(int taskId, double taskLength) {
        super(taskId, taskLength, 1, 1);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return task.toString();
    }
    
}
