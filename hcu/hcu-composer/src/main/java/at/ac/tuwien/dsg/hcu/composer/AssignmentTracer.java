package at.ac.tuwien.dsg.hcu.composer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class AssignmentTracer extends Tracer {

    private DiscovererInterface discoverer;
    List<ComputingElement> pool;

    public AssignmentTracer(String file) {
        super(file);
    }

    public DiscovererInterface getDiscoverer() {
        return discoverer;
    }
    
    public void initPool() {
        List<Service> allServices = discoverer.discoverServices(null, new Specification());
        Hashtable<Long,ComputingElement> elements = new Hashtable<Long,ComputingElement>();
        for (Service service: allServices) {
            if (!elements.containsKey(service.getProvider().getId())) {
                elements.put(service.getProvider().getId(), service.getProvider());
            }
        }
        pool = new ArrayList<ComputingElement>(elements.values());
    }

    public void setDiscoverer(DiscovererInterface discoverer) {
        this.discoverer = discoverer;
    }

    public void traceln() {
        if (pool==null) {
            initPool();
        }
        String row = "";
        for (ComputingElement e: pool) {
            row += e.getAssignmentCount() + ",";
        }
        traceln(row);
    }

    public String getTraceHeader() {
        String header = "";
        /*
        if (pool==null) {
            initPool();
        }
        for (ComputingElement e: pool) {
            header += e.getName() + ",";
        } */
        return header;
    }

}
