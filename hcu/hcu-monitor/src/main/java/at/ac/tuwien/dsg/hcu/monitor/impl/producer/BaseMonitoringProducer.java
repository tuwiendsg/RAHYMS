package at.ac.tuwien.dsg.hcu.monitor.impl.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class BaseMonitoringProducer implements MonitoringProducerInterface {

    protected Map<Integer, Subscription> register;
    protected Map<String, Object> topics = new HashMap<String, Object>();
    protected int lastId = 0;
    protected MonitoringAgentInterface agent;
    
    public BaseMonitoringProducer() {
        register = new HashMap<Integer, Subscription>();
    }

    @Override
    public void publish(Data data) {
        for (Subscription subscription: register.values() ) {
            if (subscription.getTopic().trim().equalsIgnoreCase(data.getName().trim())) {
                subscription.getConsumer().receive(data);
            }
        }
    }

    @Override
    public void adjust(Integer subscriptionId, HashMap<String, Object> config) {
        Subscription subscription = register.get(subscriptionId);
        subscription.setConfig(config);
        register.put(subscriptionId, subscription); // not sure if it's necessary to put back
    }

    @Override
    public int subscribe(Subscription subscription) {
        if (topics.get(subscription.getTopic())==null) {
            // unknown topic, reject
            return 0;
        }
        int id = lastId++;
        register.put(id, subscription);
        
        // adjust agent config, necessary?
        if (subscription.getConfig()!=null) {
            agent.getAdapter().adjust(subscription.getConfig());
        }
        
        return id;
    }

    @Override
    public void setMonitoringAgent(MonitoringAgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
    }

    @Override
    public void publish(List<Data> data) {
        for (Data adata: data) {
            publish(adata);
        }
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        topics.put(topicName, config);
    }

}
