package at.ac.tuwien.dsg.hcu.rest.rs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import at.ac.tuwien.dsg.hcu.rest.resource.Assignment;
import at.ac.tuwien.dsg.hcu.rest.resource.Collective;
import at.ac.tuwien.dsg.hcu.rest.resource.Task;
import at.ac.tuwien.dsg.hcu.rest.resource.Assignment.Status;
import at.ac.tuwien.dsg.hcu.rest.services.PeerService;
import at.ac.tuwien.dsg.hcu.rest.services.TaskService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/collective") 
@Api(value = "/collective", description = "Manage collective")
public class CollectiveRestService {
    @Inject private PeerService peerService;
    @Inject private TaskService taskService;

    private static int PAGE_SIZE = 100;

    @Produces({ MediaType.APPLICATION_JSON })
    @GET
    @ApiOperation(value = "List all collectives", notes = "List all collectives using paging", response = Collective.class, responseContainer = "List")
    public List<Collective> getCollective( @ApiParam(value = "Page to fetch", required = true) @QueryParam("page") @DefaultValue("1") final int page) {
        return peerService.getCollectives(page, PAGE_SIZE);
    }

    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/{collectiveId}")
    @GET
    @ApiOperation(value = "Find collective by id", notes = "Find collective by id", response = Collective.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Collective with such id doesn't exists")           
    })
    public Collective getCollective(@ApiParam(value = "Id to lookup for", required = true) @PathParam("collectiveId") final Integer id) {
        return peerService.getCollectiveById(id);
    }
    
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/{collectiveId}/assignment/{peerId}")
    @GET
    @ApiOperation(value = "Find assignment", notes = "Find assignment", response = Assignment.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Assignment with such ids doesn't exists")           
    })
    public Assignment getAssignment(
            @ApiParam(value = "Collective Id to lookup for", required = true) @PathParam("collectiveId") final Integer collectiveId,
            @ApiParam(value = "Peer Id to lookup for", required = true) @PathParam("peerId") final Integer peerId
            ) {
        Collective collective = peerService.getCollectiveById(collectiveId);
        Assignment assignment = null;
        for (Assignment a: collective.getAssignments()) {
            if (a.getPeer().getElementId()==(long)peerId) {
                assignment = a;
                break;
            }
        }
        return assignment;
    }

    @Produces({ MediaType.APPLICATION_JSON  })
    @Path("/{collectiveId}/status/{peerId}")
    @PUT
    @ApiOperation(value = "Update existing assignment", notes = "Update existing assignment", response = Collective.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Collective with such e-mail doesn't exists")           
    })
    public Collective updateCollectiveStatus(           
            @ApiParam(value = "Collective Id", required = true) @PathParam("collectiveId") final Integer id,
            @ApiParam(value = "Peer Id", required = true) @PathParam("peerId") final Integer peerId, 
            @ApiParam(value = "Status", required = true) @FormParam("status") final Assignment.Status status
            ) {

        Collective collective = peerService.getCollectiveById(id);
        for (Assignment assignment: collective.getAssignments()) {
            if (assignment.getPeer().getElementId()==(long)peerId) {
                assignment.setStatus(status);
                if (status==Status.DELEGATED) {
                    taskService.handleDelegation(collective, assignment);
                }
                break;
            }
        }
        return collective;                
    }
    
}
