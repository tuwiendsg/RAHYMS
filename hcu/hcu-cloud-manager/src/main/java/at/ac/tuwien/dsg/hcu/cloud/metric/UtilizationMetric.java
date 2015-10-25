package at.ac.tuwien.dsg.hcu.cloud.metric;

import at.ac.tuwien.dsg.hcu.common.interfaces.MetricInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;

public class UtilizationMetric implements MetricInterface {
    
    // TODO: measured machine utilization based on the queue load
    
    private static final int MAX_ASSIGNMENT = 50;

    @Override
    public Object measure(Service service, String name, Object[] params) {
        if (!name.equalsIgnoreCase("utilization")) return null;
        return measure(service.getProvider(), name, params);
    }

    @Override
    public Object measure(ComputingElement element, String name, Object[] params) {
        if (!name.equalsIgnoreCase("utilization")) return null;
        int k = element.getAssignmentCount();
        int finished = element.getFinishedCount();
        double util = ((double)k - finished) / MAX_ASSIGNMENT;
        return util;
    }

    @Override
    public Object update(Service service, String name, Object[] params) {
        return null;
    }

    @Override
    public Object update(ComputingElement element, String name, Object[] params) {
        return null;
    }

}
