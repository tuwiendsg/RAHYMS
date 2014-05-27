package scu.composer.test;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

import scu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.scucrowd.model.FuzzyAptitude;
import at.ac.tuwien.dsg.scucrowd.model.FuzzyCompetence;
import at.ac.tuwien.dsg.scucrowd.model.Job;
import at.ac.tuwien.dsg.scucrowd.model.Objective;
import at.ac.tuwien.dsg.scucrowd.model.Quality;
import at.ac.tuwien.dsg.scucrowd.model.RelationEdge;
import at.ac.tuwien.dsg.scucrowd.model.SolutionComponent;
import at.ac.tuwien.dsg.scucrowd.model.Task;
import at.ac.tuwien.dsg.scucrowd.model.TaskRequest;

public class OrigApplet extends JApplet {

  private static final long serialVersionUID = 3877521306902446441L;
  private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
  private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    OrigApplet applet = new OrigApplet();
    applet.init();
    
    JFrame frame = new JFrame();
    frame.getContentPane().add(applet);
    frame.setTitle("xxxx JGraphT Adapter to JGraph Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    //frame.setVisible(true);
  }
  
  

  public void init() {
    
    ListenableDirectedWeightedGraph<SolutionComponent, RelationEdge> graph = generateGraph();
    JGraphModelAdapter<SolutionComponent, RelationEdge> jgAdapter = new JGraphModelAdapter<SolutionComponent, RelationEdge>(graph);
    JGraph jgraph = new JGraph(jgAdapter);
    
    adjustDisplaySettings(jgraph);
    getContentPane().add(jgraph);
    resize(DEFAULT_SIZE);
    
  }
  
  public static ListenableDirectedWeightedGraph<SolutionComponent, RelationEdge> generateGraph() {
    
    Task t = new Task();
    t.setTitle("First task");
    t.setType("T1");

    // job 1
    FuzzyCompetence competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("1", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("2", Quality.FAIR));
    t.getJobSet().add(new Job(competence));

    // job 2
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("2", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("3", Quality.FAIR));
    t.getJobSet().add(new Job("J1", competence));
    
    // job 3
    competence = new FuzzyCompetence();
    competence.getAptitudeSet().add(new FuzzyAptitude("4", Quality.POOR));
    competence.getAptitudeSet().add(new FuzzyAptitude("5", Quality.FAIR));
    t.getJobSet().add(new Job("J1", competence));
    
    TaskRequest request = new TaskRequest(t, 0, 100, Quality.FAIR, 10, new Objective(1, 1, 1, 1));
    ConstructionGraph cg = ConstructionGraph.generate(request);
    System.out.println(cg.getGraph().toString());
    
    return cg.getGraph();
  }

  private void adjustDisplaySettings(JGraph jg)  {
    jg.setPreferredSize(DEFAULT_SIZE);

    Color c = DEFAULT_BG_COLOR;
    String colorStr = null;

    try {
      colorStr = getParameter("bgcolor");
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (colorStr != null) {
      c = Color.decode(colorStr);
    }

    jg.setBackground(c);
  }

}
