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
import at.ac.tuwien.dsg.hcu.util.Util;

public class Broker implements BrokerInterface, StatisticInterface, Wakeable {

    protected Map<Integer, Subscription> register;
    protected Map<String, AgentInterface> agents;
    protected Map<String, AgentInterface> topicProviders;
    protected int lastSubscriptionId = 0;
    protected Waker waker;
    protected Map<String, Object> properties;
    protected boolean producerBasedQualityEngineEnabled = false;
    protected boolean isStopping = false;

    public Broker() {
        register = new HashMap<Integer, Subscription>();
        agents = new HashMap<String, AgentInterface>();
        properties = new HashMap<String, Object>();
        topicProviders = new HashMap<String, AgentInterface>();
    }
    
    @Override
    public void adjust(Map<String, Object> config) {
        if (config.containsKey("producer_based_quality_engine_enabled")) {
            producerBasedQualityEngineEnabled = (boolean) config.getOrDefault("producer_based_quality_engine_enabled", false);
        }
    }

    @Override
    public void adjust(Integer subscriptionId, Map<String, Object> config) {
        Subscription subscription = register.get(subscriptionId);
        subscription.setConfig(config);
        register.put(subscriptionId, subscription); // not sure if it's necessary to put back
    }

    private boolean dataMatchSubscription(Data data, Subscription subscription) {
        boolean match = false;
        if (!producerBasedQualityEngineEnabled) {
            match = subscription.getTopic().trim().equalsIgnoreCase(data.getName().trim());
        } else {
            match = subscription.getQualityEmbeddedTopic().equalsIgnoreCase(data.getName().trim());
        }
        return match;
    }
    
    @Override
    public void publish(Data data) {
        // set message_received_count property
        increaseProperty("message_received_count");
        
        for (Subscription subscription: register.values() ) {
            if (dataMatchSubscription(data, subscription)) {
                if (data.getMetaData("eof")!=null) {
                    subscription.getConsumerAgent().receive(data);
                    increaseProperty("message_published_count");
                    isStopping = true;
                }
                QualityEngine qualityEngine = QualityEngine.getInstance(subscription);
                if (qualityEngine!=null && !producerBasedQualityEngineEnabled) {
                    qualityEngine.setWaker(waker);
                    qualityEngine.receive(data);
                } else {
                    subscription.getConsumerAgent().receive(data);
                    increaseProperty("message_published_count");
                }
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
        
        if (!producerBasedQualityEngineEnabled) {
            // create QualityEngine for this subscription
            new QualityEngine(subscription, producerBasedQualityEngineEnabled, this);
        } else {
            AgentInterface agent = topicProviders.get(subscription.getTopic());
            agent.getProducer().subscribe(subscription);
        }
        
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
    public void registerTopic(AgentInterface agent, String topic) {
        // only necessary when producer_based_quality_engine_enabled=true
        topicProviders.put(topic, agent);
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
    public void increaseProperty(String name) {
        Integer count = (Integer) properties.getOrDefault(name, 0);
        properties.put(name, count + 1);
    }

    @Override
    public boolean isStopping() {
        return isStopping;
    }


}
