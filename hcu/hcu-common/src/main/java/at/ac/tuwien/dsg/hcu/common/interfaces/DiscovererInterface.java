package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;

public interface DiscovererInterface {

    // discover service without time constraint
    public List<Service> discoverServices(Functionality functionality,
            Specification specification);   
    
    // discover service with time constraint
    List<Service> discoverServices(Functionality functionality,
            Specification specification, double timeStart, double load,
            double deadline);

    // retrieve connectedness of services
    public List<Connection> discoverConnections(List<Service> services);

    public void setConfiguration(Map<String, Object> config);
}
