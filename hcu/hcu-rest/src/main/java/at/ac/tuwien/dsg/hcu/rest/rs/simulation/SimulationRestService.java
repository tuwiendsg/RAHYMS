package at.ac.tuwien.dsg.hcu.rest.rs.simulation;

import at.ac.tuwien.dsg.hcu.rest.resource.simulation.*;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationService;
import com.wordnik.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

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
    })
    public Response startSimulation(@ApiParam( value = "Json string to todo", required = true) @RequestBody final SimulationParameter simulationParamater) {
        //todo brk hata olayini dogru ver soru hatta nasil hatalar web e verilmeli düzen nasil olmali
        return simulationService.startSimulation(simulationParamater) ? Response.ok().build() : Response.serverError().build();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/graph")
    @POST
    @ApiOperation(value = "Show the simulation graph", notes = "show the simulation graph with json params", response = SimulationGraph.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "SimulationGraph generated successfully"),
            @ApiResponse(code = 404, message = "graph not found"),
            @ApiResponse(code = 503, message = "SimulationGraph server not available")
    })
    public SimulationGraph showGraph(@ApiParam( value = "Json string to saved", required = true) @RequestBody final SimulationGraphData simulationGraphData) {

        return simulationService.showGraph(simulationGraphData);
    }

    @GET
    @Path("/file")
    @Produces("text/csv")
    @ApiOperation(value = "Show the simulation graph", notes = "show the simulation graph with json params", response = SimulationGraph.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "SimulationGraph generated successfully"),
            @ApiResponse(code = 404, message = "graph not found"), //todo brk degistir commentleri
            @ApiResponse(code = 503, message = "SimulationGraph server not available")
    })
    public Response getCSVFile(@ApiParam(value = "the path of csv file for simulation", required = true) @QueryParam("filePath") final String filePath) {
        File file = new File(filePath);

        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"test_file.csv\"");
        return response.build();
    }

    @GET
    @Path("/as-default")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Refresh the simulation units and tasks as default", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation units and tasks refreshed successfully"),
            @ApiResponse(code = 404, message = "object not found")
    })
    public Response refreshDBToDefault() {
        //todo brk lazim mi incele
        simulationService.refreshToDefault();
        return Response.ok().build();
    }


    @Produces({MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "List all simulations", notes = "List all simulations", response = Simulation.class, responseContainer = "List")
    public List<Simulation> getSimulation() {
        return simulationService.getSimulation();
    }

}
