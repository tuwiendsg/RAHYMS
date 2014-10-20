package at.ac.tuwien.dsg.smartsociety.demo.peer;

import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.adapters.rest.ObjectMapperProvider;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerApplicationRestServer {

    private final URI serverURI;
    private final PeerApplicationFrame frame;
    private HttpServer server;

    public PeerApplicationRestServer(int port, String serverURIPostfix, PeerApplicationFrame frame) {
        this.frame = frame;
        serverURI = URI.create("http://128.131.200.89:" + port + "/" + serverURIPostfix);
    }

    public void start() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        }
    }

    public void stop() {
        server.shutdown();
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(RestResource.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(PeerApplicationRestServer.this).to(PeerApplicationRestServer.class);
                }
            });
        }
    }

    private void forwardMessage(Message message) {
        frame.onMessage(message);
    }

    @Path("/")
    @Singleton
    @Produces(MediaType.APPLICATION_JSON)
    public static class RestResource {

        PeerApplicationRestServer server;

        @Inject
        public RestResource(PeerApplicationRestServer server) {
            this.server = server;
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response message(JsonMessageDTO message, @Context UriInfo uri) {
            if (message == null) {
                throw new WebApplicationException();
            }

            publishMessage(message.createMessage());

            return Response.status(Response.Status.CREATED).build();
        }

        @GET
        @Path("test")
        @Produces(MediaType.TEXT_PLAIN)
        public String test() {
            return "ok";
        }

        private void publishMessage(Message message) {
            server.forwardMessage(message);
        }
    }
}
