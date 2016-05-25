package at.ac.tuwien.dsg.hcu.common.model;

import java.util.Hashtable;

public class OptimizationObjective { 

    private Hashtable<String, Double> weights;

    public OptimizationObjective() {
        weights = new Hashtable<String, Double>();
        // set default weights
        setWeight("skill", 1.0);
        setWeight("connectedness", 1.0);
        setWeight("cost", 1.0);
        setWeight("time", 1.0);
    }

    public double getWeight(String name) {
        Double weight = weights.get(name);
        if (weight==null) weight = new Double(0.0);
        return weight.doubleValue();
    }
    
    public OptimizationObjective setWeight(String name, double weight) {
        weights.put(name, new Double(weight));
        return this;
    }

    @Override
    public String toString() {
        return weights.toString();
    }

    

}
