package scu.cloud.manager;

import java.util.Collection;
import java.util.Hashtable;

import scu.common.exceptions.NotFoundException;
import scu.common.interfaces.IComputingElementManager;
import scu.common.model.ComputingElement;


public class ComputingElementManagerOnMemory implements
        IComputingElementManager {
    
    protected Hashtable<Long, ComputingElement> elementCache;
    protected long lastId;

    public ComputingElementManagerOnMemory() {
        this.elementCache = new Hashtable<Long, ComputingElement>();
        this.lastId = -1;
    }

    @Override
    public ComputingElement addElement() {
        ComputingElement element = new ComputingElement(++lastId);
        element.setManager(this);
        elementCache.put(element.getId(), element);
        return element;
    }

    @Override
    public ComputingElement addElement(ComputingElement element) {
        element.setManager(this);
        elementCache.put(element.getId(), element);
        return element;
    }

    @Override
    public ComputingElement retrieveElement(long id) {
        return elementCache.get(id);
    }

    @Override
    public void updateElement(ComputingElement element) throws NotFoundException {
        if (elementCache.get(element.getId())==null) {
            throw new NotFoundException();
        }
        elementCache.put(element.getId(), element);
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

}
