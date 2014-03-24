package scu.cloud.manager;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import scu.common.exceptions.NotFoundException;
import scu.common.interfaces.IServiceManager;
import scu.common.model.ComputingElement;
import scu.common.model.Service;


public class ServiceManagerOnMemory implements
        IServiceManager {
    
    protected Hashtable<Long, ComputingElement> elementCache;
    protected LinkedList<Service> serviceCache;
    protected long lastId;

    public ServiceManagerOnMemory() {
        this.elementCache = new Hashtable<Long, ComputingElement>();
        this.lastId = -1;
    }

    @Override
    public ComputingElement addElement() {
        // find last id or next empty id
        ComputingElement cur;
        do {
            cur = elementCache.get(++lastId);
        } while (cur!=null);
        // create and add element
        ComputingElement element = new ComputingElement(lastId);
        element.setManager(this);
        elementCache.put(element.getId(), element);
        return element;
    }

    @Override
    public ComputingElement saveElement(ComputingElement element) {
        element.setManager(this);
        elementCache.put(element.getId(), element);
        return element;
    }

    @Override
    public ComputingElement retrieveElement(long id) {
        return elementCache.get(id);
    }

    @Override
    public void removeElement(ComputingElement element) throws NotFoundException {
        if (elementCache.get(element.getId())==null) {
            throw new NotFoundException();
        }
        elementCache.remove(element).getId();
    }

    @Override
    public Collection<ComputingElement> retrieveElements() {
        return elementCache.values();
    }

    @Override
    public Service saveService(Service service) {
        ComputingElement element = retrieveElement(service.getProvider().getId());
        if (element!=null) {
            element.addService(service);
        } 
        saveElement(service.getProvider());
        return service;
    }

    @Override
    public void removeService(Service service) throws NotFoundException {
        ComputingElement element = retrieveElement(service.getProvider().getId());
        if (element!=null) {
            saveElement(element);
            element.removeService(service.getFunctionality());
        } 
    }

}
