package scu.composer.algorithm.aco;

import java.util.ArrayList;
import java.util.Hashtable;

import scu.composer.Composer;
import scu.composer.model.ConstructionGraph;
import scu.composer.model.Solution;

public interface ACOVariantInterface {

  public void init(String configFile, ConstructionGraph cons, Composer composer);
  public Solution findOneSolution();
  void updatePheromene(ArrayList<Solution> solutions, 
          Hashtable<Solution, Double> objectiveValues);
  public Solution getCurrentBestSolution();
  public void setBestSolution(Solution best);
  
}
