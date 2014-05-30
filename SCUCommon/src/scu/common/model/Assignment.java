package scu.common.model;

import scu.common.exceptions.NotFoundException;

public class Assignment {
    
    protected Service assignee;
    protected Task task;
    protected Role role;
    
    public Assignment(Service assignee, Task task, Functionality functionality) 
            throws NotFoundException {
        this.assignee = assignee;
        this.task = task;
        setRoleByFunctionality(functionality);
    }

    public Assignment(Service assignee, Task task, Role role) {
        this.assignee = assignee;
        this.task = task;
        this.role = role;
    }
    
    public Assignment() {
    }

    public Service getAssignee() {
        return assignee;
    }

    public void setAssignee(Service assignee) {
        this.assignee = assignee;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public void setRoleByFunctionality(Functionality functionality) 
            throws NotFoundException {
        Role r = task.getRole(functionality);
        if (r==null) throw new NotFoundException();
        else this.role = r;        
    }

    @Override
    public String toString() {
        return "Assignment [assignee=" + assignee.getDescription() 
                + ", task=" + task.getDescription()
                + ", role=" + role.getFunctionality() + "]";
    }
    
    

}
