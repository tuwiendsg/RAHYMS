package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public abstract class BaseConsumer implements ConsumerInterface {

    protected AgentInterface agent;
    protected Map<String, HashMap<String, Object>> topics = new HashMap<String, HashMap<String, Object>>();
    
    @Override
    public void subscribeTo(ProducerInterface producer,
            Subscription subscription) {
        subscription.setConsumer(this);
        agent.getBroker().subscribe(subscription);
    }

    @Override
    public void setAgent(AgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public AgentInterface getAgent() {
        return agent;
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        topics.put(topicName, config);
    }
}
