package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Calendar;

/**
 * Created by karaoglan on 07/10/15.
 */
@ApiModel(value = "Simulation", description = "Simulation representation")
public class Simulation {

    @ApiModelProperty(value = "Simulation's id", required = true)
    private String id;

    @ApiModelProperty(value = "The date when the simulation was submitted", required = true)
    private String timeCreated;

    @ApiModelProperty(value = "The path of CSV file which contains corresponding simulation graph parameters")
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

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}
