package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public interface ProducerInterface {

    public void adjust(Map<String, Object> config);

    public void publish(List<Data> data);
    public void publish(Data data);
    
    // internal methods
    public void setAgent(AgentInterface agent);
    public AgentInterface getAgent();
    public void addTopic(String topicName, Map<String, Object> config);
}
