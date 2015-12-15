package at.ac.tuwien.dsg.hcu.rest;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import at.ac.tuwien.dsg.hcu.rest.common.Util;
import at.ac.tuwien.dsg.hcu.rest.config.AppConfig;

public class RunRestServer {

    private static final String PROP_FILE = "/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-rest/config/rest.properties";

    public static void main(final String[] args) throws Exception {
        Resource.setDefaultUseCaches(false);

        int serverPort = Integer.parseInt(Util.getProperty(PROP_FILE, "SERVER_PORT"));
        String serverHost = Util.getProperty(PROP_FILE, "SERVER_HOST");
        String restContextPath = Util.getProperty(PROP_FILE, "REST_CONTEXT_PATH");
        String swaggerContextPath = Util.getProperty(PROP_FILE, "SWAGGER_CONTEXT_PATH");
        String webuiContextPath = Util.getProperty(PROP_FILE, "WEBUI_CONTEXT_PATH");
        String hcuRestConfig = PROP_FILE;

        final Server server = new Server(serverPort);
        System.setProperty(AppConfig.SERVER_PORT, Integer.toString(serverPort));
        System.setProperty(AppConfig.SERVER_HOST, serverHost);
        System.setProperty(AppConfig.CONTEXT_PATH, restContextPath);				
        System.setProperty(AppConfig.WEBUI_CONTEXT_PATH, webuiContextPath);                
        System.setProperty(AppConfig.REST_CONFIG, hcuRestConfig);

        // Configuring Apache CXF servlet and Spring listener  
        final ServletHolder servletHolder = new ServletHolder(new CXFServlet()); 		 		
        final ServletContextHandler context = new ServletContextHandler(); 		
        context.setContextPath("/");
        context.addServlet(servletHolder, "/" + restContextPath + "/*"); 	 		
        context.addEventListener(new ContextLoaderListener()); 		 		
        context.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
        context.setInitParameter("contextConfigLocation", AppConfig.class.getName());

        // Configuring Swagger as static web resource
        final ServletHolder swaggerHolder = new ServletHolder(new DefaultServlet());
        final ServletContextHandler swagger = new ServletContextHandler();
        swagger.setContextPath("/" + swaggerContextPath);
        swagger.addServlet(swaggerHolder, "/*");
        swagger.setResourceBase(new ClassPathResource("/webapp").getURI().toString());

        // Configuring webui as static web resource
        final ServletHolder webuiHolder = new ServletHolder(new DefaultServlet());
        final ServletContextHandler webui = new ServletContextHandler();
        webui.setContextPath("/" + webuiContextPath);
        webui.addServlet(webuiHolder, "/*");
        webui.setResourceBase(new ClassPathResource("/webui").getURI().toString());

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
        handlers.addHandler(swagger);
        handlers.addHandler(webui);

        System.out.println("RAHYMS REST server running at http://" + serverHost + ":" + serverPort + "/" + restContextPath);
        System.out.println("The swagger playground is available at http://" + serverHost + ":" + serverPort + "/" + swaggerContextPath);

        server.setHandler(handlers);
        server.start();
        server.join();

    }
}

