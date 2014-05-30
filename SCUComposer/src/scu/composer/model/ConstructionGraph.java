package scu.composer.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.jgrapht.graph.ListenableDirectedWeightedGraph;

import scu.common.interfaces.DiscovererInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.Role;
import scu.common.model.Service;
import scu.common.model.Task;

// TODO: extends ListenableDirectedWeightedGraph<SolutionComponent, DefaultWeightedEdge> directly
// TODO: sort level by the number of workers available for that level
public class ConstructionGraph {

    private static Logger logger = Logger.getLogger("ProvisionerLogger");
    private ListenableDirectedWeightedGraph<SolutionComponent, RelationEdge> graph;
    private ArrayList<ArrayList<SolutionComponent>> componentList;
    private int numberOfRoles;
    private int maxNumberOfWorker = 0;

    private double minPheromone = 9999;
    private double maxPheromone = -9999;

    private SolutionComponent initialComponent;
    private SolutionComponent finalComponent;
    
    private ServiceManagerInterface manager;
    private DiscovererInterface discoverer;

    public ConstructionGraph(ServiceManagerInterface manager, DiscovererInterface discoverer) {
        this.manager = manager;
        this.discoverer = discoverer;
        init();
    }
    
    public void init() {
        this.graph = new ListenableDirectedWeightedGraph<SolutionComponent, RelationEdge>(RelationEdge.class);
        this.componentList = new ArrayList<ArrayList<SolutionComponent>>();        
    }

    public ListenableDirectedWeightedGraph<SolutionComponent, RelationEdge> getGraph() {
        return graph;
    }

    public double getMinPheromone() {
        return minPheromone;
    }

    public void updateMinMaxPheromone(double value) {
        if (value<minPheromone) this.minPheromone = value;
        if (value>maxPheromone) this.maxPheromone = value;
    }

    public double getMaxPheromone() {
        return maxPheromone;
    }

    public ArrayList<ArrayList<SolutionComponent>> getComponentList() {
        return componentList;
    }

    public int getNumberOfRoles() {
        return numberOfRoles;
    }

    public void setNumberOfRoles(int numberOfRoles) {
        this.numberOfRoles = numberOfRoles;
    }

    public int getMaxNumberOfWorker() {
        return maxNumberOfWorker;
    }

    public void setMaxNumberOfWorker(int maxNumberOfJob) {
        this.maxNumberOfWorker = maxNumberOfJob;
    }

    public SolutionComponent getInitialComponent() {
        return initialComponent;
    }

    public void setInitialComponent(SolutionComponent initialComponent) {
        this.initialComponent = initialComponent;
    }

    public SolutionComponent getFinalComponent() {
        return finalComponent;
    }

    public void setFinalComponent(SolutionComponent finalComponent) {
        this.finalComponent = finalComponent;
    }

    public ConstructionGraph generate(Task task) {

        init();

        // start node
        SolutionComponent sc0 = new SolutionComponent(0);
        this.getComponentList().add(new ArrayList<SolutionComponent>());
        this.getComponentList().get(0).add(sc0);
        this.getGraph().addVertex(sc0);
        this.setInitialComponent(sc0);

        // iterate task job / competence set
        int level = 1;
        for (Role role: task.getRoles()) {

            ArrayList<SolutionComponent> comps = generateSolutionComponents(
                    level, role, task);

            // no feasible solution at this level, we should not proceed
            if (comps==null) {
                logger.info("No workers found, submissionTime=" + 
                        task.getSubmissionTime() + ", role=" + role);
                return null;
            }

            // add vertices and edges
            this.getComponentList().add(comps);
            for (SolutionComponent sc: comps) {
                this.getGraph().addVertex(sc);
                for (SolutionComponent prevsc: this.getComponentList().get(level-1)) {
                    this.getGraph().addEdge(prevsc, sc);
                }
            }

            // update max number of worker
            if (comps.size()>this.getMaxNumberOfWorker()) {
                this.setMaxNumberOfWorker(comps.size());
            }
            level++;
        }

        // final node
        SolutionComponent scf = new SolutionComponent(level);
        this.getComponentList().add(new ArrayList<SolutionComponent>());
        this.getComponentList().get(level).add(scf);
        this.getGraph().addVertex(scf);
        for (SolutionComponent prevsc: this.getComponentList().get(level-1)) {
            this.getGraph().addEdge(prevsc, scf);
        }
        this.setFinalComponent(scf);

        this.setNumberOfRoles(level-1);

        return this;
    }

