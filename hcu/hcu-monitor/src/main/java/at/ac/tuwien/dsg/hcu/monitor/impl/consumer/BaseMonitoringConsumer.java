package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public abstract class BaseMonitoringConsumer implements MonitoringConsumerInterface {

    protected MonitoringAgentInterface agent;
    protected Map<String, HashMap<String, Object>> topics = new HashMap<String, HashMap<String, Object>>();
    
    @Override
    public void subscribeTo(MonitoringProducerInterface producer,
            Subscription subscription) {
        subscription.setConsumer(this);
        producer.subscribe(subscription);
    }

    @Override
    public void setAgent(MonitoringAgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public MonitoringAgentInterface getAgent() {
        return agent;
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        topics.put(topicName, config);
    }
}
