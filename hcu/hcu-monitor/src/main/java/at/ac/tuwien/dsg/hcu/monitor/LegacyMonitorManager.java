package at.ac.tuwien.dsg.hcu.monitor;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.RuleEngineInterface;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.AssignmentListener;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.CollectiveListener;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.FinishListener;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.utilization.HCUUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.utilization.HumanUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.utilization.MachineUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.old_stream.AssignmentStream;
import at.ac.tuwien.dsg.hcu.monitor.old_stream.BaseStream;
import at.ac.tuwien.dsg.hcu.monitor.old_stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.monitor.old_stream.UnitStream;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class LegacyMonitorManager implements MonitorInterface {

    private static final String ENGINE_URI = "dsg.tuwien.ac.at/hcu/monitor/MonitorManager"; 

    // define event streams
    @SuppressWarnings("unchecked")
    private static Class<BaseStream>[] eventTypes = new Class[]{
        AssignmentStream.class,
        UnitStream.class,
        CollectiveStream.class
        };
    
    // define event listeners
    @SuppressWarnings("unchecked")
    private static Class<ListenerInterface>[] eventListeners =  new Class[]{
        CollectiveListener.class,
        AssignmentListener.class,
        HumanUtilizationListener.class,
        MachineUtilizationListener.class,
        HCUUtilizationListener.class,
        FinishListener.class
        };

    // define rules
    private static String ruleFile = "../hcu-monitor/rules/monitoring_rules.drl";
    
    private static LegacyMonitorManager instance = null;

    private Configuration configuration = null;
    private EPServiceProvider epService = null;
    private RuleEngine ruleEngine = null;
    
    private boolean enabled = false;
    private boolean initialized = false;
    
    public LegacyMonitorManager(boolean monitoringEnabled) {
        this.enabled = monitoringEnabled;
        if (monitoringEnabled) {
            initMonitorManager(eventTypes, eventListeners);
            ruleEngine.start();
        }
    }
    
    public void enable() {
        if (!enabled) {
            initMonitorManager(eventTypes, eventListeners);
            ruleEngine.start();
            enabled = true;
        }
    }

    public void disable() {
        if (enabled) {
            ruleEngine.stop();
            enabled = false;
        }
    }

    public void initMonitorManager(Class[] eventTypes, Class[] eventListeners) {

        if (initialized) {
            return;
        }
        
        Util.log("Setting up event processor engine");

        // initialize configuration
        configuration = new Configuration();
        configuration.getEngineDefaults().getExpression().setUdfCache(false);
        configuration.addPlugInSingleRowFunction("collectiveStatus", CollectiveStream.class.getName(), "getCollectiveStatus");
        configuration.addImport("at.ac.tuwien.dsg.hcu.monitor.stream.*");
        configuration.addImport("at.ac.tuwien.dsg.hcu.common.model.*");
        configuration.addImport("at.ac.tuwien.dsg.hcu.common.model.Task.Status");
        for (Class clazz: eventTypes) {
            configuration.addEventType(clazz);
        }
        
        // initialize EP engine
        epService = EPServiceProviderManager.getProvider(ENGINE_URI, configuration);
        epService.initialize();
        
        // run create windows
        LegacyWindowsCreator.run(epService);
        
        // initialize listeners
        for (Class clazz: eventListeners) {
            try {
                ListenerInterface listener = (ListenerInterface)clazz.getConstructor().newInstance();
                listener.initiate(epService, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // initialize rule engine
        ruleEngine = new RuleEngine(ruleFile);
        
    }

    @Override
    public void sendEvent(Object obj) {
        if (enabled) {
            epService.getEPRuntime().sendEvent(obj);
        }
    }

    public RuleEngineInterface getRuleEngine() {
        return ruleEngine;
    }


}
