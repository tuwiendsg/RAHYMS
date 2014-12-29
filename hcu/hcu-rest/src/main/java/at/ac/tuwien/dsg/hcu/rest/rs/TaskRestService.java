package at.ac.tuwien.dsg.hcu.rest.rs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import at.ac.tuwien.dsg.hcu.rest.resource.Task;
import at.ac.tuwien.dsg.hcu.rest.services.TaskService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/task") 
@Api(value = "/task", description = "Manage task")
public class TaskRestService {
    @Inject private TaskService taskService;

    private static int PAGE_SIZE = 100;

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "List all tasks", notes = "List all tasks using paging", response = Task.class, responseContainer = "List")
    public List<Task> getTask(@ApiParam( value = "Page to fetch", required = true) @QueryParam("page") @DefaultValue("1") final int page ) {
        return taskService.getTasks(page, PAGE_SIZE);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @GET
    @ApiOperation(value = "Find task by id", notes = "Find task by id", response = Task.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Task with such id doesn't exists")			 
    })
    public Task getTask(@ApiParam(value = "Id to lookup for", required = true) @PathParam("id") final Integer id) {
        return taskService.getById(id);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiOperation(value = "Create new task", notes = "Create new task", response = Task.class)
    @ApiResponses({
        @ApiResponse(code = 201, message = "Task created successfully"),
        @ApiResponse(code = 404, message = "Task generator rule not found"),
        @ApiResponse(code = 503, message = "Unable to assemble a collective to serve the task")
    })
    public Task addTask( @Context final UriInfo uriInfo,
            @ApiParam(value = "Task's name", required = true) @FormParam("name") final String name, 
            @ApiParam(value = "Task's content", required = true) @FormParam("content") final String content, 
            @ApiParam(value = "Task's tag, e.g., a category", required = true ) @FormParam("tag") final String tag, 
            @ApiParam(value = "Task's severity", required = true ) @FormParam("severity") final Task.SeverityLevel severity 
            ) {

        Task task = taskService.createTask(name, content, tag, severity);
        return task;
    }

}
