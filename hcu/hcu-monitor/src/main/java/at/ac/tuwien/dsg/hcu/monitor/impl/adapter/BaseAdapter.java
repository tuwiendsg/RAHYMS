package at.ac.tuwien.dsg.hcu.monitor.impl.adapter;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;

public abstract class BaseAdapter implements AdapterInterface, Wakeable {

    protected AgentInterface agent = null;
    protected Waker waker = null;
    protected Map<String, Map<String, Object>> topics = new HashMap<String, Map<String, Object>>();
    protected Map<String, Object> config = new HashMap<String, Object>();
    

    @Override
    public void addTopic(String topicName, Map<String, Object> config) {
        topics.put(topicName, config);
    }

    @Override
    public void adjust(Map<String, Object> config) {
        this.config.putAll(config);
    }

    @Override
    public void setMonitoringAgent(AgentInterface agent) {
        this.agent = agent;
    }
    

    @Override
    public void setWaker(Waker waker) {
        this.waker = waker;
    }

}
