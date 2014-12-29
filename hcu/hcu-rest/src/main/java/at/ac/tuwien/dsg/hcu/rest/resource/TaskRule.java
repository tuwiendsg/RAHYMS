package at.ac.tuwien.dsg.hcu.rest.resource;

import java.util.List;

import at.ac.tuwien.dsg.hcu.rest.resource.Task.SeverityLevel;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskRule", description = "Rules for mapping a task request to an HCU task")
public class TaskRule {

    @ApiModelProperty(value = "Task generator rule id", required = true)
    private Integer id; 

    @ApiModelProperty(value = "Task generator rule condition", required = true)
    private Condition condition; 

    @ApiModelProperty(value = "Task generator rule consequence", required = true)
    private Consequence consequence;

    public TaskRule(Integer id, String tag, SeverityLevel severity, List<String> services, Double load) {
        super();
        this.id = id;
        this.condition = new Condition(tag, severity);
        this.consequence = new Consequence(services, load);
    }
    
    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Consequence getConsequence() {
        return consequence;
    }

    public void setConsequence(Consequence consequence) {
        this.consequence = consequence;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static class Condition {
        @ApiModelProperty(value = "Condition: task's tag, e.g., a category", required = true)
        private String tag; 
        @ApiModelProperty(value = "Condition: task's severity", required = true)
        private Task.SeverityLevel severity;
        public Condition(String tag, SeverityLevel severity) {
            super();
            this.tag = tag;
            this.severity = severity;
        }
        public String getTag() {
            return tag;
        }
        public void setTag(String tag) {
            this.tag = tag;
        }
        public Task.SeverityLevel getSeverity() {
            return severity;
        }
        public void setSeverity(Task.SeverityLevel severity) {
            this.severity = severity;
        }
        public boolean match(Condition condition) {
            return condition.getTag().equalsIgnoreCase(tag) && condition.getSeverity().compareTo(severity)==0;
        }
    }
    
    public static class Consequence {
        @ApiModelProperty(value = "Mapped parameter: required requiredServices", required = true)
        private List<String> requiredServices;
        @ApiModelProperty(value = "Mapped parameter: loadFactor factor", required = true)
        private Double loadFactor;
        public Consequence(List<String> services, Double load) {
            super();
            this.requiredServices = services;
            this.loadFactor = load;
        }
        public List<String> getRequiredServices() {
            return requiredServices;
        }
        public void setRequiredServices(List<String> services) {
            this.requiredServices = services;
        }
        public Double getLoadFactor() {
            return loadFactor;
        }
        public void setLoadFactor(Double loadFactor) {
            this.loadFactor = loadFactor;
        }
    }
    
}
