package at.ac.tuwien.dsg.hcu.cloud.negotiator;

import java.util.List;

import at.ac.tuwien.dsg.hcu.common.interfaces.NegotiateCallbackInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.NegotiateInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

/**
 * This class exemplify assignment negotiations.
 * We just simply agree with the proposed assignments here.
 *
 */
public class SimpleNegotiator implements NegotiateInterface {

    @Override
    public void negotiate(Task task, List<Assignment> assignments, NegotiateCallbackInterface callback) {
        task.setStatus(Task.Status.NEGOTIATING);
        System.out.println("NEGOTIATING task #" + task.getId() + ", assignments=" + assignments);
        callback.deployAssignments(task, assignments);
    }

}
