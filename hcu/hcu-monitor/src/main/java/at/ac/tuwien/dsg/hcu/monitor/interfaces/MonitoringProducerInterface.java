package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public interface MonitoringProducerInterface {

    public int subscribe(Subscription subscription); // returns the subscription id
    public void adjust(HashMap<String, Object> config);
    public void adjust(Integer subscriptionId, HashMap<String, Object> config);

    public void publish(Data data);
    public void publish(List<Data> data);
    
    // internal methods
    public void setMonitoringAgent(MonitoringAgentInterface agent);
    public void addTopic(String topicName, HashMap<String, Object> config);
}