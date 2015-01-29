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
import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.monitor.MonitorManager;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSConsumer;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.ConfigJsonArray;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;

public class RunSimulation {
    
    private static String DEFAULT_CONFIG = "config/consumer.properties";
    
    public static void main(String[] args) {

        Simulation simulation = new Simulation();
        boolean initResult = simulation.init(DEFAULT_CONFIG);
        if (initResult) {
            simulation.start();
        }

    }

}
