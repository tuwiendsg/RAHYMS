package at.ac.tuwien.dsg.salam.rest.resource;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.salam.common.model.ComputingElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Peer", description = "Peer representation")
public class Peer {
    
    @ApiModelProperty(value = "Peer's name", required = true) 
    private String name;
    
    @ApiModelProperty(value = "Peer's e-mail address", required = false)
    private String email;

    @ApiModelProperty(value = "Peer's rest address", required = false)
    private String rest;

    @ApiModelProperty(value = "Peer's provided services", required = true)
    private List<String> services;
    
    private Long elementId;

    public static enum CommType {
        EMAIL, REST
    }

    public Peer() {
    }

    public Peer(String param, CommType type) {
        super();
        switch (type) {
            case EMAIL:
                this.email = param;
                break;
            case REST:
                this.rest = param;
                break;
        }
        services = new ArrayList<String>();
    }

    public Peer(String name, String email) {
        this(email, CommType.EMAIL);
        this.name = name;
    }

    public Peer(String name, String email, List<String> services) {
        this(name, email);
        this.services = services;
    }

    public Peer(String name, String param, CommType type, List<String> services) {
        this(param, type);
        this.name = name;
        this.services = services;
    }

    public Peer(String name, String email, String rest, List<String> services) {
        this(name, email);
        this.rest = rest;
        this.services = services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public Long getElementId() {
        return elementId;
    }

    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }


}
