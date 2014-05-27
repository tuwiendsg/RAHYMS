package scu.composer.test;

import java.util.ArrayList;

import org.jgrapht.graph.Subgraph;

import scu.composer.Composer;
import scu.composer.ShowGraphApplet;
import scu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.scucrowd.icumanager.WorkerHandler;
import at.ac.tuwien.dsg.scucrowd.model.ConnectednessGraph;
import at.ac.tuwien.dsg.scucrowd.model.FuzzyAptitude;
import at.ac.tuwien.dsg.scucrowd.model.FuzzyCompetence;
import at.ac.tuwien.dsg.scucrowd.model.Job;
import at.ac.tuwien.dsg.scucrowd.model.JobAssignment;
import at.ac.tuwien.dsg.scucrowd.model.Objective;
import at.ac.tuwien.dsg.scucrowd.model.Quality;
import at.ac.tuwien.dsg.scucrowd.model.RelationEdge;
import at.ac.tuwien.dsg.scucrowd.model.SolutionComponent;
import at.ac.tuwien.dsg.scucrowd.model.Task;
import at.ac.tuwien.dsg.scucrowd.model.TaskRequest;
import at.ac.tuwien.dsg.scucrowd.model.Worker;
import at.ac.tuwien.dsg.scucrowd.model.WorkerRelation;

public class Tester {

  
  public static void main(String[] args) {
    //testGenerateConstructionGraph();
    //testConnectedness();
    testProvisioning();
  }

  public static void testGetWorkers() {
    
    FuzzyCompetence fc = new FuzzyCompetence();
    fc.getAptitudeSet().add(new FuzzyAptitude("1", Quality.FAIR));
    fc.getAptitudeSet().add(new FuzzyAptitude("10", Quality.FAIR));
    //ArrayList<Worker> workers = (ArrayList<Worker>)APIClient.metaCrowd().getWorkers(fc); 
    //for (Worker w: workers) {
      //System.out.println(w);
    //}
  }
  
  public static TaskRequest createBuggyTaskRequest() {
    Task t = new Task();
    t.setTitle("Buggy task");
    t.setType("T1");

    // job 1
    FuzzyCompetence competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("5", Quality.GOOD));
    competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.VERY_GOOD));
    competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.GOOD));
    competence.getAptitudeSet().add(new FuzzyAptitude("9", Quality.GOOD));
    t.getJobSet().add(new Job("J1", competence, 5));
    
    // job 2
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.GOOD));
    competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("9", Quality.GOOD));
    t.getJobSet().add(new Job("J2", competence, 5));

    // job 3
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("6", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("4", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.POOR));
    t.getJobSet().add(new Job("J3", competence, 5));
  
    TaskRequest request = new TaskRequest(t, 56, 80, Quality.VERY_GOOD, 25.37110807953035, new Objective(1, 0, 0, 0));
    
    return request;
  }
    
  public static TaskRequest createTaskRequest() {
    Task t = new Task();
    t.setTitle("First task");
    t.setType("T1");

    // job 1
    FuzzyCompetence competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("1", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("2", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("3", Quality.POOR));
    t.getJobSet().add(new Job("J1", competence, 5));

    // job 2
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("4", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("5", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.POOR));
    t.getJobSet().add(new Job("J2", competence, 5));

    // job 3
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("9", Quality.POOR));
    t.getJobSet().add(new Job("J3", competence, 8));
    
    // job 4
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("10", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("11", Quality.POOR));
    t.getJobSet().add(new Job("J4", competence, 12));

    // job 5
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("12", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("13", Quality.POOR));
    t.getJobSet().add(new Job("J5", competence, 12));

    TaskRequest request = new TaskRequest(t, 0, 40, Quality.POOR, 20, new Objective(1, 0, 0, 0));
    
    return request;
  }
    
  public static void testGenerateConstructionGraph() {

    TaskRequest request = createTaskRequest();
    ConstructionGraph cg = ConstructionGraph.generate(request);
    System.out.println(cg.getGraph().toString());

    // show the graph
    ShowGraphApplet<SolutionComponent, RelationEdge> applet = 
        new ShowGraphApplet<SolutionComponent, RelationEdge>();
    applet.showGraph(cg.getGraph());

  }
  
  public static void testConnectedness() {

    // test getWorkerRelations
    ArrayList<Worker> workers = new ArrayList<Worker>();
    workers.add(new Worker(1));
    workers.add(new Worker(2));
    workers.add(new Worker(3));
    workers.add(new Worker(4));
    workers.add(new Worker(5));
    workers.add(new Worker(6));
    workers.add(new Worker(7));
    workers.add(new Worker(8));
    workers.add(new Worker(9));
    workers.add(new Worker(10));
    //ArrayList<WorkerRelation> relations = (ArrayList<WorkerRelation>)APIClient.metaCrowd().getWorkerRelations(workers);
    ArrayList<WorkerRelation> relations = WorkerHandler.getWorkerRelations(workers);
    for (WorkerRelation r: relations) {
      System.out.println(r);
    }

    // generate
    ConnectednessGraph cg = ConnectednessGraph.generate(relations);
    // subgraph
    ArrayList<Worker> subworkers = new ArrayList<Worker>();
    subworkers.add(new Worker(1));
    subworkers.add(new Worker(2));
    subworkers.add(new Worker(3));
    Subgraph<Worker, RelationEdge, ConnectednessGraph> subgraph = cg.getSubgraphFromWorker(subworkers);
    
    // show getWorkerRelations in applet
    ShowGraphApplet<Worker, RelationEdge> applet = new ShowGraphApplet<Worker, RelationEdge>();
    applet.showGraph(cg);

    // show
    System.out.println(subgraph);
    //System.out.println(cg.getConnectedness());
    //System.out.println(subgraph.getBase().getConnectedness(subgraph));
    
  }
  
  public static void testProvisioning() {
    //TaskRequest request = createTaskRequest();
    TaskRequest request = createBuggyTaskRequest();
    Composer.compose(request);
  }
  
}