    public ArrayList<Service> getServiceList() {
        Hashtable<String, Service> services = new Hashtable<String, Service>(); 
        for (SolutionComponent c: getGraph().vertexSet()) {
            if (c.getAssignee()!=null && !services.containsKey(c.getAssignee().getTitle())) {
                services.put(c.getAssignee().getTitle(), c.getAssignee());
            }
        }
        return new ArrayList<Service>(services.values());
    }

    public double getPheromone(SolutionComponent comp) {
        return comp.getPheromone();
    }

    public void setPheromone(SolutionComponent comp, double value) {
        comp.setPheromone(value);
    }

    public ArrayList<SolutionComponent> generateSolutionComponents(
            int level, Role role, Task task) {

        ArrayList<SolutionComponent> components = new ArrayList<SolutionComponent>();

        int duration = estimateDuration(task.getLoad(), null);
        int submissionTime = task.getSubmissionTime();
        int deadline = (int)task.getSpecification()
                .findObjective("deadline") // deadline spec must exist
                .getValue();
        
        ArrayList<Service> services = discoverer.discoverServices(
                role.getFunctionality(), 
                task.getSpecification().merge(role.getSpecification()), 
                submissionTime, duration, deadline);

        // no feasible solution
        if (services.size()==0) return null;

        for (Service service: services) {
            long estDuration = estimateDuration(task.getLoad(), service);
            SolutionComponent comp = new SolutionComponent(level, service, task, role);
            Integer estimatedResponseTime = (Integer) service.getMetric(
                    "response_time", new Object[]{submissionTime, duration});
            if (estimatedResponseTime!=null) {
                comp.setEstimatedResponseTime(estimatedResponseTime);
                components.add(comp);
            }
        }
        return components;
    }

    public String getTrail(Solution solution) {
        String trail = "";
        for (int i=0; i<solution.getList().size(); i++) {
            SolutionComponent comp = solution.getList().get(i);
            if (trail.equals("")) trail = Double.toString(comp.getPheromone());
            else trail += ", " + Double.toString(comp.getPheromone());
        }
        return trail;
    }

    public String printLayers() {
        String s = "";
        int i = 0;
        for (ArrayList<SolutionComponent> layer: componentList) {
            if (!s.equals("")) s += "\n";
            s += "Layer #" + i + ":" + layer.size() + ": ";
            String scomp = "";
            for (SolutionComponent comp: layer) {
                if (!scomp.equals("")) scomp += ",";
                scomp += comp;
            }
            s += scomp;
            i++;
        }
        return s;
    }

    public BigInteger getSpaceSize() {
        BigInteger size = BigInteger.valueOf(1);
        for (ArrayList<SolutionComponent> layer: componentList) {
            size = size.multiply(BigInteger.valueOf(layer.size()));
        }
        return size;
    }

    public double getAverageNodeBranching() {
        long nodes = 0;
        for (int i=1; i<componentList.size()-1; i++) {
            ArrayList<SolutionComponent> layer = componentList.get(i);
            nodes += layer.size();
        }
        return nodes / componentList.size();
    }

    public double getAverageNodeBranching(double epsilonRatio) {
        long nodes = 0;
        for (int i=1; i<componentList.size()-1; i++) {
            ArrayList<SolutionComponent> layer = componentList.get(i);
            // get average of pheromone
            double avg = 0;
            for (SolutionComponent comp: layer) {
                avg = comp.getPheromone(); 
            }
            avg = avg / layer.size();
            // include only node with pheromone above average * epsilon
            for (SolutionComponent comp: layer) {
                if (comp.getPheromone()>avg*epsilonRatio) nodes++; 
            }
        }
        return nodes / componentList.size();
    }

    public String getLayersSize() {
        String size = "";
        for (int i=1; i<componentList.size()-1; i++) {
            ArrayList<SolutionComponent> layer = componentList.get(i);
            size += layer.size() + ",";
        }
        return "\"" + size + "\"";
    }
    
    private int estimateDuration(double load, Service service) {
        double perfRating = 1.0;
        if (service!=null) {
            perfRating = (double) service.getProvider().getProperties()
                    .getValue("performance_rating");
        }
        return (int) Math.ceil(load / perfRating);
    }

}
