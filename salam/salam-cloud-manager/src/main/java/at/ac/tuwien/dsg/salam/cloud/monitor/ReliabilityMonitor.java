package at.ac.tuwien.dsg.salam.cloud.monitor;

import at.ac.tuwien.dsg.salam.cloud.monitor.helper.ReliabilityCalculator;
import at.ac.tuwien.dsg.salam.common.interfaces.MetricMonitorInterface;
import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.Service;

public class ReliabilityMonitor implements MetricMonitorInterface {

    @Override
    public Object measure(Service service, String name, Object[] params) {
        if (!name.equalsIgnoreCase("reliability")) return null;
        return measure(service.getProvider(), name, params);
    }

    @Override
    public Object measure(ComputingElement element, String name, Object[] params) {
        if (!name.equalsIgnoreCase("reliability")) return null;
        int k = element.getAssignmentCount();
        Boolean countNext = false;
        double clock = (Double)params[0];
        if (params.length>1) countNext = (Boolean)params[1];
        if (countNext) k++;
        return ReliabilityCalculator.single(element, k, clock);
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
