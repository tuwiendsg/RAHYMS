package at.ac.tuwien.dsg.hcu.rest.resource;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Calendar;

/**
 * Created by karaoglan on 07/10/15.
 */
@ApiModel(value = "Simulation", description = "Simulation representation")
public class Simulation {

    //todo @karaoglan needed?
    @ApiModelProperty(value = "Task's id", required = true)
    private Integer id;

    @ApiModelProperty(value = "The date when the simulation was submitted", required = true)
    private Calendar timeCreated;

    @ApiModelProperty(value = "The parameters of simulation", required = true)
    private String content;

    public Simulation() {
        this.timeCreated = Calendar.getInstance();
    }

    public Simulation(Integer id, String content) {
        super();
        this.id = id;
        this.timeCreated = Calendar.getInstance();
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Calendar getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Calendar timeCreated) {
        this.timeCreated = timeCreated;
    }
}
