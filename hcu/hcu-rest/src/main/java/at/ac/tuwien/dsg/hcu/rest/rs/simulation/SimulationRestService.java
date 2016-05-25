package at.ac.tuwien.dsg.hcu.rest.rs.simulation;

import at.ac.tuwien.dsg.hcu.rest.resource.simulation.*;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationService;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationTaskMongoDBService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by karaoglan on 07/10/15.
 */
@Path("/simulation")
@Api(value = "/simulation", description = "Manage Simulation Properties")
public class SimulationRestService {
    @Inject private SimulationService simulationService;

    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiOperation(value = "Start a simulation", notes = "start a simulation with json params units and tasks", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation started successfully"),
            @ApiResponse(code = 404, message = "Simulation not found"),
            @ApiResponse(code = 503, message = "Simulation server not available")
    })//todo brk bisey döndermek zorunda degil response yollanabilir.AX ismini degistir
    public Response startSimulation(@ApiParam( value = "Json string to todo", required = true) @RequestBody final SimulationStartParam jsonData) {
        //todo brk hata olayini dogru ver soru hatta nasil hatalar web e verilmeli düzen nasil olmali
        return simulationService.startSimulation(jsonData) ? Response.ok().build() : Response.serverError().build();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/graph")
    @POST
    @ApiOperation(value = "Show the simulation graph", notes = "show the simulation graph with json params", response = Graph.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Graph generated successfully"),
            @ApiResponse(code = 404, message = "graph not found"),
            @ApiResponse(code = 503, message = "Graph server not available")
    })
    public Graph showGraph(@ApiParam( value = "Json string to saved", required = true) @RequestBody final GraphData graphData ) {

        return simulationService.showGraph(graphData);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/graph/copy")
    @POST
    @ApiOperation(value = "copy csv file into temp folder", notes = "copy file", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 503, message = "could not copy file")
    })
    public Response copyFileWithPathToTemp(@ApiParam( value = "path of the file to be copied", required = true) @FormParam("path") final String path,
                                           @ApiParam( value = "temp path of the file into copied", required = true) @FormParam("tempPath") final String tempPath                                     ) {

        return simulationService.copyFileToTemp(path, tempPath) ? Response.ok().build() : Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "List all simulations", notes = "List all simulations", response = Simulation.class, responseContainer = "List")
    public List<Simulation> getSimulation() {
        return simulationService.getSimulation();
    }

}
