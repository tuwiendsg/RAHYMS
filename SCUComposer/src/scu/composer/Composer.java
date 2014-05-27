package scu.composer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import scu.common.interfaces.DiscovererInterface;
import scu.common.interfaces.MetricMonitorInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.Connection;
import scu.common.model.Service;
import scu.common.model.optimization.OptimizationObjective;
import scu.common.model.optimization.TaskWithOptimization;
import scu.composer.algorithm.ACOAlgorithm;
import scu.composer.algorithm.ComposerAlgorithmInterface;
import scu.composer.algorithm.aco.ACOVariantInterface;
import scu.composer.model.ConnectednessGraph;
import scu.composer.model.ConstructionGraph;
import scu.composer.model.Solution;
import scu.composer.model.SolutionComponent;
import scu.util.Util;


public class Composer {

    private static Logger logger = Logger.getLogger("ProvisionerLogger");
    private static Tracer globalTracer = null;
    private static String algoName;

    private String configFile;
    private TaskWithOptimization task;
    private ConstructionGraph constructionGraph;
    private ConnectednessGraph connectednessGraph;
    
    private ServiceManagerInterface manager;
    private DiscovererInterface discoverer;

    public Composer(String configFile, ServiceManagerInterface manager, 
            DiscovererInterface discoverer) {
        this.manager = manager;
        this.discoverer = discoverer;
        this.configFile = configFile;
        init();
    }
    
    public TaskWithOptimization getTask() {
        return task;
    }

    public void setTask(TaskWithOptimization task) {
        this.task = task;
    }

    public ConstructionGraph getConstructionGraph() {
        return constructionGraph;
    }

    public void setConstructionGraph(ConstructionGraph constructionGraph) {
        this.constructionGraph = constructionGraph;
    }

    public ConnectednessGraph getConnectednessGraph() {
        return connectednessGraph;
    }

    public void setConnectednessGraph(ConnectednessGraph connectednessGraph) {
        this.connectednessGraph = connectednessGraph;
    }

    public void init() {
        
        // init param
        algoName = Util.getProperty(configFile, "algorithm");
        System.out.println("Using " + algoName + ".");

        // init tracer
        String traceFilePrefix = Util.getProperty(configFile, "trace_file_prefix");
        if (traceFilePrefix!=null && !traceFilePrefix.equals("")) {
            logger.info("Initiating tracer.");
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
            Date now = new Date();
            String date = sdfDate.format(now);      
            globalTracer = new Tracer(traceFilePrefix + "global-" + date + ".csv");
            globalTracer.traceln(algoName);
            globalTracer.traceln("flag,algo_time,task,data,task," + globalTracer.getSolutionTraceHeader());
        }

        // start service
        //Endpoint.publish("http://localhost:8082/Composer", new ProvisionerWS());
        //logger.info("Composer Service started using " + algoName + " algorithm.");

    }

