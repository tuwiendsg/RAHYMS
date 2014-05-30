package scu.cloud.discoverer;

import java.util.ArrayList;
import java.util.LinkedList;

import scu.common.fuzzy.MembershipFunction;
import scu.common.fuzzy.function.SkillMembershipFunctionCollection;
import scu.common.interfaces.DiscovererInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.Functionality;
import scu.common.model.HumanComputingElement;
import scu.common.model.Metrics;
import scu.common.model.Service;
import scu.common.sla.Objective;
import scu.common.sla.Specification;

public class Discoverer implements DiscovererInterface {

    private ServiceManagerInterface manager;

    // cache
    private static LinkedList<Service> serviceCache = null; 

    public Discoverer(ServiceManagerInterface manager) {
        
    }

    @Override
    public DiscovererInterface setServiceManager(
            ServiceManagerInterface serviceManager) {
        this.manager = serviceManager;
        return this;
    }

    @Override
    public ArrayList<Service> discoverServices(Functionality functionality,
            Specification specification) {
        return discoverServices(functionality, specification, 0, 0, 0);
    }

    @Override
    public ArrayList<Service> discoverServices(Functionality functionality,
            Specification specification,
            int timeStart, int duration, int deadline) {   

        if (serviceCache==null) serviceCache = 
                (LinkedList<Service>) manager.retrieveServices(functionality);

        ArrayList<Service> services = new ArrayList<Service>();

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
                Metrics metrics = null;
                if (service.getMetrics().has("response_time")) {
                    metrics = service.getMetrics();
                } else if (service.getProvider().getMetrics().has("response_time")) {
                    metrics = service.getProvider().getMetrics();
                }
                if (metrics!=null) {
                    int responseTime = (int) metrics.getValue(
                            "response_time", new Object[]{timeStart, duration});
                    if (responseTime==-1 || responseTime>deadline) {
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
        Double skillScore = (Double) element.getSkills().getValue(objective.getName());
        if (skillScore==null || (skillScore<=lowerBound || skillScore>=upperBound)) {
            match = false;
        }

        return match;
    }


}
