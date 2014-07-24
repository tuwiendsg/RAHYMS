package scu.run;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;

import scu.adapter.gridsim.GSConsumer;
import scu.cloud.discoverer.Discoverer;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.cloud.scheduler.DependencyProcessor;
import scu.cloud.scheduler.Scheduler;
import scu.common.interfaces.ComposerInterface;
import scu.common.interfaces.DependencyProcessorInterface;
import scu.common.interfaces.DiscovererInterface;
import scu.common.interfaces.SchedulerInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.composer.Composer;
import scu.util.ConfigJson;
import scu.util.Util;


public class RunSimulation {
    
    private static String configFile = "config/consumer.properties";
    
    public static void main(String[] args) {

        try {
            
            Util.log().info("Initializing tester");
            
            // config
            ConfigJson taskConfig = new ConfigJson(Util.getProperty(configFile, "task_generator_config"));
            ConfigJson svcConfig = new ConfigJson(Util.getProperty(configFile, "service_generator_config"));

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
