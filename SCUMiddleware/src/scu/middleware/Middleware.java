package scu.middleware;

import java.util.ArrayList;
import java.util.Hashtable;

import scu.common.interfaces.ComposerInterface;
import scu.common.interfaces.DiscovererInterface;
import scu.common.interfaces.MiddlewareInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.Assignment;
import scu.common.model.Task;

public class Middleware implements MiddlewareInterface {

    private ServiceManagerInterface serviceManager;
    private DiscovererInterface discoverer;
    private ComposerInterface composer;
    
    private Hashtable<Long, Task> tasks;
    private Hashtable<Long, ArrayList<Assignment>> assignments;
    
    public Middleware() {
        tasks = new Hashtable<Long, Task>();
        assignments = new Hashtable<Long, ArrayList<Assignment>>();
    }
    
    @Override
    public MiddlewareInterface setServiceManager(
            ServiceManagerInterface serviceManager) {
        this.serviceManager = serviceManager;
        return this;
    }

    @Override
    public MiddlewareInterface setDiscoverer(DiscovererInterface discoverer) {
        this.discoverer = discoverer;
        return this;
    }

    @Override
    public MiddlewareInterface setComposerInterface(ComposerInterface composer) {
        this.composer = composer;
        return this;
    }

    @Override
    public void submitTask(Task task) {
        // TODO handle subtask

    }

    @Override
    public void finishAssigment(Assignment assignment) {
        // TODO Auto-generated method stub

    }

}
