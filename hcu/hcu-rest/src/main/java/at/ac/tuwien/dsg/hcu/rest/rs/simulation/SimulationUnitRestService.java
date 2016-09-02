package at.ac.tuwien.dsg.hcu.rest.rs.simulation;

import at.ac.tuwien.dsg.hcu.rest.exceptions.IllegalSimulationArgumentException;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationUnit;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationUnitMongoDBService;
import com.wordnik.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/simulation-unit")
@Api(value = "/simulation-unit", description = "Manage Simulation Unit Properties")
public class SimulationUnitRestService {

    @Inject
    private SimulationUnitMongoDBService simulationUnitMongoDBService;

    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiOperation(value = "Create new Simulation Unit", notes = "create simulation unit properties as string json", response = SimulationUnit.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation unit created successfully"),
            @ApiResponse(code = 503, message = "Server or Mongodb is not available")
    })
    public SimulationUnit createSimulationUnit(@ApiParam(value = "name of unit", required = true) @FormParam("name") final String name,
                                               @ApiParam(value = "properties of unit", required = true) @FormParam("unit") final String unit) {

        SimulationUnit simulationUnit = new SimulationUnit(name, unit);
        return simulationUnitMongoDBService.createSimulationUnit(simulationUnit);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{objectId}")
    @GET
    @ApiOperation(value = "Find unit by objectId", notes = "Find unit by objectId", response = SimulationUnit.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "objectId is null or empty"),
            @ApiResponse(code = 404, message = "unit with such objectId doesn't exist")
    })
    public SimulationUnit getSimulationUnit(@ApiParam(value = "objectId of unit", required = true) @PathParam("objectId") final String objectId) {
        return simulationUnitMongoDBService.getSimulationUnit(objectId);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/default")
    @GET
    @ApiOperation(value = "Find default unit from db", notes = "Find default unit first row of simulation-unit", response = SimulationUnit.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "no unit found")
    })
    public SimulationUnit getDefaultSimulationUnit() {
        return simulationUnitMongoDBService.getDefaultSimulationUnit();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "List all units", notes = "List all simulation units", response = SimulationUnit.class, responseContainer = "List")
    public List<SimulationUnit> getSimulationUnit() {
        return simulationUnitMongoDBService.getSimulationUnit();
    }

    @Produces({MediaType.APPLICATION_JSON})
    @PUT
    @Path("/{objectId}")
    @ApiOperation(value = "Update existing unit", notes = "Update existing simulation unit with given params", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Simulation unit successfully updated"),
            @ApiResponse(code = 400, message = "Simulation unit properties are not valid"),
            @ApiResponse(code = 404, message = "Simulation unit with such id doesn't exists")
    })
    public Response updateSimulationUnit(
            @ApiParam(value = "objectId of unit", required = true) @PathParam("objectId") final String objectId,
            @ApiParam(value = "name of unit", required = true) @FormParam("name") final String name,
            @ApiParam(value = "properties of unit", required = true) @FormParam("unit") final String unit) {

        if (!simulationUnitMongoDBService.updateSimulationUnit(new SimulationUnit(name, unit, objectId))) {
            throw new IllegalSimulationArgumentException();
        }

        return Response.ok().build();
    }

    @Path("/{objectId}")
    @DELETE
    @ApiOperation(value = "Delete existing simulation unit", notes = "Delete existing simulation unit with given objectId", response = Response.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "unit successfully deleted"),
            @ApiResponse(code = 400, message = "ObjectId is null or empty"),
            @ApiResponse(code = 404, message = "unit with such objectId doesn't exists")
    })
    public Response deleteSimulationUnit(@ApiParam(value = "objectId of unit", required = true) @PathParam("objectId") final String objectId) {
        if(!simulationUnitMongoDBService.removeSimulationUnit(objectId)) {
            throw new IllegalSimulationArgumentException();
        }
        return Response.ok().build();
    }

}
