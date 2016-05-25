package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public abstract class BaseConsumer implements ConsumerInterface {

    protected AgentInterface agent;
    protected Map<String, Map<String, Object>> topics = new HashMap<String, Map<String, Object>>();
    protected Map<String, Object> config = new HashMap<String, Object>();

    @Override
    public void subscribeTo(ProducerInterface producer,
            Subscription subscription) {
        subscription.setConsumerAgent(getAgent());
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
    public void addTopic(String topicName, Map<String, Object> config) {
        topics.put(topicName, config);
    }

    @Override
    public void adjust(Map<String, Object> config) {
        this.config.putAll(config);
    }
}
