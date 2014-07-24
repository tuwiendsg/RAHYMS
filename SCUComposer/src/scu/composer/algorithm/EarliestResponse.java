package scu.composer.algorithm;

import java.util.ArrayList;
import java.util.List;

import scu.common.model.Assignment;
import scu.composer.Composer;
import scu.composer.model.ConstructionGraph;
import scu.composer.model.Solution;
import scu.composer.model.SolutionComponent;

public class EarliestResponse implements ComposerAlgorithmInterface {

    private ConstructionGraph cons;
    private Composer composer;

    @Override
    public void init(String configFile, ConstructionGraph cons, Composer composer) {
        this.cons = cons;
        this.composer = composer;

    }

    @Override
    public Solution solve() {
        Solution solution = new Solution();
        for (int i=1; i<cons.getComponentList().size()-1; i++) {
            ArrayList<SolutionComponent> components = cons.getComponentList().get(i);
            double earliestRT = 9999;
            SolutionComponent earliestSolutionComponent = null;
            for (SolutionComponent comp: components) {
                double forecastedResponseTime = comp.getForecastedResponseTime(solution, composer.getDp());
                if (earliestRT > forecastedResponseTime && !solution.contains(comp.getAssignee().getProvider())) {
                    earliestRT = forecastedResponseTime;
                    earliestSolutionComponent = comp;
                }
            }
            solution.addSolutionComponent(earliestSolutionComponent);
        }

        // measure objective values
        ArrayList<Solution> solutionList = new ArrayList<Solution>();
        solutionList.add(solution);
        composer.calculateObjectiveValues(solutionList);

        return solution;
    }

}
