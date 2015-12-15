package at.ac.tuwien.dsg.hcu.rest.services;

import at.ac.tuwien.dsg.hcu.rest.exceptions.CollectiveNotAvailable;
import at.ac.tuwien.dsg.hcu.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.hcu.rest.resource.Simulation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by karaoglan on 07/10/15.
 */
public class SimulationService {

    public Simulation saveJson(String jsonContent) {

        final Simulation simulation = new Simulation(0, jsonContent);

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(jsonContent);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter("/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-simulation/config/burak.json");
            writer.write(json.toJSONString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return simulation;
    }

    public void response() {
        //throw new CollectiveNotAvailable();
    }
}
