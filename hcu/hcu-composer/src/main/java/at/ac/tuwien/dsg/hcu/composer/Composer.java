package at.ac.tuwien.dsg.hcu.composer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import at.ac.tuwien.dsg.hcu.cloud.metric.helper.ReliabilityTracer;
import at.ac.tuwien.dsg.hcu.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.OptimizationObjective;
import at.ac.tuwien.dsg.hcu.common.model.Role;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.composer.algorithm.ComposerAlgorithmInterface;
import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import at.ac.tuwien.dsg.hcu.composer.model.ConnectednessGraph;
import at.ac.tuwien.dsg.hcu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import at.ac.tuwien.dsg.hcu.composer.model.SolutionComponent;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import gridsim.GridSim;
import org.bson.types.ObjectId;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


public class Composer implements ComposerInterface {

    private static ComposerTracer composerTracer = null;
    private static ReliabilityTracer reliabilityTracer = null;
    private static AssignmentTracer assignmentTracer = null;
    private static String algoName;
    private static int taskCounter = 0;

    private String dbTracerValues;
    private DBTracer dbTracer;
    private String configFile;
    private Task task;
    private ConstructionGraph constructionGraph;
    private ConnectednessGraph connectednessGraph;

    private ServiceManagerInterface manager;
    private DiscovererInterface discoverer;
    private DependencyProcessorInterface dp;

    private ObjectId simulationId;

    public Composer(String configFile,
                    ServiceManagerInterface serviceManager,
                    DiscovererInterface discoverer,
                    DependencyProcessorInterface dp,
                    ObjectId simulationId) {

        this.configFile = configFile;
        this.manager = serviceManager;
        this.discoverer = discoverer;
        this.dp = dp;
        this.simulationId = simulationId;
        init();
    }

    public DependencyProcessorInterface getDp() {
        return dp;
    }

    public void setDp(DependencyProcessorInterface dp) {
        this.dp = dp;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
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

        dbTracerValues = "";

        //until here


        // init param
        algoName = Util.getProperty(configFile, "algorithm");
        Util.log().info("[Composer] Initializing using " + algoName + " algorithm");

        // get composer tracer
        composerTracer = (ComposerTracer) Tracer.getTracer("composer");

        if (composerTracer != null) {
            dbTracer = new DBTracer();

            if(simulationId != null) {
                MongoDatabase.updateSimulationObject(simulationId, composerTracer.getFileName(), null);
            }

        }

        // get reliability tracer
        reliabilityTracer = (ReliabilityTracer) Tracer.getTracer("reliability");
        if (reliabilityTracer != null) {
            reliabilityTracer.setDiscoverer(discoverer);
        }

        assignmentTracer = (AssignmentTracer) Tracer.getTracer("assignment");
        if (assignmentTracer != null) {
            assignmentTracer.setDiscoverer(discoverer);
        }

        // start service
        //Endpoint.publish("http://localhost:8082/Composer", new ProvisionerWS());
        //logger.info("Composer Service started using " + algoName + " algorithm.");

    }

