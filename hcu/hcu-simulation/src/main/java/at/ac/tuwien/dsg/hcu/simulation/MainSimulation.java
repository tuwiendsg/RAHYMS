package at.ac.tuwien.dsg.hcu.simulation;

import at.ac.tuwien.dsg.hcu.cloud.discoverer.Discoverer;
import at.ac.tuwien.dsg.hcu.cloud.manager.ServiceManagerOnMemory;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.Scheduler;
import at.ac.tuwien.dsg.hcu.common.interfaces.*;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.monitor.legacy.LegacyMonitorManager;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.Util;
import org.json.JSONException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karaoglan on 06/04/16.
 */
public class MainSimulation {

    public static final String FILE_TEMP = "/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-simulation/config/";
    //todo brk config web den alinacak, basedirectory ile alakali
    private static String configFile = FILE_TEMP + "consumer.properties";

    //todo brk düzelt cok fazla param modeli uygun bir dependcy olan yere al, misal common ..
    public void runSimulation(List<String> units, List<String> tasks, String composerContent, int numberOfCycles, int waitBetweenCycles,
                              String simulationName, String simulationDescription) {

        try {

            Util.log().info("Initializing Main Simulation");

            // task config
            ArrayList<ConfigJson> taskConfig = new ArrayList<ConfigJson>();
            /*String taskGeneratorConfig = Util.getProperty(configFile, "task_generator_config");
            for (String configPath : taskGeneratorConfig.split(",")) {
                taskConfig.add(new ConfigJson(configPath));
            }*/

            int counter = 0;
            String taskFilePath = "";
            for (String taskJson : tasks) { //todo change subs
                Writer writer = null;

                try {
                    taskFilePath = FILE_TEMP + "task" + ++counter + ".json";
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(taskFilePath), "utf-8"));
                    writer.write(taskJson);
                } catch (IOException ex) {
                    // report
                } finally {
                    try {
                        writer.close();
                    } catch (Exception ex) {/*ignore*/}
                }

                taskConfig.add(new ConfigJson(taskFilePath));
            }

            counter = 0;

            // service config
            ArrayList<ConfigJson> svcConfig = new ArrayList<ConfigJson>();
            /*String serviceGeneratorConfig = Util.getProperty(configFile, "service_generator_config");
            for (String configPath : serviceGeneratorConfig.split(",")) {
                svcConfig.add(new ConfigJson(configPath));
            }*/

            String unitFilePath = "";
            for (String unitJson : units) {         //todo change sub.
                Writer writer = null;

                try {
                    unitFilePath = FILE_TEMP + "unit" + ++counter + ".json";
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(unitFilePath), "utf-8"));
                    writer.write(unitJson);
                } catch (IOException ex) {
                    // report
                } finally {
                    try {
                        writer.close();
                    } catch (Exception ex) {/*ignore*/}
                }

                svcConfig.add(new ConfigJson(unitFilePath));
            }

            // TODO: configure the availability behavior of resources

            // init components
            ServiceManagerInterface manager = new ServiceManagerOnMemory();
            DiscovererInterface discoverer = new Discoverer(manager);
            DependencyProcessorInterface dp = new DependencyProcessor();

            //todo brk change that take from content composer
            //todo brk bu ve yukaridaki unit ve task icin generic bir cözüm bul

            String composerFilePath = "";
            Writer writer = null;


            /*try {
                composerFilePath = FILE_TEMP + "composer4.properties";
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(composerFilePath), "utf-8"));
                writer.write(composerContent);
            } catch (IOException ex) {
                //todo brk bütün keyleri dolas ve = le bagla sadece.
                // report
            } finally {
                try {
                    writer.close();
                } catch (Exception ex) {}
            }*/

            ComposerInterface composer = new Composer(Util.getProperty(configFile, "composer_config"),/*
                    composerFilePath, *///todo brk change that su an yenilemiyor dosya icerisine atmiyor program esnasinda, properties ekledikten sonra web dede ayni sorun var
                    manager, discoverer, dp);

            SchedulerInterface scheduler = new Scheduler(composer, dp);
            
            // TODO: enable monitoring for simulation from the web
            boolean monitoringEnabled = false;
            MonitorInterface monitor = new LegacyMonitorManager(monitoringEnabled);

            // start the consumer
            GSConsumer.start(
                    scheduler,
                    manager,
                    monitor,
                    taskConfig,
                    svcConfig,
                    numberOfCycles,
                    waitBetweenCycles,
                    null,
                    false
            );


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
