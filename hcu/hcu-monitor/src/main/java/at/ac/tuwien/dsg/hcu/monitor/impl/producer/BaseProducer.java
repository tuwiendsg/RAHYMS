package at.ac.tuwien.dsg.hcu.monitor.impl.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class BaseProducer implements ProducerInterface {

    protected AgentInterface agent;
    protected Map<String, Map<String, Object>> topics = new HashMap<String, Map<String, Object>>();
    protected Map<String, Object> config = new HashMap<String, Object>();
    
    @Override
    public void setAgent(AgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public void adjust(Map<String, Object> config) {
        this.config.putAll(config);
    }

    @Override
    public void publish(List<Data> data) {
        agent.getBroker().publish(data);
    }

    @Override
    public void publish(Data data) {
        agent.getBroker().publish(data);
    }

    @Override
    public void addTopic(String topicName, Map<String, Object> config) {
        topics.put(topicName, config);
    }

    @Override
    public AgentInterface getAgent() {
        return agent;
    }

}
