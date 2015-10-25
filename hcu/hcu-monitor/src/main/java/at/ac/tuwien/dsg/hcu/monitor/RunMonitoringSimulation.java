package at.ac.tuwien.dsg.hcu.monitor;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RunMonitoringSimulation {

    private static String DEFAULT_SCENARIO = "scenarios/test.json";

    public static void main(String[] args) {
        
        String scenario = DEFAULT_SCENARIO;
        if (args.length>0) {
            scenario = args[0];
        }

        try {
            
            // parse json scenario
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> scenarioConfig = mapper.readValue(new File(scenario), Map.class);
            
            // run simulation
            Simulation simulation = new Simulation();
            boolean initResult = simulation.init(scenarioConfig);
            if (initResult) {
                simulation.start();
            }
            
            System.out.println("EXITING MAIN");
            
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
