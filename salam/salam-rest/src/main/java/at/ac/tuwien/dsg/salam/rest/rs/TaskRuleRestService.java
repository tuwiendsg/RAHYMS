package at.ac.tuwien.dsg.salam.rest.rs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
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

import at.ac.tuwien.dsg.salam.rest.resource.Task.SeverityLevel;
import at.ac.tuwien.dsg.salam.rest.resource.TaskRule;
import at.ac.tuwien.dsg.salam.rest.services.TaskRuleService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/task_rule") 
@Api(value = "/task_rule", description = "Manage task generator rules")
public class TaskRuleRestService {
    @Inject private TaskRuleService taskRuleService;
    
    private static int PAGE_SIZE = 100;

    @Produces({ MediaType.APPLICATION_JSON })
    @GET
    @ApiOperation(value = "List all task generator rules", notes = "List all task generator rules using paging", response = TaskRule.class, responseContainer = "List")
    public List<TaskRule> getTaskRule( @ApiParam(value = "Page to fetch", required = true) @QueryParam("page") @DefaultValue("1") final int page) {
        return taskRuleService.getTaskRules(page, PAGE_SIZE);
    }

    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/{id}")
    @GET
    @ApiOperation(value = "Find rule by id", notes = "Find rule by id", response = TaskRule.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Task generator rule with such id doesn't exists")			 
    })
    public TaskRule getRule(@ApiParam(value = "Id to lookup for", required = true) @PathParam("id") final Integer id) {
        return taskRuleService.getTaskRuleById(id);
    }

    @Produces({ MediaType.APPLICATION_JSON  })
    @POST
    @ApiOperation(value = "Create new task generator rule", notes = "Create new task generator rule")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Task generator rule created successfully")
    })
    public Response addTaskRule(
            @Context final UriInfo uriInfo,
            @ApiParam(value = "Condition: tag", required = true) @FormParam("tag") final String tag,
            @ApiParam(value = "Condition: severity", required = true) @FormParam("severity") final SeverityLevel severity,
            @ApiParam(value = "Consequence: services required", required = true) @FormParam("required_services") final List<String> required_services,
            @ApiParam(value = "Consequence: load factor", required = true) @FormParam("load_factor") final Double load_factor
            ) {
        List<String> _services = Arrays.asList(required_services.get(0).split(","));
        Integer id = taskRuleService.addTaskRule(tag, severity, _services, load_factor);
        return Response.created(uriInfo.getRequestUriBuilder().path(id.toString()).build()).build();
    }

    @Path("/{id}")
    @DELETE
    @ApiOperation(value = "Delete existing task generator rule", notes = "Delete existing task generator", response = TaskRule.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Task generator rule with such id doesn't exists")			 
    })
    public Response deleteTaskRule(@ApiParam(value = "Id", required = true) @PathParam("id") final Integer id) {
        taskRuleService.removeTaskRule(id);
        return Response.ok().build();
    }

}