    @Override
    public List<Assignment> partialCompose(Task task,
                                           List<Role> roles, double clock) {

        // TODO: handle sub-task

        // check objective weight
        if (task.getOptimizationObjective().getWeight("skill") +
                task.getOptimizationObjective().getWeight("connectedness") +
                task.getOptimizationObjective().getWeight("cost") +
                task.getOptimizationObjective().getWeight("time") <= 0) {
            Util.log().info("Invalid objective!");
            if (composerTracer != null) {
                composerTracer.traceln("," + task.getName() + ",,\"" + task.toString() + "\",\"Invalid objective!\"");

                //todo brk burasi cagirilmiyor aslinda
                dbTracerValues += "," + task.getName() + ",,\"" + task.toString() + "\",\"Invalid objective!\"";
            }
            return new ArrayList<Assignment>();
        }

        // init an instance
        this.setTask(task);

        // generate construction graph
        Util.log().info("Generating construction graph");
        ConstructionGraph constructionGraph =
                new ConstructionGraph(discoverer);
        constructionGraph = constructionGraph.generate(task, roles);
        this.setConstructionGraph(constructionGraph);

        if (constructionGraph == null) {
            Util.log().warning("No feasible solution found!");
            if (composerTracer != null) {
                composerTracer.traceln(GridSim.clock() + "," + task.getId() + ",\"\",0," + task.getName() + ",,\"" + task.toString() + "\",\"No feasible solution found!\"");
                //todo brk hata nasil?
                dbTracerValues += algoName;
                dbTracerValues += "," + GridSim.clock() + "," + task.getId() + ",\"\",0," + task.getName() + ",,\"" + task.toString() + "\",\"No feasible solution found!\"";
            }
            return null;
        }

        // show graph
        //System.out.println(constructionGraph.printLayers());
        //ShowGraphApplet<SolutionComponent, RelationEdge> applet = new ShowGraphApplet<SolutionComponent, RelationEdge>();
        //applet.showGraph(constructionGraph.getGraph());

        // generate connectedness graph
        Util.log().info("Generating connectedness graph");
        this.setConnectednessGraph(generateConnectednessGraph(constructionGraph.getServiceList()));

        // show graph
        //ShowGraphApplet<Worker, RelationEdge> applet = new ShowGraphApplet<Worker, RelationEdge>();
        //applet.showGraph(connectednessGraph);

        Solution solution = startAlgoritm();

        if (reliabilityTracer != null && constructionGraph != null) {
            reliabilityTracer.traceln(task, solution.getAssignments(), clock, taskCounter);
        }

        if (assignmentTracer != null && constructionGraph != null) {
            assignmentTracer.traceln();
        }

        return solution.getAssignments();

    }

    @Override
    public List<Assignment> compose(Task task, double clock) {
        taskCounter++;
        return partialCompose(task, null, clock);
    }

    private ConnectednessGraph generateConnectednessGraph(List<Service> services) {
        Util.log().info("Generating connectedness graph");
        ArrayList<Connection> relations = (ArrayList<Connection>) manager.getConnections(services);
        ConnectednessGraph connectednessGraph = new ConnectednessGraph();
        connectednessGraph.generate(relations);
        return connectednessGraph;
    }

