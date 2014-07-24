package scu.common.interfaces;

import java.util.List;

import scu.common.model.Functionality;
import scu.common.model.Service;
import scu.common.sla.Specification;

public interface DiscovererInterface {

    // discover service without time constraint
    public List<Service> discoverServices(Functionality functionality,
            Specification specification);   
    
    // discover service with time constraint
    List<Service> discoverServices(Functionality functionality,
            Specification specification, double timeStart, double load,
            double deadline);

}
