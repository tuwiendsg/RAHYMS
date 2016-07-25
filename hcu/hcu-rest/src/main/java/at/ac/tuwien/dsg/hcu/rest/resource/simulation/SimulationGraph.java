package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Calendar;

@ApiModel(value = "SimulationGraph", description = "SimulationGraph representation")
public class SimulationGraph {

    @ApiModelProperty(value = "SimulationGraph's mongo id", required = true)
    private String id;

    @ApiModelProperty(value = "The date when the graph was submitted", required = true)
    private Calendar timeCreated;

    @ApiModelProperty(value = "The image of graph", required = true)
    private String image;

    public SimulationGraph() {
        this.timeCreated = Calendar.getInstance();
    }

    public SimulationGraph(String id, String image) {
        super();
        this.id = id;
        this.timeCreated = Calendar.getInstance();
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Calendar getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Calendar timeCreated) {
        this.timeCreated = timeCreated;
    }
}
