package at.ac.tuwien.dsg.hcu.rest.services.simulation;

import at.ac.tuwien.dsg.hcu.composer.helper.MongoDatabase;
import at.ac.tuwien.dsg.hcu.rest.common.SimulationGraph;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.Graph;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.GraphData;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.Simulation;
import at.ac.tuwien.dsg.hcu.rest.rs.simulation.SimulationRestService;
import at.ac.tuwien.dsg.hcu.simulation.MainSimulation;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karaoglan on 07/10/15.
 */
public class SimulationService {

    public boolean startSimulation(SimulationRestService.AX jsonData) {

        //todo brk bazen simulation u ayni elemanlarla pespese yapinca veri eklenmiyor. simulation icin yeni instance lazimmis ask
        new MainSimulation().runSimulation(jsonData.getUnits(), jsonData.getTasks(), jsonData.getComposerProperties(),
                jsonData.getConsumerProperties().getNumberOfCycles(), jsonData.getConsumerProperties().getWaitBetweenCycles());

        return true;
    }

    public Graph showGraph(GraphData graphData) {
        SimulationGraph graph = SimulationGraph.startDrawingGraph(graphData.getxAxis(), graphData.getyAxis(), graphData.getSimulationDate());
        BufferedImage image = graph.getImage();

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

        //todo bu id lere cözüm bul
        return new Graph(0, result);
    }

    public List<Simulation> getSimulation() {
        List<Simulation> simulations = new ArrayList<>();

        DBCursor cursor = MongoDatabase.getSimulationCollection().find();
        DBObject returnedSimulation;

        while (cursor.hasNext()) {
            Simulation simulation = new Simulation();

            returnedSimulation = cursor.next();
            simulation.setId((returnedSimulation.get("_id")).toString());

            simulation.setTimeCreated((String) returnedSimulation.get("date"));

            simulation.setFilePath((String) returnedSimulation.get("file_path"));
            simulations.add(simulation);
        }

        return simulations;
    }

    public boolean copyFileToTemp(String path, String tempPath) {

        try {
            Files.copy(Paths.get(path), Paths.get(tempPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
