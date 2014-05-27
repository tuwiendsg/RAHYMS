package scu.cloud.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import scu.cloud.generator.ServiceGenerator;
import scu.common.exceptions.NotFoundException;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.ComputingElement;
import scu.common.model.Connection;
import scu.common.model.Functionality;
import scu.common.model.Service;
import scu.util.ConfigJson;

public class ServiceManagerOnMemory implements
        ServiceManagerInterface {
    
    protected Hashtable<Long, ComputingElement> elementCache;
    protected LinkedList<Service> serviceCache;
    protected long lastId;
    
    private ServiceManagerOnMemory _instance;

    public ServiceManagerOnMemory() {
        this.elementCache = new Hashtable<Long, ComputingElement>();
        this.serviceCache  = new LinkedList<Service>();
        this.lastId = -1;
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
    public Collection<Service> retrieveServices(Functionality functionality) {
        LinkedList<Service> services = new LinkedList<Service>();
        for (Service s: serviceCache) {
            if (s.getFunctionality().equals(functionality)) {
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
    public Collection<Connection> getConnections(ArrayList<Service> services) {
        
        ArrayList<Connection> connections = new ArrayList<Connection>();

        // get a list of computing elements serving the services
        ArrayList<ComputingElement> elements = new ArrayList<ComputingElement>();
        ArrayList<Long> added = new ArrayList<Long>();
        for (Service s: services) {
            if (added.indexOf(s.getProvider().getId())==-1) {
                elements.add(s.getProvider());
                added.add(s.getProvider().getId());
            }
        }
        
        // special case for single worker
        if (elements.size()==1) {
          connections.add(new Connection(elements.get(0)));
        }
          
        // iterate to get the connections, assuming that the connection is undirectional
        for (int i=0; i<elements.size(); i++) {
            ComputingElement e1 = elements.get(i);
            for (int j=i+1; j<elements.size(); j++) {
                ComputingElement e2 = elements.get(j);
                Connection c = e1.getConnection(e2);
                if (c!=null) connections.add(c);
          }
        }
        return connections;
    }

    @Override
    public ServiceManagerInterface getInstance() {
        if (_instance==null) _instance = new ServiceManagerOnMemory();
        return _instance;
    }

}
