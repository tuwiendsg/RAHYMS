package scu.common.interfaces;

import java.util.ArrayList;

import scu.common.model.Functionality;
import scu.common.model.Service;
import scu.common.sla.Specification;

public interface DiscovererInterface {

    // methods for setting middleware component
    public DiscovererInterface setServiceManager(
            ServiceManagerInterface serviceManager);

    // discover service without time constraint
    public ArrayList<Service> discoverServices(Functionality functionality,
            Specification specification);   
    
    // discover service with time constraint
    public ArrayList<Service> discoverServices(Functionality functionality,
            Specification specification,
            int timeStart, int duration, int deadline);

}
