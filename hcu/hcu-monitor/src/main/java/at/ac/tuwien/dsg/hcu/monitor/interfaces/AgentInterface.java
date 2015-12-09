package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;


public interface AgentInterface {

    public void start();
    public void stop();
    public void adjust(Map<String, Object> config);

    public void publish(List<Data> data);
    public void publish(Data data);
    public void receive(Data data);
    
    // internal methods
    public BrokerInterface getBroker();
    public ProducerInterface getProducer();
    public ConsumerInterface getConsumer();
    public AdapterInterface getAdapter();
    public void setBroker(BrokerInterface broker);
    public void setProducer(ProducerInterface producer);
    public void setConsumer(ConsumerInterface consumer);
    public void setAdapter(AdapterInterface adapter);
    public String getName();
    public void setName(String name);
    public void addTopic(String topicName, Map<String, Object> config);
    public boolean isRunning();
    
}
