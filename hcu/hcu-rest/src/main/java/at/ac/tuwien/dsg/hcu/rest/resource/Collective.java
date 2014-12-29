package at.ac.tuwien.dsg.hcu.rest.resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Collective", description = "Collective representation")
public class Collective {
    @ApiModelProperty(value = "Collective's id", required = true) private Integer id;
    @ApiModelProperty(value = "The date when the collective was assembled", required = true) private Calendar timeCreated;
    @ApiModelProperty(value = "Collective's task", required = true) private Task task;
    @ApiModelProperty(value = "Collective's assignments", required = true) private List<Assignment> assignments;
    @ApiModelProperty(value = "Collective status", required = true) private Status status;

    public enum Status {
        RUNNING, FINISHED, TERMINATED
    }

    public Collective() {
        this.assignments = new ArrayList<Assignment>(); 
        this.timeCreated = Calendar.getInstance();
    }

    public Collective(Integer id, Task task, List<Assignment> assignments) {
        super();
        this.id = id;
        this.task = task;
        this.assignments = assignments;
        this.timeCreated = Calendar.getInstance();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void addAssignment(String service, Peer peer) {
        this.assignments.add(new Assignment(service, peer, Assignment.Status.ASSIGNED));
    }

    public String getTimeCreated() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
        return sdf.format(timeCreated.getTime());
    }

    public void setTimeCreated(Calendar timeCreated) {
        this.timeCreated = timeCreated;
    }
    
    public Status getStatus() {
        // TODO: define terminated status
        Status status = Status.FINISHED;
        for (Assignment a: this.assignments) {
            if (a.getStatus()!=Assignment.Status.FINISHED && a.getStatus()!=Assignment.Status.DELEGATED) {
                status = Status.RUNNING;
            }
        }
        return status;
    }
}
