package scu.common.interfaces;

import java.util.Collection;

import scu.common.exceptions.NotFoundException;
import scu.common.model.ComputingElement;

public interface IComputingElementManager {
    
    public ComputingElement addElement();
    public ComputingElement addElement(ComputingElement element);
    public ComputingElement retrieveElement(long id);
    public Collection<ComputingElement> retrieveElements();
    public void updateElement(ComputingElement element) throws NotFoundException;
    public void removeElement(ComputingElement element) throws NotFoundException;

}
