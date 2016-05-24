package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Created by karaoglan on 16/04/16.
 */
@ApiModel(value = "Simulation Task", description = "Properties of Task for Simulation")
public class SimulationTask {

    @ApiModelProperty(value = "Task's name", required = true)
    private String name;

    @ApiModelProperty(value = "task's properties", required = true)
    private String task;

    @ApiModelProperty(value = "Tasks's objectId", required = false)
    private String id;

    public SimulationTask() {
    }

    @JsonCreator
    public SimulationTask(@JsonProperty("name") String name,
                          @JsonProperty("task") String task
                          ) {
        this.name = name;
        this.task = task;
    }

    public SimulationTask(String name, String task, String id) {
        this.name = name;
        this.task = task;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
