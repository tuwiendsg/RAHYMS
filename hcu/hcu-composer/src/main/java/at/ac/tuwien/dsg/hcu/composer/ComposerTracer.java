package at.ac.tuwien.dsg.hcu.composer;

import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class ComposerTracer extends Tracer {

    public ComposerTracer() {}

    public ComposerTracer(String file) {
        super(file);
    }

    public void traceln(Solution solution, String prefix) {
        traceln(prefix + solution.trace());
    }

    public String getTraceHeader() {
        return "clock,task_id,flag,algo_time,task_name,data,task,solution_components,objective_value,cost,norm_cost,competency,connectedness,mu_connectedness,response_time,norm_response_time";
    }
}
