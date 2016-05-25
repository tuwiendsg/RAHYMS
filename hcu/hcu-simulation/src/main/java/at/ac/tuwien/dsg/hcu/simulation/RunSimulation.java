package at.ac.tuwien.dsg.hcu.simulation;

public class RunSimulation {
    
    private static String DEFAULT_CONFIG = "config/consumer.properties";
    //private static String DEFAULT_LOG4J_CONFIG = "config/log4j2.xml";
    
    public static void main(String[] args) {

        //System.setProperty("log4j.configurationFile", DEFAULT_LOG4J_CONFIG);
        
        Simulation simulation = new Simulation();
        boolean initResult = simulation.init(DEFAULT_CONFIG);
        if (initResult) {
            simulation.start();
        }

    }

}
