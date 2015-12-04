package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public interface ProducerInterface {

    public void adjust(HashMap<String, Object> config);

    public void publish(List<Data> data);
    
    // internal methods
    public void setAgent(AgentInterface agent);
    public AgentInterface getAgent();
    public void addTopic(String topicName, HashMap<String, Object> config);
}
