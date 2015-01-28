package at.ac.tuwien.dsg.hcu.monitor.action;

import at.ac.tuwien.dsg.hcu.common.model.SCU;

public class Action {

    public static void handleUtilizationOverload(SCU collective, Object util) {
        System.out.println("Overloaded utilization=" + util +  " on collective " + collective);
    }
    
}
