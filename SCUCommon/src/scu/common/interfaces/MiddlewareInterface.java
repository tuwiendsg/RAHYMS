package scu.common.interfaces;

import scu.common.model.Assignment;
import scu.common.model.Task;

public interface MiddlewareInterface {

    // methods for setting middleware component
    public MiddlewareInterface setServiceManager(
            ServiceManagerInterface serviceManager);
    public MiddlewareInterface setDiscoverer(
            DiscovererInterface discoverer);
    public MiddlewareInterface setComposerInterface(
            ComposerInterface composer);
    
    public void submitTask(Task task);
    public void finishAssigment(Assignment assignment);
    
}
