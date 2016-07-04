package at.ac.tuwien.dsg.hcu.cloud.discoverer;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.hcu.common.fuzzy.MembershipFunction;
import at.ac.tuwien.dsg.hcu.common.fuzzy.function.SkillMembershipFunctionCollection;
import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.HumanComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.sla.Objective;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;

public class Discoverer implements DiscovererInterface {

    private ServiceManagerInterface manager;

    // cache
    private static List<Service> serviceCache = null; 

    public Discoverer(ServiceManagerInterface manager) {
        this.manager = manager;        
    }

    public List<Service> discoverServices(Functionality functionality,
            Specification specification) {
        return discoverServices(functionality, specification, 0, 0, 0);
    }

    public List<Service> discoverServices(Functionality functionality,
            Specification specification,
            double timeStart, double load, double deadline) {   

        serviceCache = (List<Service>) manager.retrieveServices(functionality);

        List<Service> services = new ArrayList<Service>();

        for (Service service: serviceCache) {
            
            boolean match = true;
            
            for (Objective objective: specification.getObjectives()) {
                switch (objective.getType()) {
                case STATIC:
                    match = isPropertyMatched(service, objective);
                    break;
                case METRIC:
                    match = isMetricMatched(service, objective);
                    break;
                case SKILL:
                    match = isSkillMatched(service, objective);
                    break;
                default:
                    break;
                }
                if (!match) break;
            }
        
            // check response time
            if (match && deadline>0) {
                Double estimatedResponseTime = (Double) service.getMetric(
                                "response_time", new Object[]{timeStart, load});
                if (estimatedResponseTime!=null) {
                    if (estimatedResponseTime > deadline) {
                        // deadline will be violated if we assign to this service
                        match = false;
                    }
                } else {
                    match = false;
                }
                
            }

            if (match) services.add(service);
        }

        return services;
    }
    
    private boolean isPropertyMatched(Service service, Objective objective) {
        // for now we only consider skill for filtering services
        return true;
    }
    
    private boolean isMetricMatched(Service service, Objective objective) {
        // for now we only consider skill for filtering services
        return true;
    }

    private boolean isSkillMatched(Service service, Objective objective) {

        boolean match = true;
        
        MembershipFunction function = SkillMembershipFunctionCollection
                .getInstance()
                .getMembershipFunction((String) objective.getValue());
        double lowerBound = function.lowerBound();
        double upperBound = function.upperBound();
        HumanComputingElement element = (HumanComputingElement)service.getProvider();
        Double skillScore = (Double) element.getSkills().getValue(objective.getName(), 0.0);
        if (skillScore==null || (skillScore<=lowerBound || skillScore>=upperBound)) {
            match = false;
        }

        return match;
    }

    @Override
    public List<Connection> discoverConnections(List<Service> services) {
        
        ArrayList<Connection> connections = new ArrayList<Connection>();

        // get a list of computing elements serving the services
        ArrayList<ComputingElement> elements = new ArrayList<ComputingElement>();
        ArrayList<Long> added = new ArrayList<Long>();
        for (Service s: services) {
            if (added.indexOf(s.getProvider().getId())==-1) {
                elements.add(s.getProvider());
                added.add(s.getProvider().getId());
            }
        }
        
        // special case for single worker
        if (elements.size()==1) {
          connections.add(new Connection(elements.get(0).getId()));
        }
          
        // iterate to get the connections, assuming that the connection is undirectional
        for (int i=0; i<elements.size(); i++) {
            ComputingElement e1 = elements.get(i);
            for (int j=i+1; j<elements.size(); j++) {
                ComputingElement e2 = elements.get(j);
                Connection c = e1.getConnection(e2);
                if (c!=null) connections.add(c);
          }
        }
        return connections;
    }


}
