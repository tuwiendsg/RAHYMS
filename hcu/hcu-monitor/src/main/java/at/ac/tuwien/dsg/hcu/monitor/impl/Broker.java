package at.ac.tuwien.dsg.hcu.monitor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.QualityEngine;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.StatisticInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class Broker implements BrokerInterface, StatisticInterface, Wakeable {

    protected Map<Integer, Subscription> register;
    protected Map<String, AgentInterface> agents;
    protected int lastSubscriptionId = 0;
    protected Waker waker;
    protected Map<String, Object> properties;

    public Broker() {
        register = new HashMap<Integer, Subscription>();
        agents = new HashMap<String, AgentInterface>();
        properties = new HashMap<String, Object>();
    }
    
    @Override
    public void adjust(Map<String, Object> config) {
    }

    @Override
    public void adjust(Integer subscriptionId, Map<String, Object> config) {
        Subscription subscription = register.get(subscriptionId);
        subscription.setConfig(config);
        register.put(subscriptionId, subscription); // not sure if it's necessary to put back
    }

    @Override
    public void publish(Data data) {
        // set message_received_count property
        Integer count = (Integer) properties.getOrDefault("message_received_count", 0);
        properties.put("message_received_count", count + 1);

        for (Subscription subscription: register.values() ) {
            if (subscription.getTopic().trim().equalsIgnoreCase(data.getName().trim())) {
                if (data.getMetaData("eof")!=null) {
                    subscription.getConsumerAgent().receive(data);
                }
                QualityEngine qualityEngine = QualityEngine.getInstance(subscription);
                if (qualityEngine!=null) {
                    qualityEngine.setWaker(waker);
                    qualityEngine.receive(data);
                } else {
                    subscription.getConsumerAgent().receive(data);
                }
                // set message_published_count property
                Integer count2 = (Integer) properties.getOrDefault("message_published_count", 0);
                properties.put("message_published_count", count2 + 1);
            }
        }
    }

    @Override
    public void publish(List<Data> data) {
        for (Data adata: data) {
            publish(adata);
        }
    }

    @Override
    public int subscribe(Subscription subscription) {

        // put to register
        int id = lastSubscriptionId++;
        register.put(id, subscription);
        
        // create QualityEngine for this subscription
        new QualityEngine(subscription);
        
        return id;
    }

    @Override
    public void wake(int wakeId) {
    }

    @Override
    public void setWaker(Waker waker) {
        this.waker = waker;
    }

    @Override
    public void registerAgent(AgentInterface agent) {
        agents.put(agent.getName(), agent);
    }

    @Override
    public AgentInterface getAgent(String agentName) {
        return agents.get(agentName);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public void resetProperty(String name) {
        properties.remove(name);
    }


}
