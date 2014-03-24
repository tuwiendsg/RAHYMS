package scu.adapter.gridsim;

import scu.common.model.Task;
import gridsim.Gridlet;

public class GSTask extends Gridlet {

    protected Task task;
    
    public GSTask(Task task) {
        this((int)task.getId(), task.getLoad());
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
