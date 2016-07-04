package at.ac.tuwien.dsg.hcu.cloud.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import at.ac.tuwien.dsg.hcu.cloud.generator.ServiceGenerator;
import at.ac.tuwien.dsg.hcu.common.exceptions.NotFoundException;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;

public class ServiceManagerOnMemory implements
        ServiceManagerInterface {
    
    protected static Hashtable<Long, ComputingElement> elementCache = new Hashtable<Long, ComputingElement>();
    protected static ArrayList<Service> serviceCache = new ArrayList<Service>();
    protected static long lastId = -1;
    
    private ServiceManagerOnMemory _instance;

    public ServiceManagerOnMemory() {
    }

    @Override
    public ComputingElement createElement() {
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
    public ComputingElement registerElement(ComputingElement element) {
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
    public Service registerService(Service service) {
        ComputingElement element = retrieveElement(service.getProvider().getId());
        if (element!=null) {
            element.addService(service);
        } 
        registerElement(service.getProvider());
        serviceCache.add(service);
        return service;
    }

    @Override
    public void removeService(Service service) throws NotFoundException {
        ComputingElement element = retrieveElement(service.getProvider().getId());
        if (element!=null) {
            registerElement(element);
            element.removeService(service.getFunctionality());
        } 
        serviceCache.remove(service);
    }
    
    @Override
    public List<Service> retrieveServices(Functionality functionality) {
        List<Service> services = new ArrayList<Service>();
        for (Service s: serviceCache) {
            if (s.getFunctionality().equals(functionality) || functionality==null) {
                services.add(s);
            }
        }
        return services;
    }

    public void generate(ConfigJson genConfig) {
        try {
            // generate services
            ServiceGenerator svcGen = new ServiceGenerator(genConfig);
            ArrayList<Service> services;
            services = svcGen.generate();
            // save
            for (Service service : services) {
                registerService(service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServiceManagerInterface getInstance() {
        if (_instance==null) _instance = new ServiceManagerOnMemory();
        return _instance;
    }

    @Override
    public Service getServiceById(int serviceId) {
        Service found = null;
        for (Service s: serviceCache) {
            if (s.getId()==serviceId) {
                found = s;
            }
        }
        return found;
    }

}
