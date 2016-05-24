package at.ac.tuwien.dsg.hcu.simulation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import at.ac.tuwien.dsg.hcu.cloud.discoverer.Discoverer;
import at.ac.tuwien.dsg.hcu.cloud.manager.ServiceManagerOnMemory;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.Scheduler;
import at.ac.tuwien.dsg.hcu.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.Util;

//todo brk mainsimulation kullaniliyor bunun yerine sil.
public class RunSimulation {
    
    private static String configFile = "/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-simulation/config/consumer.properties";
    private static final String MONGODB_DATABASE = "HCU-SIMULATION";

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
            GSConsumer.start(
                    5, 1, scheduler, manager, taskConfig, svcConfig);

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
