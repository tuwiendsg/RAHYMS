package at.ac.tuwien.dsg.hcu.composer;

import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class ComposerTracer extends Tracer {

    public ComposerTracer(String file) {
        super(file);
    }

    public void traceln(Solution solution, String prefix) {
        traceln(prefix + solution.trace());
    }

    public String getTraceHeader() {
        return "flag,algo_time,task,data,task,solution_components,objective_value,cost,norm_cost,competency,connnectedness,mu_connnectedness,response_time,norm_response_time";
    }

}
