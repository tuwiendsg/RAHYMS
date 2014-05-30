package scu.common.interfaces;

import java.util.ArrayList;

import scu.common.model.Assignment;
import scu.common.model.Task;

public interface ComposerInterface {

    // methods for setting composer component
    public ComposerInterface setServiceManager(
            ServiceManagerInterface serviceManager);
    public ComposerInterface setDiscoverer(
            DiscovererInterface discoverer);

    // compose methods
    public ArrayList<Assignment> compose(Task task);
}