    public Solution compose(TaskWithOptimization task) {

        // TODO: handle sub-task
        
        Solution solution = new Solution();

        // check objective weight
        if (task.getOptObjective().getWeight("skill") + 
                task.getOptObjective().getWeight("connectedness") +
                task.getOptObjective().getWeight("cost") +
                task.getOptObjective().getWeight("time") <= 0) {
            logger.info("Invalid objective!");
            globalTracer.traceln(","+task.getName()+",,\"" + task.toString() + "\",\"Invalid objective!\"");
            return solution;
        }

        // init an instance
        this.setTask(task);

        // generate construction graph
        System.out.println("Generating construction graph.");
        ConstructionGraph constructionGraph = 
                new ConstructionGraph(manager, discoverer);
        constructionGraph = constructionGraph.generate(task);
        this.setConstructionGraph(constructionGraph);

        if (constructionGraph==null) {
            System.out.println("No feasible solution found!");
            globalTracer.traceln(","+task.getName()+",,\"" + task.toString() + "\",\"No feasible solution found!\"");
            return null;
        }

        // show graph
        System.out.println(constructionGraph.printLayers());
        //ShowGraphApplet<SolutionComponent, RelationEdge> applet = new ShowGraphApplet<SolutionComponent, RelationEdge>();
        //applet.showGraph(constructionGraph.getGraph());

        // generate connectedness graph
        System.out.println("Generating connectedness graph.");
        ArrayList<Service> services = constructionGraph.getServiceList();
        //ArrayList<WorkerRelation> relations = (ArrayList<WorkerRelation>)APIClient.metaCrowd().getWorkerRelations(workers);
        ArrayList<Connection> relations = (ArrayList<Connection>) manager.getConnections(services);
        ConnectednessGraph connectednessGraph = new ConnectednessGraph();
        connectednessGraph.generate(relations);
        this.setConnectednessGraph(connectednessGraph);

        // show graph
        //ShowGraphApplet<Worker, RelationEdge> applet = new ShowGraphApplet<Worker, RelationEdge>();
        //applet.showGraph(connectednessGraph);

        ComposerAlgorithmInterface algorithm = null;

        try {

            // init algorithm
            algorithm = (ComposerAlgorithmInterface)Class
                    .forName("scu.composer.algorithm." + algoName).newInstance();
            algorithm.init(configFile, constructionGraph, this);

            // solve
            long startTime = System.nanoTime();
            solution = algorithm.solve();
            long algoTime = System.nanoTime() - startTime;

            logger.info("Solution: " + solution.toString());

            // trace
            if (globalTracer!=null) {
                String data = "";
                if (solution!=null && solution.getList().size()>0) {
                    data = solution.getData();
                }
                String flag = "";
                if (this.isSolutionFeasible(solution)) flag = "f"; 
                globalTracer.trace(flag + "," + algoTime + ","+task.getName()+","
                        +data+",\"" + task.toString() + "\",");
                if (solution!=null && solution.getList().size()>0) {
                    globalTracer.traceln(solution, "");
                } else {
                    globalTracer.traceln("Algo can't find solution!"); 
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return solution;

    }

    public boolean isSolutionFeasible(Solution s) {
        
        OptimizationObjective objective = task.getOptObjective();

        double costLimit = (double) task.getObjectiveValue("cost_limit");
        long deadline = (int) task.getObjectiveValue("deadline");
        
        if (objective.getWeight("connectedness")>0 && 
                s.getList().size()>1 && s.getFuzzyConnectedness()==0.0) return false;
        else if (objective.getWeight("cost")>0 && 
                s.getCost()>costLimit) return false;
        
        // the following two should not happen because we have prune the construction graph
        else if (objective.getWeight("skill")>0 && s.getSkillScore()==0.0) return false;
        else if (objective.getWeight("time")>0 && s.getTime()>deadline) return false;
        
        else return true;
    }

    public ArrayList<Solution> filterFeasibleSolutions(ArrayList<Solution> solutions) {
        ArrayList<Solution> feasibleSolutions = new ArrayList<Solution>();
        for (Solution solution: solutions) {
            if (isSolutionFeasible(solution)) {
                feasibleSolutions.add(solution);
            }
        }
        return feasibleSolutions;
    }

    public Hashtable<Solution,Double> calculateObjectiveValues(ArrayList<Solution> solutions) {

        OptimizationObjective objective = task.getOptObjective();
        
        String connectednessQuality = (String) task.getObjectiveValue("connectedness");
        Hashtable<Solution,Double> objectiveValues = new Hashtable<Solution,Double>();

        // get objective weight
        double competencyWeight = objective.getWeight("skill");
        double connectednessWeight = objective.getWeight("connectedness");
        double rtWeight = objective.getWeight("time");
        double costWeight = objective.getWeight("cost");

        // pre calculate cost and response time (required for normalization)
        ArrayList<Double> costs = null;
        double maxCost = 0;
        if (costWeight>0) {
            costs = getCostForEachSolution(solutions);
            maxCost = Collections.max(costs);
        }
        ArrayList<Long> responseTimes = null;
        long maxResponseTime = 0;
        if (rtWeight>0) {
            responseTimes = getResponseTimeForEachSolution(solutions);
            maxResponseTime = Collections.max(responseTimes);
        }

        // calculate for each solution
        int k = 0;
        for (Solution solution: solutions) {

            // competency objective
            double competenceScore = 0;
            if (competencyWeight>0) {
                double minCompentency = Double.MAX_VALUE;
                for (SolutionComponent comp: solution.getList()) {
                    double competency = comp.getGradeOfSkill();
                    if (competency<minCompentency) minCompentency = competency;
                }
                if (minCompentency>Double.MAX_VALUE-1) competenceScore = 0; // selected workers do not have competency
                else competenceScore = minCompentency;
            }

            // connectedness objective
            double connectednessScore = 0;
            double fuzzyyConnectednessScore = 0;
            if (connectednessWeight>0) {
                connectednessScore = connectednessGraph.getConnectedness(solution);
                // fuzzify
                fuzzyyConnectednessScore = SolutionComponent.getGradeOfConnectedness(connectednessScore, connectednessQuality);
            }

            // cost objective
            double costScore = 0;
            double normCostScore = 0;
            if (costWeight>0) {
                costScore = costs.get(k);
                double center = (double) task.getObjectiveValue("cost_limit");
                normCostScore = center / (center + costScore);
            }

            // response time objective
            double responseTimeScore = 0;
            double normResponseTimeScore = 0;
            if (rtWeight>0) {
                responseTimeScore = responseTimes.get(k);
                double center = (int) task.getObjectiveValue("deadline") - 
                        task.getSubmissionTime() * 1.0;
                normResponseTimeScore = center / (center + responseTimeScore);
            }

            // aggregate objectivity scores
            double aggregateScore = ( 
                    (competencyWeight * (1-competenceScore)) +
                    (connectednessWeight * (1-fuzzyyConnectednessScore)) +
                    (rtWeight * (1-normResponseTimeScore)) +
                    (costWeight * (1-normCostScore)) ) 
                    / (competencyWeight + connectednessWeight + rtWeight + costWeight);

            // save the measurement values on the first element
            solution.setSkillScore(competenceScore);
            solution.setConnectednessScore(connectednessScore);
            solution.setFuzzyConnectedness(fuzzyyConnectednessScore);
            solution.setTime(responseTimeScore);
            solution.setNormTime(normResponseTimeScore);
            solution.setCost(costScore);
            solution.setNormCost(normCostScore);
            solution.setAggregateScore(aggregateScore);

            objectiveValues.put(solution, aggregateScore);
            k++;
        }

        return objectiveValues;

    }

    private ArrayList<Double> getCostForEachSolution(ArrayList<Solution> solutions) {
        ArrayList<Double> costs = new ArrayList<Double>();
        for (Solution solution: solutions) {
            double cost = 0;
            for (SolutionComponent comp: solution.getList()) {
                cost += (double)comp.getAssignee().getProvider().getProperties()
                        .getValue("cost");
            }
            costs.add(cost);
        }
        return costs;
    }

    private ArrayList<Long> getResponseTimeForEachSolution(ArrayList<Solution> solutions) {
        ArrayList<Long> responseTimes = new ArrayList<Long>();
        for (Solution solution: solutions) {
            long responseTime = getMaxResponseTime(solution);
            responseTimes.add(responseTime);
        }
        return responseTimes;
    }

    private long getMaxResponseTime(Solution s) {
        long max = 0;
        for (SolutionComponent comp: s.getList()) {
            if (max<comp.getEstimatedResponseTime()) {
                max = comp.getEstimatedResponseTime();
            }
        }
        return max;
    }

    public Hashtable<SolutionComponent,Double> calculateHeuristicScores(
            ArrayList<SolutionComponent> options, Solution solutionSoFar) {

        // init vars
        Hashtable<SolutionComponent,Double> scores = new Hashtable<SolutionComponent,Double>();
        ArrayList<Double> deltaConnectedness = new ArrayList<Double>();
        ArrayList<Double> deltaCost = new ArrayList<Double>();
        ArrayList<Long> deltaRT = new ArrayList<Long>();
        long prevRT = 0;

        // get objective weights
        OptimizationObjective objective = task.getOptObjective();
        double competencyWeight = objective.getWeight("skill");
        double connectednessWeight = objective.getWeight("connectedness");
        double rtWeight = objective.getWeight("time");
        double costWeight = objective.getWeight("cost");

        // get response time so far
        if (rtWeight>0) {
            prevRT = getMaxResponseTime(solutionSoFar);
        }

        // first iteration to calculate medians
        for (SolutionComponent comp: options) {
            // connectedness
            if (connectednessWeight>0) {
                double dc = connectednessGraph.getDeltaConnectedness(solutionSoFar, 
                        comp.getAssignee().getProvider());
                deltaConnectedness.add(dc);
            }
            // cost
            if (costWeight>0) {
                deltaCost.add((double)comp.getAssignee().getProvider().getProperties()
                        .getValue("cost"));
            }
            // response time
            if (rtWeight>0) {
                if (comp.getEstimatedResponseTime()<prevRT) deltaRT.add(0L);
                else deltaRT.add(comp.getEstimatedResponseTime()-prevRT);
            }
        }

        // calculate connectedness median
        double medianConnectedness = 0;
        if (connectednessWeight>0) {
            medianConnectedness = (Collections.min(deltaConnectedness) + Collections.max(deltaConnectedness)) / 2;
        }

        // calculate cost median
        double medianCost = 0;
        if (costWeight>0) {
            medianCost = (Collections.min(deltaCost) + Collections.max(deltaCost)) / 2;
        }

        // calculate response time median
        double medianRT = 0;
        if (rtWeight>0) {
            medianRT = (Collections.min(deltaRT) + Collections.max(deltaRT)) / 2;
        }

        // second iteration to measure heuristic factors
        int i = 0;
        while (i<options.size()) {

            double connScore = 0;
            //if (medianConnectedness>0) connScore = deltaConnectedness.get(i) / (medianConnectedness +  deltaConnectedness.get(i));
            //double connCenter = ((InfinityTrapezoidalMembershipFunction)ConnectednessMembershipFunctions.getInstance().getMembershipFunction(task.getConnectedness())).center();
            double connCenter = 0.5;
            if (connectednessWeight>0) connScore = deltaConnectedness.get(i) / (connCenter +  deltaConnectedness.get(i));

            double costScore = 0;
            //if (medianCost>0) costScore = medianCost / (medianCost + deltaCost.get(i));
            double costCenter = (double) task.getObjectiveValue("cost_limit");
            if (medianCost>0) costScore = costCenter / (costCenter + deltaCost.get(i));

            double rtScore = 0;
            if (medianRT>0) rtScore = medianRT / (medianRT + deltaRT.get(i));

            double competencyScore = 0;
            if (competencyWeight>0) competencyScore = options.get(i).getGradeOfSkill();

            // calculate heuristic factor
            double heuristicScore = 
                    ( (connScore * connectednessWeight) + 
                            (costScore * costWeight) + 
                            (rtScore * rtWeight) +
                            (competencyScore * competencyWeight) ) / 
                            (connectednessWeight + costWeight + rtWeight + competencyWeight);

            scores.put(options.get(i), heuristicScore);

            i++;
        }

        return scores;
    }

    public double calculateObjectiveValue(Solution solution) {
        ArrayList<Solution> solutionList = new ArrayList<Solution>();
        solutionList.add(solution);
        Hashtable<Solution, Double> values = calculateObjectiveValues(solutionList);
        return values.get(solution);
    }
}
