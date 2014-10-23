package at.ac.tuwien.dsg.salam.simulation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import at.ac.tuwien.dsg.salam.cloud.discoverer.Discoverer;
import at.ac.tuwien.dsg.salam.cloud.manager.ServiceManagerOnMemory;
import at.ac.tuwien.dsg.salam.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.salam.cloud.scheduler.Scheduler;
import at.ac.tuwien.dsg.salam.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.salam.composer.Composer;
import at.ac.tuwien.dsg.salam.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.salam.util.ConfigJson;
import at.ac.tuwien.dsg.salam.util.Util;

public class RunSimulation {
    
    private static String configFile = "config/consumer.properties";
    
    public static void main(String[] args) {

        try {
            
            Util.log().info("Initializing tester");
            
            // task config
            ArrayList<ConfigJson> taskConfig = new ArrayList<ConfigJson>();
            String taskGeneratorConfig = Util.getProperty(configFile, "task_generator_config");
            for (String configPath: taskGeneratorConfig.split(",")) {
                taskConfig.add(new ConfigJson(configPath));
            }
            // service config
            ArrayList<ConfigJson> svcConfig = new ArrayList<ConfigJson>();
            String serviceGeneratorConfig = Util.getProperty(configFile, "service_generator_config");
            for (String configPath: serviceGeneratorConfig.split(",")) {
                svcConfig.add(new ConfigJson(configPath));
            }

            // TODO: configure the availability behavior of resources

            // init components
            ServiceManagerInterface manager = new ServiceManagerOnMemory();
            DiscovererInterface discoverer = new Discoverer(manager);
            DependencyProcessorInterface dp = new DependencyProcessor();
            ComposerInterface composer = new Composer(Util.getProperty(configFile, "composer_config"), 
                    manager, discoverer, dp);
            SchedulerInterface scheduler = new Scheduler(composer, dp);

            // start the consumer
            GSConsumer.start(configFile, scheduler, manager, taskConfig, svcConfig);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
