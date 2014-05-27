package scu.common.model;

import java.util.ArrayList;

public class SCU {

    ArrayList<Assignment> assignments;
    Metrics metrics;
    Properties properties;
    
    public SCU() {
        assignments = new ArrayList<Assignment>();
        metrics = new Metrics();
        properties = new Properties();
    }

    public ArrayList<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(ArrayList<Assignment> assignments) {
        this.assignments = assignments;
    }
    
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }    

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    
    
}
