package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Simulation Unit", description = "Properties of Unit for Simulation")
public class SimulationUnit {

    @ApiModelProperty(value = "unit's mongod objectId", required = false)
    private String id;

    @ApiModelProperty(value = "unit's name", required = true)
    private String name;

    @ApiModelProperty(value = "unit's properties", required = true)
    private String unit;

    public SimulationUnit() {
    }

    @JsonCreator
    public SimulationUnit(@JsonProperty("name") String name,
                          @JsonProperty("unit") String unit
    ) {
        this.name = name;
        this.unit = unit;
    }

    public SimulationUnit(String name, String unit, String id) {
        this.name = name;
        this.unit = unit;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
