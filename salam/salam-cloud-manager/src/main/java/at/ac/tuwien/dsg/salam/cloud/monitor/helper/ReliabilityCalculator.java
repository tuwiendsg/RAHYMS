package at.ac.tuwien.dsg.salam.cloud.monitor.helper;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.Service;

public class ReliabilityCalculator {

    public static double startClock = -1.0; // compensating simulation init delay
    
    public static double single(ComputingElement element, int k, double clock) {
        if (startClock < 0.0) startClock = clock;
        double R = 0;
        if (element.getProperties().getValue("fault_probability", null)!=null) {
            double p = (Double)element.getProperties().getValue("fault_probability", null);
            R = Math.pow((1-p), k*ReliabilityTracer.K_MULTIPLIER);
        } else if (element.getProperties().getValue("fault_rate", null)!=null) {
            double rate = (Double)element.getProperties().getValue("fault_rate", null);
            R = Math.exp(-1 * rate * (clock-startClock));
        }
        //System.out.println("R(" + element.getName() + "," + k + ") = " + R);
        return R;        
    }
    
    public static double single(ComputingElement element, boolean isCountingForNextK, double clock) {
        if (isCountingForNextK) return single(element, element.getAssignmentCount() + 1, clock);
        else return single(element, element.getAssignmentCount(), clock);
    }
    
    private static double dfsDynamicFailure(List<ComputingElement> elements, int index, double currentR, int nFailure, int minFailure, boolean isCountingForNextK, double clock) {
        if (index==elements.size()) {
            // no more
            if (nFailure >= minFailure) {
                return currentR;
            } else {
                return 0.0;
            }
        }
        if ((index-nFailure) > elements.size()-minFailure) { // nSuccess >= maxSuccess
            // stop
            return 0.0;
        }
        ComputingElement element = elements.get(index); 
        double r = single(element, isCountingForNextK, clock);
        index++;
        double rOnFailSide = dfsDynamicFailure(elements, index, currentR*(1-r), nFailure+1, minFailure, isCountingForNextK, clock);
        double rOnSuccessSide = dfsDynamicFailure(elements, index, currentR*r, nFailure, minFailure, isCountingForNextK, clock);
        return rOnFailSide + rOnSuccessSide;
    }
    
    public static double dynamic(List<ComputingElement> elements, int minSuccess, boolean isCountingForNextK, double clock) {
        int minFailureForDfs = elements.size() - (minSuccess -1);
        double dynamicFailure = dfsDynamicFailure(elements, 0, 1, 0, minFailureForDfs, isCountingForNextK, clock);
        return 1 - dynamicFailure;
    }

    public static double dynamicService(List<Service> services, int minSuccess, boolean isCountingForNextK, double clock) {
        // get list of elements from services
        List<ComputingElement> elements = new ArrayList<ComputingElement>();
        for (Service service: services) {
            ComputingElement provider = service.getProvider();
            if (!elements.contains(provider)) {
                elements.add(provider);
            }
        }
        return dynamic(elements, minSuccess, isCountingForNextK, clock);
    }

    public static double average(List<ComputingElement> elements, boolean isCountingForNextK, double clock) {
    	double sum = 0.0;
    	int n = 0;
        for (ComputingElement element: elements) {
        	sum += single(element, isCountingForNextK, clock);
        	n++;
        }
    	return (sum / n);
    }

    public static double averageAssignmentCounter(List<ComputingElement> elements, double clock) {
    	double sum = 0.0;
    	int n = 0;
        for (ComputingElement element: elements) {
        	sum += element.getAssignmentCount();
        	n++;
        }
    	return (sum / n);
    }
}
