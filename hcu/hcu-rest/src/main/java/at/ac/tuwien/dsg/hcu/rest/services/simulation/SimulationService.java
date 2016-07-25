package at.ac.tuwien.dsg.hcu.rest.services.simulation;

import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import at.ac.tuwien.dsg.hcu.simulation.util.SimulationGraphDrawer;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationGraph;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationGraphData;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.Simulation;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationParameter;
import at.ac.tuwien.dsg.hcu.simulation.MainWebSimulation;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimulationService {

    public boolean startSimulation(SimulationParameter simulationParameter) {

        simulationParameter.getSimulation().setTimeCreated(new Date().toString());
        ObjectId simulationId = new ObjectId();
        simulationParameter.getSimulation().setId(simulationId.toString());

        MongoDatabase.putSimulationObject(
                simulationId,
                simulationParameter.getSimulation().getTimeCreated(),
                simulationParameter.getSimulation().getSimulationName(),
                simulationParameter.getSimulation().getSimulationDescription(),
                null
                );

        //todo brk bazen simulation u ayni elemanlarla pespese yapinca veri eklenmiyor. simulation icin yeni instance lazimmis muhammad dedi
        new MainWebSimulation().runSimulation(
                simulationParameter.getUnits(),
                simulationParameter.getTasks(),
                simulationParameter.getComposerProperties(),
                simulationParameter.getConsumerProperties().getNumberOfCycles(),
                simulationParameter.getConsumerProperties().getWaitBetweenCycles(),
                simulationParameter.getConsumerProperties().getTracerConfig(),
                simulationId
        );

        return true;
    }

    public SimulationGraph showGraph(SimulationGraphData simulationGraphData) {
        SimulationGraphDrawer simulationGraphDrawer = SimulationGraphDrawer.startDrawingGraph(
                simulationGraphData.getxAxis(), simulationGraphData.getyAxis(), simulationGraphData.getSimulationId()
        );

        BufferedImage image = simulationGraphDrawer.getImage();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStream b64 = new Base64OutputStream(os);

        try {
            ImageIO.write(image, "png", b64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = "";

        try {
            result = os.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ObjectId graphId = new ObjectId();

        return new SimulationGraph(graphId.toString(), result);
    }

    public List<Simulation> getSimulation() {
        List<Simulation> simulations = new ArrayList<>();

        DBCursor cursor = MongoDatabase.getSimulationCollection().find();
        DBObject returnedSimulation;

        while (cursor.hasNext()) {
            Simulation simulation = new Simulation();

            returnedSimulation = cursor.next();
            simulation.setId((returnedSimulation.get("_id")).toString());
            simulation.setSimulationName((String) returnedSimulation.get("name"));
            simulation.setSimulationDescription((String) returnedSimulation.get("description"));
            simulation.setTimeCreated((String) returnedSimulation.get("timeCreated"));
            simulation.setTimeFinished((String) returnedSimulation.get("timeFinished"));
            simulation.setFilePath((String) returnedSimulation.get("filePath"));
            simulations.add(simulation);
        }

        return simulations;
    }

    public void refreshToDefault() {
        try {
            MongoDatabase.toBringDefaultSimulationUnitsAndTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
