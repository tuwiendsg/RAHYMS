package at.ac.tuwien.dsg.hcu.rest.rs;

import at.ac.tuwien.dsg.hcu.rest.common.SimulationGraph;
import at.ac.tuwien.dsg.hcu.rest.resource.Simulation;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationData;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.Unit;
import at.ac.tuwien.dsg.hcu.rest.services.SimulationService;
import at.ac.tuwien.dsg.hcu.simulation.RunSimulation;
import com.wordnik.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by karaoglan on 07/10/15.
 */
@Path("/simulation")
@Api(value = "/simulation", description = "Manage Simulation Properties")
public class SimulationRestService {
    @Inject private SimulationService simulationService;

    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiOperation(value = "Start a simulation", notes = "start a simulation with json params", response = Simulation.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation started successfully"),
            @ApiResponse(code = 404, message = "Simulation not found"),
            @ApiResponse(code = 503, message = "Simulation server not available")
    })
    public Simulation startSimulation(@ApiParam( value = "Json string to saved", required = true) @RequestBody final SimulationData simulationData ) {

        try {
            new RunSimulation().main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        simulationService.response();
        SimulationGraph.main(new String[]{simulationData.getxAxis(), simulationData.getyAxis()});
        return null;
        //return simulationService.saveJson("");
    }

}
