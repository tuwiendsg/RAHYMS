package at.ac.tuwien.dsg.hcu.simulation;

import at.ac.tuwien.dsg.hcu.cloud.discoverer.Discoverer;
import at.ac.tuwien.dsg.hcu.cloud.manager.ServiceManagerOnMemory;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.hcu.cloud.scheduler.Scheduler;
import at.ac.tuwien.dsg.hcu.common.interfaces.*;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import at.ac.tuwien.dsg.hcu.monitor.legacy.LegacyMonitorManager;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.ConfigJsonArray;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


import java.io.*;

public class MainWebSimulation {

    public void runSimulation(
            List<String> units,
            List<String> tasks,
            String composerContent,
            int numberOfCycles,
            int waitBetweenCycles,
            String tracerConfig,
            ObjectId simulationId
    ) {

        try {

            Util.log().info("Initializing Main Simulation");

            // task config
            ArrayList<ConfigJson> taskConfig = new ArrayList<ConfigJson>();
            int counter = 0;
            String taskFilePath = "";
            for (String taskJson : tasks) {
                Writer writer = null;

                try {
                    taskFilePath = "config/" + "task" + ++counter + ".json";
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

            String unitFilePath = "";
            for (String unitJson : units) {
                Writer writer = null;

                try {
                    unitFilePath = "config/" + "unit" + ++counter + ".json";
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

            String composerFilePath = "config/simulation-web-composer.properties";
            Properties prop = new Properties();
            OutputStream output = null;

            JSONObject jsonObject = new JSONObject(composerContent);
            Set keys = jsonObject.keySet();
            Iterator a = keys.iterator();

            while (a.hasNext()) {
                String key = (String) a.next();
                // loop to get the dynamic key
                Object value = jsonObject.get(key);
                if(value instanceof Integer) {
                    Integer intValue = (Integer) value;
                    prop.setProperty(key, intValue.toString());

                } else if (value instanceof Double) {
                    Double doubleValue = (Double) value;
                    prop.setProperty(key, doubleValue.toString());

                } else {
                    prop.setProperty(key, (String) value);
                }


            }

            try {


                output = new FileOutputStream(composerFilePath);;

                // save properties to project root folder
                prop.store(output, null);

            } catch (IOException io) {
                io.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            ConfigJsonArray tracerConfigArray = null;


            // tracer config
            if (tracerConfig!=null) {
                try {
                    tracerConfigArray = new ConfigJsonArray(tracerConfig);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    System.out.println("Invalid tracer_config");
                }
            }

            // init tracers
            if (tracerConfig!=null) {
                Tracer.initFromConfig(tracerConfigArray);
            }

            ComposerInterface composer = new Composer(
                    composerFilePath,
                    manager,
                    discoverer,
                    dp,
                    simulationId
                    );

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
                    false,
                    true
            );

            //simulation finished
            MongoDatabase.updateSimulationObject(simulationId, null, new Date().toString());

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
