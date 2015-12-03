package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;


public interface MonitoringAgentInterface {

    public void start();
    public void stop();
    public void adjust(HashMap<String, Object> config);
    public void adjust(Integer subscriptionId, HashMap<String, Object> config);

    // internal methods
    public MonitoringProducerInterface getProducer();
    public MonitoringConsumerInterface getConsumer();
    public MonitoringAdapterInterface getAdapter();
    public void setProducer(MonitoringProducerInterface producer);
    public void setConsumer(MonitoringConsumerInterface consumer);
    public void setAdapter(MonitoringAdapterInterface adapter);
    public String getName();
    public void setName(String name);
    public void addTopic(String topicName, HashMap<String, Object> config);
    public boolean isRunning();
    
}