    private Solution startAlgoritm() {

        Solution solution = new Solution();
        ComposerAlgorithmInterface algorithm = null;

        try {

            // init algorithm
            algorithm = (ComposerAlgorithmInterface) Class
                    .forName(this.getClass().getPackage().getName() + ".algorithm." + algoName).newInstance();
            algorithm.init(configFile, constructionGraph, this);

            // solve
            long startTime = System.nanoTime();
            solution = algorithm.solve();
            long algoTime = System.nanoTime() - startTime;

            //Util.log().warning("Solution: " + solution.toString());

            String data = "";
            String flag = "";
            // trace
            if (composerTracer != null) {
                if (solution != null && solution.size() > 0) {
                    data = solution.getData();
                }
                if (this.isSolutionFeasible(solution)) flag = "f";
                composerTracer.trace(GridSim.clock() + "," + task.getId() + "," + flag + "," + algoTime + "," + task.getName() + ","
                        + data + ",\"" + task.toString() + "\",");

                dbTracerValues += simulationId.toString();
                dbTracerValues += "," + algoName;
                dbTracerValues += "," + GridSim.clock() + "," + task.getId() + "," + flag + "," + algoTime + "," + task.getName() + ","
                        + data + "," + task.toString() + ",";

                if (solution != null && solution.size() > 0) {
                    composerTracer.traceln(solution, "");
                    dbTracerValues += dbTracer.trace(solution);

                } else {
                    composerTracer.traceln("Algo can't find solution!");
                }
            }

            dbTracer.traceln(dbTracerValues);
            dbTracerValues = new String("");

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

        OptimizationObjective objective = task.getOptimizationObjective();

        // get SLOs constraints
        double costLimit = (double) task.getObjectiveValue("cost_limit", 99999.0);
        double deadline = (double) task.getObjectiveValue("deadline", 99999.0);

        if (objective.getWeight("connectedness") > 0 &&
                s.size() > 1 && s.getFuzzyConnectedness() == 0.0) return false;
        else if (objective.getWeight("cost") > 0 &&
                s.getCost() > costLimit) return false;

        else if (objective.getWeight("skill") > 0 && s.getSkillScore() == 0.0) return false;
        else if (objective.getWeight("time") > 0) {
            double finishTime = dp.forecastFinishTime(s.getAssignments());
            if (finishTime > deadline) return false;
            else return true;
        } else return true;
    }

    public ArrayList<Solution> filterFeasibleSolutions(ArrayList<Solution> solutions) {
        ArrayList<Solution> feasibleSolutions = new ArrayList<Solution>();
        for (Solution solution : solutions) {
            if (isSolutionFeasible(solution)) {
                feasibleSolutions.add(solution);
            }
        }
        return feasibleSolutions;
    }

    public Hashtable<Solution, Double> calculateObjectiveValues(ArrayList<Solution> solutions) {

        OptimizationObjective objective = task.getOptimizationObjective();

        String connectednessQuality = (String) task.getObjectiveValue("connectedness", "poor");
        Hashtable<Solution, Double> objectiveValues = new Hashtable<Solution, Double>();

        // get objective weight
        double competencyWeight = objective.getWeight("skill");
        double connectednessWeight = objective.getWeight("connectedness");
        double rtWeight = objective.getWeight("time");
        double costWeight = objective.getWeight("cost");

        // pre calculate cost and response time (required for normalization)
        ArrayList<Double> costs = null;
        double maxCost = 0;
        if (costWeight > 0) {
            costs = getCostForEachSolution(solutions);
            maxCost = Collections.max(costs);
        }
        ArrayList<Double> responseTimes = null;
        double maxResponseTime = 0;
        if (rtWeight > 0) {
            responseTimes = getResponseTimeForEachSolution(solutions);
            maxResponseTime = Collections.max(responseTimes);
        }

        // calculate for each solution
        int k = 0;
        for (Solution solution : solutions) {

            // competency objective
            double competenceScore = 0;
            if (competencyWeight > 0) {
                double minCompentency = Double.MAX_VALUE;
                for (SolutionComponent comp : solution.getList()) {
                    double competency = comp.getGradeOfSkill();
                    if (competency < minCompentency) minCompentency = competency;
                }
                if (minCompentency > Double.MAX_VALUE - 1)
                    competenceScore = 0; // selected workers do not have competency
                else competenceScore = minCompentency;
            }

            // connectedness objective
            double connectednessScore = 0;
            double fuzzyyConnectednessScore = 0;
            if (connectednessWeight > 0) {
                connectednessScore = connectednessGraph.getConnectedness(solution);
                // fuzzify
                fuzzyyConnectednessScore = SolutionComponent.getGradeOfConnectedness(connectednessScore, connectednessQuality);
            }

            // cost objective
            double costScore = 0;
            double normCostScore = 0;
            if (costWeight > 0) {
                costScore = costs.get(k);
                double center = (double) task.getObjectiveValue("cost_limit", 99999.0);
                normCostScore = center / (center + costScore);
            }

            // response time objective
            double responseTimeScore = 0;
            double normResponseTimeScore = 0;
            if (rtWeight > 0) {
                responseTimeScore = responseTimes.get(k);
                double center = (double) task.getObjectiveValue("deadline", 99999.0) -
                        task.getSubmissionTime() * 1.0;
                normResponseTimeScore = center / (center + responseTimeScore);
            }

            // aggregate objectivity scores
            double aggregateScore = (
                    (competencyWeight * (1 - competenceScore)) +
                            (connectednessWeight * (1 - fuzzyyConnectednessScore)) +
                            (rtWeight * (1 - normResponseTimeScore)) +
                            (costWeight * (1 - normCostScore)))
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
        for (Solution solution : solutions) {
            double cost = 0;
            for (SolutionComponent comp : solution.getList()) {
                cost += (double) comp.getAssignee().getProvider().getProperties()
                        .getValue("cost", 0.0);
            }
            costs.add(cost);
        }
        return costs;
    }

    private ArrayList<Double> getResponseTimeForEachSolution(ArrayList<Solution> solutions) {
        ArrayList<Double> responseTimes = new ArrayList<Double>();
        for (Solution solution : solutions) {
            double responseTime = dp.forecastFinishTime(solution.getAssignments());
            responseTimes.add(responseTime);
        }
        return responseTimes;
    }

    public Hashtable<SolutionComponent, Double> calculateHeuristicScores(
            ArrayList<SolutionComponent> options, Solution solutionSoFar) {

        // init vars
        Hashtable<SolutionComponent, Double> scores = new Hashtable<SolutionComponent, Double>();
        ArrayList<Double> deltaConnectedness = new ArrayList<Double>();
        ArrayList<Double> deltaCost = new ArrayList<Double>();
        ArrayList<Double> deltaRT = new ArrayList<Double>();
        double prevRT = 0;

        // get objective weights
        OptimizationObjective objective = task.getOptimizationObjective();
        double competencyWeight = objective.getWeight("skill");
        double connectednessWeight = objective.getWeight("connectedness");
        double rtWeight = objective.getWeight("time");
        double costWeight = objective.getWeight("cost");

        // get response time so far
        if (rtWeight > 0) {
            prevRT = dp.forecastFinishTime(solutionSoFar.getAssignments());
        }

        // first iteration to calculate medians
        for (SolutionComponent comp : options) {
            // connectedness
            if (connectednessWeight > 0) {
                double dc = connectednessGraph.getDeltaConnectedness(solutionSoFar,
                        comp.getAssignee().getProvider());
                deltaConnectedness.add(dc);
            }
            // cost
            if (costWeight > 0) {
                deltaCost.add((double) comp.getAssignee().getProvider().getProperties()
                        .getValue("cost", 0.0));
            }
            // response time
            if (rtWeight > 0) {
                List<Assignment> assignmentSoFar = solutionSoFar.getAssignments();
                assignmentSoFar.add(comp);
                double forecastedResponseTime = dp.forecastFinishTime(assignmentSoFar);
                if (forecastedResponseTime < prevRT) deltaRT.add(0.0);
                else deltaRT.add(forecastedResponseTime - prevRT);
            }
        }

        // calculate connectedness median
        double medianConnectedness = 0;
        if (connectednessWeight > 0) {
            medianConnectedness = (Collections.min(deltaConnectedness) + Collections.max(deltaConnectedness)) / 2;
        }

        // calculate cost median
        double medianCost = 0;
        if (costWeight > 0) {
            medianCost = (Collections.min(deltaCost) + Collections.max(deltaCost)) / 2;
        }

        // calculate response time median
        double medianRT = 0;
        if (rtWeight > 0) {
            medianRT = (Collections.min(deltaRT) + Collections.max(deltaRT)) / 2;
        }

        // second iteration to measure heuristic factors
        int i = 0;
        while (i < options.size()) {

            double connScore = 0;
            //if (medianConnectedness>0) connScore = deltaConnectedness.get(i) / (medianConnectedness +  deltaConnectedness.get(i));
            //double connCenter = ((InfinityTrapezoidalMembershipFunction)ConnectednessMembershipFunctions.getInstance().getMembershipFunction(task.getConnectedness())).center();
            double connCenter = 0.5;
            if (connectednessWeight > 0)
                connScore = deltaConnectedness.get(i) / (connCenter + deltaConnectedness.get(i));

            double costScore = 0;
            //if (medianCost>0) costScore = medianCost / (medianCost + deltaCost.get(i));
            double costCenter = (double) task.getObjectiveValue("cost_limit", 99999.0);
            if (medianCost > 0) costScore = costCenter / (costCenter + deltaCost.get(i));

            double rtScore = 0;
            if (medianRT > 0) rtScore = medianRT / (medianRT + deltaRT.get(i));

            double competencyScore = 0;
            if (competencyWeight > 0) competencyScore = options.get(i).getGradeOfSkill();

            // calculate heuristic factor
            double heuristicScore =
                    ((connScore * connectednessWeight) +
                            (costScore * costWeight) +
                            (rtScore * rtWeight) +
                            (competencyScore * competencyWeight)) /
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
