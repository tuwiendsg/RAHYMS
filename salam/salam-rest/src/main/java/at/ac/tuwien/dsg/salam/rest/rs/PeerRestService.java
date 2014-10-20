package at.ac.tuwien.dsg.salam.rest.rs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import at.ac.tuwien.dsg.salam.rest.resource.Peer;
import at.ac.tuwien.dsg.salam.rest.services.PeerService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/peer") 
@Api(value = "/peer", description = "Manage peer")
public class PeerRestService {
    @Inject private PeerService peerService;

    private static int PAGE_SIZE = 100;

    @Produces({ MediaType.APPLICATION_JSON })
    @GET
    @ApiOperation(value = "List all peers", notes = "List all peers using paging", response = Peer.class, responseContainer = "List")
    public List<Peer> getPeer( @ApiParam(value = "Page to fetch", required = true) @QueryParam("page") @DefaultValue("1") final int page) {
        return peerService.getPeers(page, PAGE_SIZE);
    }

    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/{email}")
    @GET
    @ApiOperation(value = "Find peer by e-mail", notes = "Find peer by e-mail", response = Peer.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Peer with such e-mail doesn't exists")			 
    })
    public Peer getPeer(@ApiParam(value = "E-Mail address to lookup for", required = true) @PathParam("email") final String email) {
        return peerService.getPeerByEmail(email);
    }

    @Produces({ MediaType.APPLICATION_JSON  })
    @POST
    @ApiOperation(value = "Create new peer", notes = "Create new peer")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Peer created successfully"),
        @ApiResponse(code = 409, message = "Peer with such e-mail already exists")
    })
    public Response addPeer(@Context final UriInfo uriInfo,
            @ApiParam(value = "Name", required = true) @FormParam("name") final String name,
            @ApiParam(value = "E-Mail", required = true) @FormParam("email") final String email, 
            @ApiParam(value = "REST URL", required = true) @FormParam("rest") final String rest, 
            @ApiParam(value = "Provided services", required = true) @FormParam("services_provided") final List<String> services
            ) {

        List<String> _services = Arrays.asList(services.get(0).split(","));
        peerService.addPeer(name, email, rest, _services);
        return Response.created(uriInfo.getRequestUriBuilder().path(email).build()).build();
    }

    @Produces({ MediaType.APPLICATION_JSON  })
    @Path("/{email}")
    @PUT
    @ApiOperation(value = "Update existing peer", notes = "Update existing peer", response = Peer.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Peer with such e-mail doesn't exists")			 
    })
    public Peer updatePeer(			
            @ApiParam(value = "Name", required = true) @FormParam("name") final String name,
            @ApiParam(value = "E-Mail", required = true) @FormParam("email") final String email, 
            @ApiParam(value = "REST URL", required = true) @FormParam("rest") final String rest, 
            @ApiParam(value = "Provided services", required = true) @FormParam("services_provided") final List<String> services
            ) {

        final Peer peer = peerService.getPeerByEmail(email);

        if(name != null) {
            peer.setName(name);
        }

        if(services != null) {
            List<String> _services = Arrays.asList(services.get(0).split(","));
            peer.setServices(_services);
        }

        return peer; 				
    }

    @Path("/{email}")
    @DELETE
    @ApiOperation(value = "Delete existing peer", notes = "Delete existing peer", response = Peer.class)
    @ApiResponses({
        @ApiResponse(code = 404, message = "Peer with such e-mail doesn't exists")			 
    })
    public Response deletePeer(@ApiParam(value = "E-Mail", required = true) @PathParam("email") final String email) {
        peerService.removePeer(email);
        return Response.ok().build();
    }

}
