package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SimulationParameter {
    private List<String> units;
    private List<String> tasks;
    private String composerProperties;
    private ConsumerProperties consumerProperties;
    private Simulation simulation;

    public SimulationParameter() {
    }

    @JsonCreator
    public SimulationParameter(@JsonProperty("units") List<String> units,
                               @JsonProperty("tasks") List<String> tasks,
                               @JsonProperty("composerProperties") String composerProperties,
                               @JsonProperty("consumerProperties") ConsumerProperties consumerProperties,
                               @JsonProperty("simulation") Simulation simulation) {

        this.units = units;
        this.tasks = tasks;
        this.composerProperties = composerProperties;
        this.consumerProperties = consumerProperties;
        this.simulation = simulation;
    }

    public String getComposerProperties() {
        return composerProperties;
    }

    public void setComposerProperties(String composerProperties) {
        this.composerProperties = composerProperties;
    }

    public ConsumerProperties getConsumerProperties() {
        return consumerProperties;
    }

    public void setConsumerProperties(ConsumerProperties consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public List<String> getUnits() {
        return units;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public static class ConsumerProperties {
        private int numberOfCycles;
        private int waitBetweenCycles;
        private String tracerConfig;

        public ConsumerProperties() {
        }

        public String getTracerConfig() {
            return tracerConfig;
        }

        public void setTracerConfig(String tracerConfig) {
            this.tracerConfig = tracerConfig;
        }

        public int getNumberOfCycles() {
            return numberOfCycles;
        }

        public void setNumberOfCycles(int numberOfCycles) {
            this.numberOfCycles = numberOfCycles;
        }

        public int getWaitBetweenCycles() {
            return waitBetweenCycles;
        }

        public void setWaitBetweenCycles(int waitBetweenCycles) {
            this.waitBetweenCycles = waitBetweenCycles;
        }
    }

}
