package at.ac.tuwien.dsg.salam.rest.resource;

import java.util.Calendar;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

// TODO: link with SALAM assignment

@ApiModel(value = "Assignment", description = "Collective assignment")
public class Assignment {
    @ApiModelProperty(value = "Assigned service", required = true) private String service;
    @ApiModelProperty(value = "Assigned peer", required = true) private Peer peer;
    @ApiModelProperty(value = "Assignment status", required = true) private Status status;

    private Calendar timeAssigned;
    private Calendar timeLastStarted = null; // last resumed (or started), to calculate current runningTime
    private Long lastRunningTime = 0L; // total running time (millisecond) before it was resumed (or if not paused before=0) 
    
    public enum Status {
        ASSIGNED, RUNNING, PAUSED, DELEGATED, FINISHED, TERMINATED
    }

    public Assignment() {
        this.status = Status.ASSIGNED;
        this.timeAssigned = Calendar.getInstance();
    }

    public Assignment(String service, Peer peer, Status status) {
        super();
        this.peer = peer;
        this.service = service;
        this.status = status;
        this.timeAssigned = Calendar.getInstance();
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Status getStatus() {
        return status;
    }
    
    public Calendar getTimeAssigned() {
        return timeAssigned;
    }

    public void setTimeAssigned(Calendar timeAssigned) {
        this.timeAssigned = timeAssigned;
    }

    // TODO: running time calculation should use monitoring directive
    public void setStatus(Status status) {
        switch (status) {
        case RUNNING:
            this.timeLastStarted = Calendar.getInstance();
            break;
        case PAUSED:
            // resuming, accumulate to lastRunningTime
            this.lastRunningTime += Calendar.getInstance().getTimeInMillis() - this.timeLastStarted.getTimeInMillis();                
            break;
        case DELEGATED:
        case TERMINATED:
        case FINISHED:
            if (this.status!=Status.PAUSED && this.timeLastStarted!=null) {
                this.lastRunningTime += Calendar.getInstance().getTimeInMillis() - this.timeLastStarted.getTimeInMillis();
            }
            break;
        case ASSIGNED:
            break;
        }
        this.status = status;
    }
    
    public String getRunningTime() {
        long runningTime = this.lastRunningTime;
        if (this.status==Status.RUNNING) {
            runningTime += Calendar.getInstance().getTimeInMillis() - this.timeLastStarted.getTimeInMillis();
        }
        
        if (runningTime==0) {
            return "-";
        }
        
        long hours = runningTime / (60 * 60 * 1000);
        long minutes = runningTime / (60 * 1000) % 60;
        long seconds = runningTime / (1000) % 60;
        
        String runtime = "";
        if (hours>0) runtime += hours + "h ";
        if (minutes>0) runtime += minutes + "m ";
        if (seconds>0) runtime += seconds + "s";
        
        return runtime;
    }

}
