package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.List;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Role;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.common.model.optimization.TaskWithOptimization;

public interface ComposerInterface {

    // compose methods
    public List<Assignment> compose(Task task, double clock);
    public List<Assignment> compose(TaskWithOptimization task, double clock);
    
    // partial compose methods 
    public List<Assignment> partialCompose(Task task, List<Role> roles, double clock);
    public List<Assignment> partialCompose(TaskWithOptimization task, List<Role> roles, double clock);
}
