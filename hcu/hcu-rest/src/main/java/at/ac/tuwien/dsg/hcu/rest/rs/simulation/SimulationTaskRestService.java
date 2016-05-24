package at.ac.tuwien.dsg.hcu.rest.rs.simulation;

import at.ac.tuwien.dsg.hcu.rest.exceptions.IllegalSimulationArgumentException;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationTask;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationTaskMongoDBService;
import com.wordnik.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by karaoglan on 10/04/16.
 */
@Path("/simulation-task")
@Api(value = "/simulation-task", description = "Manage Simulation Task Properties")
public class SimulationTaskRestService {

    @Inject
    private SimulationTaskMongoDBService simulationTaskMongoDBService;

    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiOperation(value = "Create new Simulation Task", notes = "create simulation task properties as string json", response = SimulationTask.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation task created successfully"),
            @ApiResponse(code = 503, message = "Server or Mongodb is not available")
    })
    public SimulationTask createSimulationTask(@ApiParam(value = "name of task", required = true) @FormParam("name") final String name,
                                               @ApiParam(value = "properties of task", required = true) @FormParam("task") final String task) {

        SimulationTask simulationTask = new SimulationTask(name, task);
        return simulationTaskMongoDBService.createSimulationTask(simulationTask);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{objectId}")
    @GET
    @ApiOperation(value = "Find Task by objectId", notes = "Find Task by objectId", response = SimulationTask.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "objectId is null or empty"),
            @ApiResponse(code = 404, message = "Task with such objectId doesn't exist")
    })
    public SimulationTask getSimulationTask(@ApiParam(value = "objectId of task", required = true) @PathParam("objectId") final String objectId) {
        return simulationTaskMongoDBService.getSimulationTask(objectId);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/default")
    @GET
    @ApiOperation(value = "Find default simulation task", notes = "Find default first row of simulation-task", response = SimulationTask.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "no task found")
    })
    public SimulationTask getDefaultSimulationTask() {
        return simulationTaskMongoDBService.getDefaultSimulationTask();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "List all Tasks", notes = "List all simulation Tasks", response = SimulationTask.class, responseContainer = "List")
    public List<SimulationTask> getSimulationTask() {
        return simulationTaskMongoDBService.getSimulationTask();
    }

    @Path("/{objectId}")
    @DELETE
    @ApiOperation(value = "Delete existing simulation task", notes = "Delete existing simulation task with given objectId", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Task successfully deleted"),
            @ApiResponse(code = 400, message = "ObjectId is null or empty"),
            @ApiResponse(code = 404, message = "Task with such objectId doesn't exists")
    })
    public Response deleteSimulationTask(@ApiParam(value = "objectId of Task", required = true) @PathParam("objectId") final String objectId) {
        if(!simulationTaskMongoDBService.removeSimulationTask(objectId)) {
            throw new IllegalSimulationArgumentException();
        }
        return Response.ok().build();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @PUT
    @ApiOperation(value = "Update existing task", notes = "Update existing simulation task with given params", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation task successfully updated"),
            @ApiResponse(code = 400, message = "Simulation task properties are not valid"),
            @ApiResponse(code = 404, message = "Simulation task with such id doesn't exists")
    })
    public Response updateSimulationTask(
            @ApiParam(value = "name of task", required = true) @FormParam("name") final String name,
            @ApiParam(value = "objectId of task", required = true) @FormParam("objectId") final String objectId,
            @ApiParam(value = "properties of task", required = true) @FormParam("task") final String task) {


        if (!simulationTaskMongoDBService.updateSimulationTask(new SimulationTask(name, task, objectId))) {
            throw new IllegalSimulationArgumentException();
        }

        return Response.ok().build();
    }
}
