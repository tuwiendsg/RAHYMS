package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Calendar;

/**
 * Created by karaoglan on 13/02/16.
 */
@ApiModel(value = "Graph", description = "Graph representation")
public class Graph {

    //todo brk needed?
    @ApiModelProperty(value = "Graph's id", required = true)
    private Integer id;

    @ApiModelProperty(value = "The date when the graph was submitted", required = true)
    private Calendar timeCreated;

    @ApiModelProperty(value = "The image of graph", required = true)
    private String image;

    public Graph() {
        this.timeCreated = Calendar.getInstance();
    }

    public Graph(Integer id, String image) {
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
