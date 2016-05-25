package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public interface ProducerInterface {

    public void adjust(Map<String, Object> config);

    public void publish(List<Data> data);
    public void publish(Data data);
    public int subscribe(Subscription subscription); // returns the subscription id
    
    // internal methods
    public void setAgent(AgentInterface agent);
    public AgentInterface getAgent();
    public void addTopic(String topicName, Map<String, Object> config);
}
