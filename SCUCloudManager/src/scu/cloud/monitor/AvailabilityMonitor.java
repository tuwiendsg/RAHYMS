package scu.cloud.monitor;

import java.util.Hashtable;

import scu.cloud.generator.AvailabilityGenerator;
import scu.common.interfaces.MetricMonitorInterface;
import scu.common.model.ComputingElement;
import scu.common.model.Service;
import scu.util.ConfigJson;
import scu.util.Util;

public class AvailabilityMonitor implements MetricMonitorInterface {

    private static AvailabilityMonitor _instance;
    private static AvailabilityGenerator generator;
    private Hashtable<Long, String> availabilityCache;

    public AvailabilityMonitor() {
        availabilityCache = new Hashtable<Long, String>();
    }

    public static void initGenerator(ConfigJson config) {
        generator = new AvailabilityGenerator(config);
    }

    public static AvailabilityMonitor getInstance() {
        if (_instance==null) _instance = new AvailabilityMonitor();
        return _instance;
    }

    @Override
    public Object measure(Service service, String name, Object[] params) {
        return getInstance().measureMetric(service.getProvider(), name, params);
    }

    @Override
    public Object measure(ComputingElement element, String name, Object[] params) {
        return getInstance().measureMetric(element, name, params);
    }

    private Object measureMetric(ComputingElement element, String name, Object[] params) {
        Object result = null;
        switch (name) {
        case "response_time":
            if (params.length==2) {
                long start = (long) params[0]; 
                long duration = (long) params[1]; 
                long response = earliestResponseTime(element.getId(), start, duration);
                result = response;
            }
            break;
        default:
            result = null;
        }
        return result;
    }

    private long earliestResponseTime(long id, long start, long duration) {

        // get availability sequence
        String sequence = availabilityCache.get(id);
        if (sequence==null) {
            sequence = generator.generate((duration>30?duration:30), "");
        }

        String block = Util.stringRepeat("1", duration);
        int pos = sequence.indexOf(block, (int) start);

        int maxExtension = 5;
        int i = 0;
        while (pos==-1 && i<maxExtension) {
            sequence += generator.generate((duration>30?duration:30), sequence);
            pos = sequence.indexOf(block, (int) start);
            i++;
        }
        availabilityCache.put(id, sequence);

        if (pos==-1) {
            return -1;
        } else {
            return pos+duration;
        }


    }
}
