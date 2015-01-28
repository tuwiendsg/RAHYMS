package at.ac.tuwien.dsg.hcu.cloud.metric.helper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class ReliabilityTracer extends Tracer {
    
    public static int K_MULTIPLIER = 1;
    private static int MIN_SUCCESFULL_COLLECTOR = 2;
    private static int MIN_SUCCESFULL_ASSESSOR = 1;
    private static int MIN_SUCCESFULL_SENSOR = 1;
    
    private DiscovererInterface discoverer;
    List<ComputingElement> vsuCollector;
    List<ComputingElement> vsuAssessor;
    List<ComputingElement> vsuSensor;
    List<ComputingElement> vsuCitizen;
    List<ComputingElement> vsuSurveyor;

    public ReliabilityTracer(String file, DiscovererInterface discoverer) {
        super(file);
        this.discoverer = discoverer;
    }
    
    private void initVSUs() {
        // discover VSUs. we doing fixed here, because we dont specify spec for the composer. 
        // and it will be easy to study the variability if we have fixed VSU
        List<Service> collectors = discoverer.discoverServices(new Functionality("Collector"), new Specification());
        List<Service> assessors = discoverer.discoverServices(new Functionality("Assessor"), new Specification());
        List<Service> sensors = discoverer.discoverServices(new Functionality("Sensor"), new Specification());
        vsuCollector = ComputingElement.getElementsFromServices(collectors);
        vsuAssessor = ComputingElement.getElementsFromServices(assessors);
        vsuSensor = ComputingElement.getElementsFromServices(sensors);
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
        double rCollector = ReliabilityCalculator.dynamic(vsuCollector, MIN_SUCCESFULL_COLLECTOR, false, clock);
        double rAssessor = ReliabilityCalculator.dynamic(vsuAssessor, MIN_SUCCESFULL_ASSESSOR, false, clock);
        double rSensor = ReliabilityCalculator.dynamic(vsuSensor, MIN_SUCCESFULL_SENSOR, false, clock);
        double rCitizen = ReliabilityCalculator.dynamic(vsuCitizen, 1, false, clock);
        double rSurveyor = ReliabilityCalculator.dynamic(vsuSurveyor, 1, false, clock);
        
        // aggregate R
        // case of human only, machine only, and mixed collective are uniformly distributed
        double rHumanOnly = rCollector * rAssessor;
        double rMachineOnly = rSensor;
        double rMixed = rCollector * rAssessor * rSensor;
        double aggregateR = (rHumanOnly + rMachineOnly + rMixed) / 3;

        //System.out.println((k*K_MULTIPLIER) + "," + aggregateR);
        
        // average
        double avgCollector = ReliabilityCalculator.average(vsuCollector, false, clock);
        double avgAssessor = ReliabilityCalculator.average(vsuAssessor, false, clock);
        double avgSensor = ReliabilityCalculator.average(vsuSensor, false, clock);
        double avgCitizen = ReliabilityCalculator.average(vsuCitizen, false, clock);
        double avgSurveyor = ReliabilityCalculator.average(vsuSurveyor, false, clock);

        double avgCollectorCount = ReliabilityCalculator.averageAssignmentCounter(vsuCollector, clock);
        double avgAssessorCount = ReliabilityCalculator.averageAssignmentCounter(vsuAssessor, clock);
        double avgSensorCount = ReliabilityCalculator.averageAssignmentCounter(vsuSensor, clock);
        double avgCitizenCount = ReliabilityCalculator.averageAssignmentCounter(vsuCitizen, clock);
        double avgSurveyorCount = ReliabilityCalculator.averageAssignmentCounter(vsuSurveyor, clock);
        
        traceln((clock-ReliabilityCalculator.startClock) + "," + (k*K_MULTIPLIER) + "," + task.getId() + "," + task.getName() + "," + aggregateR + "," + rCollector + "," + rAssessor + "," + rSensor + "," + rCitizen + "," + rSurveyor + "," + avgCollector + "," + avgAssessor + "," + avgSensor + "," + avgCitizen + "," + avgSurveyor + "," + avgCollectorCount + "," + avgAssessorCount + "," + avgSensorCount + "," + avgCitizenCount + "," + avgSurveyorCount);
    }
    
    public String getTraceHeader() {
        return "clock,k,task_id,task_name,r_aggregate,r_collector,r_assessor,r_sensor,r_citizen,r_surveyor,avg_collector,avg_assessor,avg_sensor,avg_citizen,avg_surveyor,avg_collector_count,avg_assessor_count,avg_sensor_count,avg_citizen_count,avg_surveyor_count";
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
