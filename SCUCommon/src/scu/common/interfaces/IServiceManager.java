package scu.common.interfaces;

import java.util.Collection;

import scu.common.exceptions.NotFoundException;
import scu.common.model.ComputingElement;
import scu.common.model.Service;

public interface IServiceManager {
    
    // manage element
    public ComputingElement addElement();
    public ComputingElement saveElement(ComputingElement element);
    public void removeElement(ComputingElement element) throws NotFoundException;
    public ComputingElement retrieveElement(long id);
    public Collection<ComputingElement> retrieveElements();

    // manage service
    public Service saveService(Service service);
    public void removeService(Service service) throws NotFoundException;

}
