package scu.common.interfaces;

import java.util.Collection;
import java.util.List;

import scu.common.exceptions.NotFoundException;
import scu.common.model.ComputingElement;
import scu.common.model.Connection;
import scu.common.model.Functionality;
import scu.common.model.Service;

public interface ServiceManagerInterface {
    
    // instantiator
    public ServiceManagerInterface getInstance();
    
    // manage element
    public ComputingElement createElement();
    public ComputingElement registerElement(ComputingElement element);
    public void removeElement(ComputingElement element) throws NotFoundException;
    public ComputingElement retrieveElement(long id);
    public Collection<ComputingElement> retrieveElements();

    // manage service
    public Service registerService(Service service);
    public void removeService(Service service) throws NotFoundException;
    public List<Service> retrieveServices(Functionality functionality);
    
    // manage relations
    public List<Connection> getConnections(List<Service> services);

}
