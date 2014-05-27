package scu.common.model;

import java.util.ArrayList;

import scu.common.sla.Specification;

public class Task {

    protected long id;
    protected double load;
    protected TaskType type;
    protected String name;
    protected String description;
    protected long submissionTime;

    protected Task parent;
    protected ArrayList<Task> subTasks;

    protected ArrayList<Role> roles;
    protected Specification specification;
    protected TaskDependency dependency;
    protected TaskPresentation presentation;
    protected Reward reward;
    
    public Task(long id, double load, Task parent, TaskType type) {
        this(id, type.getName(), type.getDescription(), load, parent, type);
    }
    
    public Task(long id, String name, String description, double load, 
            Task parent, TaskType type) {
        super();
        this.id = id;
        this.load = load;
        this.type = type;
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.subTasks = new ArrayList<Task>();
        
        // clone properties from task type
        this.roles = (ArrayList<Role>) type.getRoles().clone();
        this.dependency = (TaskDependency) type.getDependency().clone();
        this.presentation = (TaskPresentation) type.getPresentation().clone();
        this.reward = (Reward) type.getReward().clone();
    }

    public Task(long id, String name, String description, double load, Task parent) {
        super();
        this.id = id;
        this.load = load;
        this.type = null;
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.subTasks = new ArrayList<Task>();
        this.roles = new ArrayList<Role>();
        this.dependency = new TaskDependency();
        this.presentation = new TaskPresentation();
        this.reward = new Reward();
    }

    public Task() {
        this.subTasks = new ArrayList<Task>();
        this.roles = new ArrayList<Role>();
        this.dependency = new TaskDependency();
        this.presentation = new TaskPresentation();
        this.reward = new Reward();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public ArrayList<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(ArrayList<Task> subTasks) {
        this.subTasks = subTasks;
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

    public Role getRole(Functionality functionality) {
        int index = roles.indexOf(new Role(functionality));
        Role r = null;
        if (index>-1) {
            r = roles.get(index);
        } 
        return r;
    }

    public void addUpdateRole(Role role) {
        int index = roles.indexOf(role);
        if (index>-1) {
            // replace
            roles.set(index, role);
        } else {
            // add
            roles.add(role);
        }
    }
    
    public void removeRole(Role role) {
        roles.remove(role);
    }
    
    /**
     * Recursively get the list of the roles of this task type and its descendents
     * @return ArrayList<Role>
     */
    public ArrayList<Role> getAllRoles() {
        ArrayList<Role> clone = ((ArrayList<Role>) this.roles.clone());
        for (Task child : subTasks) {
            ArrayList<Role> childRoles = child.getAllRoles();
            clone.addAll(childRoles);
        }
        return clone;
    }

    public TaskDependency getDependency() {
        return dependency;
    }

    public void setDependency(TaskDependency dependency) {
        this.dependency = dependency;
    }

    public TaskPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(TaskPresentation presentation) {
        this.presentation = presentation;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }
    
    public void addSubTask(Task task) {
        subTasks.add(task);
    }

    public void addSubTask(long id, double load, TaskType taskType) {
        Task newTask = new Task(id, load, this, taskType);
        subTasks.add(newTask);
    }

    public Task getRoot() {
        Task t = this;
        while (t.getParent()!=null) {
            t = t.getParent();
        }
        return t;
    }
    
    public long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }
    
    public Object getObjectiveValue(String name) {
        return specification.findObjective(name).getValue();
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", load=" + load + ", name=" + name
                + ", roles=" + roles + ", subTasks=" + subTasks
                + ", spec=" + specification + "]";
    }

    /**
     * Tool to instantiate tasks from a TaskType.
     * Note: this can be implementation specific.
     */
    
    
}
