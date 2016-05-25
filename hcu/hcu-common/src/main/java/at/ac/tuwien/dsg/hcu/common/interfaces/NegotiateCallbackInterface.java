package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.List;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface NegotiateCallbackInterface {

    public void deployAssignments(Task task, List<Assignment> assignments);
    public void requeueTask(Task task);

}
