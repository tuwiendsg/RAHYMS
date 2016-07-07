package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Task;

public interface NegotiateInterface {

    public void negotiate(Task task, List<Assignment> assignments, NegotiateCallbackInterface callback);
    
    public void setConfiguration(Map<String, Object> config);
}
