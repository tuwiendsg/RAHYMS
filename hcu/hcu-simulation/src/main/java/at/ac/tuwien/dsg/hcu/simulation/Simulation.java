package at.ac.tuwien.dsg.hcu.simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import at.ac.tuwien.dsg.hcu.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.hcu.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.NegotiateInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.WorkerManagerInterface;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.hcu.util.ComponentImplementation;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.ConfigJsonArray;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;

public class Simulation {

    // configs
    private String config;
    private ConfigJson scenarioConfig;
    private String scenarioConfigPath;
    private ConfigJsonArray tracerConfig;
    private String composerConfig;
    private ArrayList<ConfigJson> taskConfig;
    private ArrayList<ConfigJson> svcConfig;
    
    // components
    ServiceManagerInterface manager;
    WorkerManagerInterface workerManager;
    DiscovererInterface discoverer;
    DependencyProcessorInterface dp;
    ComposerInterface composer;
    SchedulerInterface scheduler;
    MonitorInterface monitor;
    NegotiateInterface negotiator;
    
    public boolean init(String config) {

        this.config = config;
        
        // scenario config
        String scenarioConfigFile = Util.getProperty(config, "scenario_config");
        if (scenarioConfigFile==null) {
            System.out.println("scenario_config not specified");
            return false;
        }
        try {
            scenarioConfig = new ConfigJson(scenarioConfigFile);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            System.out.println("Invalid scenario_config");
            return false;
        }
        scenarioConfigPath = (new File(scenarioConfigFile)).getParent();
        
        // tracer config
        String tracerConfigFile = Util.getProperty(config, "tracer_config");
        if (tracerConfigFile!=null) {
            try {
                tracerConfig = new ConfigJsonArray(tracerConfigFile);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                System.out.println("Invalid tracer_config");
                return false;
            }
        }

        // composer config
        composerConfig = Util.getProperty(config, "composer_config");
        if (composerConfig==null) {
            System.out.println("composer_config not specified");
            return false;
        }

        return initComponents();
    }
    
    private boolean initComponents() {

        try {
            // task config
            taskConfig = new ArrayList<ConfigJson>();
            String taskGenBaseDir = scenarioConfig.getRoot().getJSONObject("task_generator").getString("basedir");
            taskGenBaseDir = scenarioConfigPath + "/" + taskGenBaseDir;
            JSONArray taskGenList = scenarioConfig.getRoot().getJSONObject("task_generator").getJSONArray("files");
            for (int i=0; i<taskGenList.length(); i++) {
                    taskConfig.add(new ConfigJson(taskGenBaseDir + taskGenList.getString(i)));
            }
            // service config
            if (scenarioConfig.getRoot().has("service_generator")) {
                svcConfig = new ArrayList<ConfigJson>();
                String svcGenBaseDir = scenarioConfig.getRoot().getJSONObject("service_generator").getString("basedir");
                svcGenBaseDir = scenarioConfigPath + "/" + svcGenBaseDir;
                JSONArray svcGenList = scenarioConfig.getRoot().getJSONObject("service_generator").getJSONArray("files");
                for (int i=0; i<svcGenList.length(); i++) {
                    svcConfig.add(new ConfigJson(svcGenBaseDir + svcGenList.getString(i)));
                }
            }
            
            // init tracers
            if (tracerConfig!=null) {
                Tracer.initFromConfig(tracerConfig);
            }
            
            // init components
            boolean hasServiceManager = scenarioConfig.getRoot().getJSONObject("implementationClasses").has("serviceManager");
            Map<String, Object> implementationConfig = null;
            if (scenarioConfig.getRoot().has("implementationConfigurations")) {
                implementationConfig = Util.jsonObjectToMap(scenarioConfig.getRoot().getJSONObject("implementationConfigurations"));
            }
            // service manager
            if (hasServiceManager) {
                manager = (ServiceManagerInterface) getImplementation("serviceManager", new Object[]{});
                if (implementationConfig!=null && implementationConfig.get("serviceManager")!=null) {
                    manager.setConfiguration((Map<String, Object>) implementationConfig.get("serviceManager"));
                }
            }
            // discoverer
            discoverer = (DiscovererInterface) getImplementation("discoverer", new Object[]{manager});
            if (implementationConfig!=null && implementationConfig.get("discoverer")!=null) {
                discoverer.setConfiguration((Map<String, Object>) implementationConfig.get("discoverer"));
            }
            // dependency processor, currently it is fixed, implementing our collective dependecy model
            dp = new DependencyProcessor();
            // composer, composer is configured using a special properties file, not using the scenario implementationConfig
            composer = (ComposerInterface) getImplementation("composer", new Object[]{composerConfig, discoverer, dp});
            // scheduler
            scheduler = (SchedulerInterface) getImplementation("scheduler", new Object[]{composer, dp});
            if (implementationConfig!=null && implementationConfig.get("scheduler")!=null) {
                scheduler.setConfiguration((Map<String, Object>) implementationConfig.get("scheduler"));
            }
            // monitor
            boolean monitoringEnabled = Boolean.parseBoolean(Util.getProperty(config, "monitor"));
            monitor = (MonitorInterface) getImplementation("monitor", new Object[]{monitoringEnabled});
            if (implementationConfig!=null && implementationConfig.get("monitor")!=null) {
                monitor.setConfiguration((Map<String, Object>) implementationConfig.get("monitor"));
            }
            // negotiator
            negotiator = (NegotiateInterface) getImplementation("negotiator", new Object[]{});
            if (implementationConfig!=null && implementationConfig.get("negotiator")!=null) {
                negotiator.setConfiguration((Map<String, Object>) implementationConfig.get("negotiator"));
            }
            scheduler.setNegotiatorInterface(negotiator);
            // worker manager
            boolean hasWorkerManager = scenarioConfig.getRoot().getJSONObject("implementationClasses").has("workerManager");
            if (hasWorkerManager) {
                workerManager = (WorkerManagerInterface) getImplementation("workerManager", new Object[]{});
                if (implementationConfig!=null && implementationConfig.get("workerManager")!=null) {
                    workerManager.setConfiguration((Map<String, Object>) implementationConfig.get("workerManager"));
                }
            }
        
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
    public void start() {
        String title = scenarioConfig.getRoot().getString("title");
        System.out.println("Running HCU simulation " + title);
        // start the consumer, using GridSim the consumer also acts as controller
        GSConsumer.start(
                scheduler,
                manager,
                workerManager,
                monitor,
                taskConfig,
                svcConfig,
                scenarioConfig.getRoot().getInt("numberOfCycles"),
                scenarioConfig.getRoot().getInt("waitBetweenCycle"),
                Util.getProperty(config, "export_arff_to"),
                Boolean.parseBoolean(Util.getProperty(config, "gridsim_debug"))
        );
    }
    
    protected Object getImplementation(String type, Object... params) {
        Object obj = null;
        String className = null;
        if (scenarioConfig.getRoot().has("implementationClasses")) {
            if (scenarioConfig.getRoot().getJSONObject("implementationClasses").has(type)) {
                className = scenarioConfig.getRoot().getJSONObject("implementationClasses").getString(type);
            }
        }
        obj = ComponentImplementation.getImplementation(type, className, params);
        if (obj==null) {
            System.err.println("Invalid " + type + " implementation " + className + ", exiting...");
            System.exit(1);
        }
        return obj;
    }
    
}
