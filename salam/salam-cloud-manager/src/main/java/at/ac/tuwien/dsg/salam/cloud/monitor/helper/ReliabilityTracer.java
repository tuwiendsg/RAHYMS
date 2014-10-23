package at.ac.tuwien.dsg.salam.cloud.monitor.helper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import at.ac.tuwien.dsg.salam.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.salam.common.model.Assignment;
import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.Functionality;
import at.ac.tuwien.dsg.salam.common.model.Service;
import at.ac.tuwien.dsg.salam.common.model.Task;
import at.ac.tuwien.dsg.salam.common.sla.Specification;
import at.ac.tuwien.dsg.salam.util.Tracer;

public class ReliabilityTracer extends Tracer {
    
    public static int K_MULTIPLIER = 2;
    private static int MIN_SUCCESFULL_COLLECTOR = 2;
    private static int MIN_SUCCESFULL_ASSESSOR = 1;
    private static int MIN_SUCCESFULL_SENSOR = 1;
    
    private DiscovererInterface discoverer;
    List<Service> vsuCollector;
    List<Service> vsuAssessor;
    List<Service> vsuSensor;
    List<ComputingElement> vsuCitizen;
    List<ComputingElement> vsuSurveyor;

    public ReliabilityTracer(String file, DiscovererInterface discoverer) {
        super(file);
        this.discoverer = discoverer;
    }
    
    private void initVSUs() {
        // discover VSUs. we doing fixed here, because we dont specify spec for the composer. 
        // and it will be easy to study the variability if we have fixed VSU
        vsuCollector = discoverer.discoverServices(new Functionality("Collector"), new Specification());
        vsuAssessor = discoverer.discoverServices(new Functionality("Assessor"), new Specification());
        vsuSensor = discoverer.discoverServices(new Functionality("Sensor"), new Specification());
        vsuCitizen = findElementWithName("Citizen");
        vsuSurveyor = findElementWithName("Surveyor");
        
        System.out.println("vsuCollector.size = " + vsuCollector.size());
        System.out.println("vsuAssessor.size = " + vsuAssessor.size());
        System.out.println("vsuSensor.size = " + vsuSensor.size());
    }

    public void traceln(Task task, ArrayList<ArrayList<Service>> vsus, ArrayList<Assignment> assignments, double clock, int k) {
        if (assignments.size()==1) return; // probably reassignment, skip
        String vsuMeasurement = "";
        double aggregateR = 1.0;
        for (ArrayList<Service> vsu: vsus) {
            double R = ReliabilityCalculator.dynamicService(vsu, MIN_SUCCESFULL_COLLECTOR, true, clock);
            aggregateR = aggregateR * R;
            vsuMeasurement += vsu.size() + "," + R + ",";
        }
        traceln(clock + "," + (k*K_MULTIPLIER) + "," + task.getId() + "," + task.getName() + "," + aggregateR + "," + vsus.size() + "," + vsuMeasurement);
    }

    
    public void traceln(Task task, ArrayList<Assignment> assignments, double clock, int k) {

        if (vsuCollector==null) {
            initVSUs();            
        }
        
        // VSU reliability
        double rCollector = ReliabilityCalculator.dynamicService(vsuCollector, MIN_SUCCESFULL_COLLECTOR, true, clock);
        double rAssessor = ReliabilityCalculator.dynamicService(vsuAssessor, MIN_SUCCESFULL_ASSESSOR, true, clock);
        double rSensor = ReliabilityCalculator.dynamicService(vsuSensor, MIN_SUCCESFULL_SENSOR, true, clock);
        double rCitizen = ReliabilityCalculator.dynamic(vsuCitizen, 1, true, clock);
        double rSurveyor = ReliabilityCalculator.dynamic(vsuSurveyor, 1, true, clock);
        
        // aggregate R
        // case of human only, machine only, and mixed collective are uniformly distributed
        double rHumanOnly = rCollector * rAssessor;
        double rMachineOnly = rSensor;
        double rMixed = rCollector * rAssessor * rSensor;
        double aggregateR = (rHumanOnly + rMachineOnly + rMixed) / 3;

        System.out.println((k*K_MULTIPLIER) + "," + aggregateR);
        
        traceln((clock-ReliabilityCalculator.startClock) + "," + (k*K_MULTIPLIER) + "," + task.getId() + "," + task.getName() + "," + aggregateR + "," + rCollector + "," + rAssessor + "," + rSensor + "," + rCitizen + "," + rSurveyor);
    }
    
    public String getTraceHeader() {
        return "clock,k,task_id,task_name,r_aggregate,r_collector,r_assessor,r_sensor,r_citizen,r_surveyor";
    }
    
    private List<ComputingElement> findElementWithName(String name) {
        List<Service> allServices = discoverer.discoverServices(null, new Specification());
        Hashtable<Long,ComputingElement> elements = new Hashtable<Long,ComputingElement>();
        for (Service service: allServices) {
            if (service.getProvider().getName().startsWith(name)) {
                if (!elements.containsKey(service.getProvider().getId())) {
                    elements.put(service.getProvider().getId(), service.getProvider());
                }
            }
        }
        return new ArrayList<ComputingElement>(elements.values());
    }
    
}
