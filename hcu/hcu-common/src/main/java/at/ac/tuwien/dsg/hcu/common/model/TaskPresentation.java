package at.ac.tuwien.dsg.hcu.common.model;

public class TaskPresentation implements Cloneable {

    // TODO: define details

    @Override
    protected Object clone() {
        // TODO: deep cloning
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return obj;
    }    
}
