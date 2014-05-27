package scu.common.interfaces;

import scu.common.model.ComputingElement;
import scu.common.model.Service;

public interface MetricMonitorInterface {

    // measurement
    public Object measure(Service service, String name, Object[] params);
    public Object measure(ComputingElement element, String name, Object[] params);
    
    // TODO: callback hook
    
}
