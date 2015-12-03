package at.ac.tuwien.dsg.hcu.monitor.test;

import at.ac.tuwien.dsg.hcu.monitor.gridsim.GSMonitoringSimulation;

public class TestCSVMonitorSim {
    
    boolean eof = false;

    public static void main(String[] args) {
        
        try {
            GSMonitoringSimulation.startSimulation(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    

}
