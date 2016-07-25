package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Calendar;

@ApiModel(value = "Simulation", description = "Simulation representation for starting simulation algorithm")
public class Simulation {

    @ApiModelProperty(value = "Simulation's object id", required = false)
    private String id;

    @ApiModelProperty(value = "The date when the simulation was submitted", required = true)
    private String timeCreated;

    @ApiModelProperty(value = "The date when the simulation is finished", required = false)
    private String timeFinished;

    @ApiModelProperty(value = "The name of the simulation", required = true)
    private String simulationName;

    @ApiModelProperty(value = "The description of the simulation", required = true)
    private String simulationDescription;

    @ApiModelProperty(value = "The path of CSV file which contains corresponding simulation graph parameters", required = false)
    private String filePath;

    public Simulation() {
        this.timeCreated = Calendar.getInstance().toString();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public String getSimulationDescription() {
        return simulationDescription;
    }

    public void setSimulationDescription(String simulationDescription) {
        this.simulationDescription = simulationDescription;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(String timeFinished) {
        this.timeFinished = timeFinished;
    }
}
