package at.ac.tuwien.dsg.hcu.monitor;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.RuleEngineInterface;
import at.ac.tuwien.dsg.hcu.monitor.listener.CollectiveListener;
import at.ac.tuwien.dsg.hcu.monitor.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.monitor.listener.utilization.HCUUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.listener.utilization.HumanUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.listener.utilization.MachineUtilizationListener;
import at.ac.tuwien.dsg.hcu.monitor.stream.AssignmentStream;
import at.ac.tuwien.dsg.hcu.monitor.stream.BaseStream;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.monitor.stream.UnitStream;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class MonitorManager implements MonitorInterface {

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
        HumanUtilizationListener.class,
        MachineUtilizationListener.class,
        HCUUtilizationListener.class,
        CollectiveListener.class
        };

    // define rules
    private static String ruleFile = "D:\\study\\my research\\experiments\\RAHYMS\\hcu\\hcu-monitor\\rules\\monitoring_rules.drl";
    
    private static MonitorManager instance = null;

    private Configuration configuration = null;
    private EPServiceProvider epService = null;
    private RuleEngine ruleEngine = null;
    
    public MonitorManager() {
        this(eventTypes, eventListeners);
    }

    private MonitorManager(Class[] eventTypes, Class[] eventListeners) {

        Util.log("Setting up event processor engine");

        // initialize configuration
        configuration = new Configuration();
        configuration.addImport("at.ac.tuwien.dsg.hcu.monitor.stream.*");
        configuration.addImport("at.ac.tuwien.dsg.hcu.common.model.*");
        for (Class clazz: eventTypes) {
            configuration.addEventType(clazz);
        }

        // initialize EP engine
        epService = EPServiceProviderManager.getProvider(ENGINE_URI, configuration);
        epService.initialize();
        
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
        ruleEngine.start();

    }

    public static MonitorManager getInstance() {
        if (instance==null) {
            instance = new MonitorManager();
        }
        return instance;
    }

    @Override
    public void sendEvent(Object obj) {
        epService.getEPRuntime().sendEvent(obj);
    }

    public RuleEngineInterface getRuleEngine() {
        return ruleEngine;
    }


}
