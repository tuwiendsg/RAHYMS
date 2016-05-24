package at.ac.tuwien.dsg.hcu.rest.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import at.ac.tuwien.dsg.hcu.rest.rs.*;
import at.ac.tuwien.dsg.hcu.rest.rs.simulation.SimulationRestService;
import at.ac.tuwien.dsg.hcu.rest.rs.simulation.SimulationTaskRestService;
import at.ac.tuwien.dsg.hcu.rest.rs.simulation.SimulationUnitRestService;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationService;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationTaskMongoDBService;
import at.ac.tuwien.dsg.hcu.rest.services.simulation.SimulationUnitMongoDBService;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.hcu.rest.resource.Peer;
import at.ac.tuwien.dsg.hcu.rest.services.PeerService;
import at.ac.tuwien.dsg.hcu.rest.services.TaskRuleService;
import at.ac.tuwien.dsg.hcu.rest.services.TaskService;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

@Configuration
public class AppConfig {
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_HOST = "server.host";
    public static final String CONTEXT_PATH = "context.path";	
    public static final String WEBUI_CONTEXT_PATH = "webui.context.path";

    public static final String REST_CONFIG = "rest.config";

    @Bean( destroyMethod = "shutdown" )
    public SpringBus cxf() {
        return new SpringBus();
    }

    @Bean @DependsOn( "cxf" )
    public Server jaxRsServer() {
        JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint( jaxRsApiApplication(), JAXRSServerFactoryBean.class );
        factory.setServiceBeans( Arrays.<Object>asList( 
                peerRestService(),
                taskRestService(), 
                taskRuleRestService(),
                simulationRestService(),
                simulationTaskRestService(),
                simulationUnitRestService(),
                collectiveRestService(),
                apiListingResourceJson()
                ) );
        factory.setAddress( factory.getAddress() );
        factory.setProviders( Arrays.< Object >asList( jsonProvider(), resourceListingProvider(), apiDeclarationProvider() ) );
        return factory.create();
    }

    @Bean @Autowired
    public BeanConfig swaggerConfig( Environment environment ) {
        final BeanConfig config = new BeanConfig();

        config.setVersion( "1.0.0" );
        config.setScan( true );
        config.setResourcePackage( Peer.class.getPackage().getName() );
        config.setBasePath( 
                String.format( "http://%s:%s/%s%s",
                        environment.getProperty( SERVER_HOST ),
                        environment.getProperty( SERVER_PORT ),
                        environment.getProperty( CONTEXT_PATH ),
                        jaxRsServer().getEndpoint().getEndpointInfo().getAddress() 
                        ) 
                );

        return config;
    }

    @Bean
    public ApiDeclarationProvider apiDeclarationProvider() {
        return new ApiDeclarationProvider();
    }

    @Bean
    public ApiListingResourceJSON apiListingResourceJson() {
        return new ApiListingResourceJSON();
    }

    @Bean
    public ResourceListingProvider resourceListingProvider() {
        return new ResourceListingProvider();
    }

    @Bean 
    public JaxRsApiApplication jaxRsApiApplication() {
        return new JaxRsApiApplication();
    }

    @Bean 
    public TaskRestService taskRestService() {
        return new TaskRestService();
    }

    @Bean 
    public TaskService taskService() {	    
        return new TaskService();
    }

    @Bean
    public SimulationService simulationService() {
        return new SimulationService();
    }

    @Bean
    public SimulationTaskMongoDBService simulationTaskService() {
        return new SimulationTaskMongoDBService();
    }

    @Bean
    public SimulationUnitMongoDBService simulationUnitService() {
        return new SimulationUnitMongoDBService();
    }

    @Bean 
    public PeerRestService peerRestService() {
        return new PeerRestService();
    }

    @Bean 
    public PeerService peerService() {
        return new PeerService();
    }

    @Bean 
    public CollectiveRestService collectiveRestService() {
        return new CollectiveRestService();
    }

    @Bean
    public SimulationRestService simulationRestService() {
        return new SimulationRestService();
    }

    @Bean
    public SimulationTaskRestService simulationTaskRestService() {
        return new SimulationTaskRestService();
    }

    @Bean
    public SimulationUnitRestService simulationUnitRestService() {
        return new SimulationUnitRestService();
    }

    @Bean 
    public TaskRuleRestService taskRuleRestService() {
        return new TaskRuleRestService();
    }

    @Bean 
    public TaskRuleService taskRuleService() {      
        return new TaskRuleService();
    }

    /*
    @Bean 
    public CollectiveRestService collectiveRestService() {
        return new CollectiveRestService();
    }
    */

    @Bean
    public JacksonJsonProvider jsonProvider() {
        return new JacksonJsonProvider();
    }
}
