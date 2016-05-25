package at.ac.tuwien.dsg.hcu.composer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import at.ac.tuwien.dsg.hcu.composer.model.SolutionComponent;

public class FairDistribution implements ComposerAlgorithmInterface {

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
        List<ComputingElement> selectedElements = new ArrayList<ComputingElement>();
        for (int i=1; i<cons.getComponentList().size()-1; i++) {
            List<SolutionComponent> components = cons.getComponentList().get(i);
            
            // sort it first to get consistent behavior on different executions
            Collections.sort(components, new Comparator<SolutionComponent>() {
                @Override
                public int compare(SolutionComponent comp1, SolutionComponent comp2) {
                    return  comp1.getAssignee().getTitle().compareTo(comp2.getAssignee().getTitle());
                }
            });
            
            int lowestAssignmentCount = 9999;
            SolutionComponent solutionComponent = null;
            for (SolutionComponent comp: components) {
                int assignmentCount = comp.getAssignee().getProvider().getAssignmentCount();
                if (lowestAssignmentCount > assignmentCount && !selectedElements.contains(comp.getAssignee().getProvider())) {
                    lowestAssignmentCount = assignmentCount;
                    solutionComponent = comp;
                }
            }
            selectedElements.add(solutionComponent.getAssignee().getProvider());
            solution.addSolutionComponent(solutionComponent);
        }

        // measure objective values
        ArrayList<Solution> solutionList = new ArrayList<Solution>();
        solutionList.add(solution);
        composer.calculateObjectiveValues(solutionList);

        return solution;
    }

}
