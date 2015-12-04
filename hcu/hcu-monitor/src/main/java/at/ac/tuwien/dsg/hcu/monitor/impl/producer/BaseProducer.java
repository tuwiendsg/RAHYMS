package at.ac.tuwien.dsg.hcu.monitor.impl.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class BaseProducer implements ProducerInterface {

    protected Map<String, HashMap<String, Object>> topics = new HashMap<String, HashMap<String, Object>>();
    protected AgentInterface agent;
    
    @Override
    public void setAgent(AgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
    }

    @Override
    public void publish(List<Data> data) {
        agent.getBroker().publish(data);
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        topics.put(topicName, config);
    }

    @Override
    public AgentInterface getAgent() {
        return agent;
    }

}
