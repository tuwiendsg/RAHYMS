package at.ac.tuwien.dsg.hcu.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ComponentImplementation {

    // default implementation classes
    public static String DEFAULT_SERVICE_MANAGER = "at.ac.tuwien.dsg.hcu.cloud.manager.ServiceManagerOnMemory";
    public static String DEFAULT_DISCOVERER = "at.ac.tuwien.dsg.hcu.cloud.discoverer.Discoverer";
    public static String DEFAULT_COMPOSER = "at.ac.tuwien.dsg.hcu.composer.Composer";
    public static String DEFAULT_SCHEDULER = "at.ac.tuwien.dsg.hcu.cloud.scheduler.Scheduler";
    public static String DEFAULT_NEGOTIATOR = "at.ac.tuwien.dsg.hcu.cloud.negotiator.SimpleNegotiator";
    public static String DEFAULT_MONITOR = "at.ac.tuwien.dsg.hcu.monitor.legacy.LegacyMonitorManager";

    public static String getDefaultImplentationClass(String type) {
        String className = "";
        switch (type) {
            case "serviceManager":
                className = DEFAULT_SERVICE_MANAGER;
                break;
            case "discoverer":
                className = DEFAULT_DISCOVERER;
                break;
            case "composer":
                className = DEFAULT_COMPOSER;
                break;
            case "scheduler":
                className = DEFAULT_SCHEDULER;
                break;
            case "negotiator":
                className = DEFAULT_NEGOTIATOR;
                break;
            case "monitor":
                className = DEFAULT_MONITOR;
                break;
        }
        return className;
    }

    public static Object getImplementation(String type, String className, Object... params) {
        Object obj = null;
        if (className==null || className.trim().equals("")) {
            className = getDefaultImplentationClass(type);
        }
        try {
            Constructor constructor = Class.forName(className).getConstructors()[0];
            obj = constructor.newInstance(params);
        } catch (SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
