package scu.common.interfaces;

import java.util.ArrayList;

import scu.common.model.ComputingElement;
import scu.common.model.Connection;
import scu.common.model.Properties;
import scu.common.model.Task;
import scu.common.sla.Specification;

public interface ICloudManager {

    public ArrayList<ComputingElement> getResources(Specification spec);
    
    public ArrayList<Connection> getResourceRelations(
            ArrayList<ComputingElement> resource);
    
    public Object getResourceProperty(ComputingElement resource, String propertyName);
    
    public Properties getResourceProperties(ComputingElement resource);

    public int getEarliestAvailability(ComputingElement resource,
            int startTime, int duration);
    
    public boolean assignTask(ComputingElement resource, Task task, int startTime);
    
}
