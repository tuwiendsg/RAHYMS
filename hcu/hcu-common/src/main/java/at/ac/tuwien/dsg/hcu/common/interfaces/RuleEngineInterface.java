package at.ac.tuwien.dsg.hcu.common.interfaces;

public interface RuleEngineInterface {

    public void removeFact(Object obj);
    public void insertFact(Object obj);
    public void terminate();
    
}
