package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public interface MonitoringConsumerInterface {

    public void start();
    public void stop();
    public void adjust(HashMap<String, Object> config);

    public void subscribeTo(MonitoringProducerInterface producer, Subscription subscription);
    public void receive(Data data);
    
    // internal methods
    public void setAgent(MonitoringAgentInterface agent);
    public MonitoringAgentInterface getAgent();
    public void addTopic(String topicName, HashMap<String, Object> config);

}
