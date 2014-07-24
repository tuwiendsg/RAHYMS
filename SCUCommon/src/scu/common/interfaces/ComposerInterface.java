package scu.common.interfaces;

import java.util.List;

import scu.common.model.Assignment;
import scu.common.model.Role;
import scu.common.model.Task;
import scu.common.model.optimization.TaskWithOptimization;

public interface ComposerInterface {

    // compose methods
    public List<Assignment> compose(Task task);
    public List<Assignment> compose(TaskWithOptimization task);
    
    // partial compose methods 
    public List<Assignment> partialCompose(Task task, List<Role> roles);
    public List<Assignment> partialCompose(TaskWithOptimization task, List<Role> roles);
    
}
