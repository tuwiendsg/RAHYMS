package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;


public interface AgentInterface {

    public void start();
    public void stop();
    public void adjust(HashMap<String, Object> config);
    public void adjust(Integer subscriptionId, HashMap<String, Object> config);

    // internal methods
    public ProducerInterface getProducer();
    public ConsumerInterface getConsumer();
    public AdapterInterface getAdapter();
    public void setProducer(ProducerInterface producer);
    public void setConsumer(ConsumerInterface consumer);
    public void setAdapter(AdapterInterface adapter);
    public String getName();
    public void setName(String name);
    public void addTopic(String topicName, HashMap<String, Object> config);
    public boolean isRunning();
    
}
