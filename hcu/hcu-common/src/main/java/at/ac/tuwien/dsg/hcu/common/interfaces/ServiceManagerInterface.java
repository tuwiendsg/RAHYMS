package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.Collection;
import java.util.List;

import at.ac.tuwien.dsg.hcu.common.exceptions.NotFoundException;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.Service;

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
    public Service getServiceById(int serviceId);
    
}
