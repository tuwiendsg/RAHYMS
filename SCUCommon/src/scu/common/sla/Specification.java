package scu.common.sla;

import java.util.ArrayList;

import scu.common.model.Properties;

public class Specification {

    protected ArrayList<Objective> slos;
    
    public Specification() {
        this.slos = new ArrayList<Objective>();
    }

    public ArrayList<Objective> getObjectives() {
        return slos;
    }

    public void addObjective(Objective objective) {
        slos.add(objective);
    }
 
    // This is to model only full compliance, i.e., all required properties exist and comply
    // TODO: separate to a Spec Matcher class (using interface)
    public boolean comply(Properties properties) {
        boolean isComply = true;
        for (Objective objective : slos) {
            Object value = properties.getValue(objective.getName());
            if (value==null) {
                isComply = false;
                break;
            } else {
                if (!objective.comply(value)) {
                    isComply = false;
                    break;
                }
            }
        }
        return isComply;
    }
    
}
