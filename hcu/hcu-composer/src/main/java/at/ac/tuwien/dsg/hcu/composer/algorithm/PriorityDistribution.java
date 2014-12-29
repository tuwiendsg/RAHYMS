package at.ac.tuwien.dsg.hcu.composer.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import at.ac.tuwien.dsg.hcu.composer.model.SolutionComponent;

public class PriorityDistribution implements ComposerAlgorithmInterface {

    private static final int SEED_SELECTOR = 1001;
    private static UniformRealDistribution distSelector = new UniformRealDistribution(new MersenneTwister(SEED_SELECTOR), 0, 1);
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
            ArrayList<SolutionComponent> components = cons.getComponentList().get(i);
            // sum up priorities
            int prioritySum = 0;
            for (SolutionComponent comp: components) {
                int priority = (int)comp.getAssignee().getProvider().getProperties().getValue("assignment_priority", 1);
                prioritySum += priority;
            }
            // repeat until we have an element that has not been selected
        	int retry = 0;
        	SolutionComponent chosenComp = null;
            do {
            	double chosen = distSelector.sample() * prioritySum;
            	int pos = 0;
            	for (SolutionComponent comp: components) {
            		int priority = (int)comp.getAssignee().getProvider().getProperties().getValue("assignment_priority", 1);
            		pos += priority;
            		if (chosen <= pos) {
            			chosenComp = comp;
            			break;
            		}
            	}
            	if (chosenComp==null) {
            		System.out.println(chosen);
            		System.out.println(prioritySum);
            	}
            } while (retry++<20 && (chosenComp==null || selectedElements.contains(chosenComp.getAssignee().getProvider())));
            
            selectedElements.add(chosenComp.getAssignee().getProvider());
            solution.addSolutionComponent(chosenComp);
        }

        // measure objective values
        ArrayList<Solution> solutionList = new ArrayList<Solution>();
        solutionList.add(solution);
        composer.calculateObjectiveValues(solutionList);

        return solution;
    }

}
