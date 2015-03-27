package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.List;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Role;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface ComposerInterface {

    // compose methods
    public List<Assignment> compose(Task task, double clock);
    
    // partial compose methods 
    public List<Assignment> partialCompose(Task task, List<Role> roles, double clock);
}
