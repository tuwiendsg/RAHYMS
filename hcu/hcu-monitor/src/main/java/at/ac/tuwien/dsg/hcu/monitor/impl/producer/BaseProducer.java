package at.ac.tuwien.dsg.hcu.monitor.impl.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.QualityEngine;
import at.ac.tuwien.dsg.hcu.monitor.gridsim.GSWaker;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.StatisticInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class BaseProducer implements ProducerInterface, Wakeable {

    protected AgentInterface agent;
    protected Map<String, Map<String, Object>> topics;
    protected Map<String, Object> config;
    protected Map<Integer, Subscription> register;
    protected int lastSubscriptionId = 0;
    protected boolean producerBasedQualityEngineEnabled = false;
    protected Waker waker;
    
    public BaseProducer() {
        topics = new HashMap<String, Map<String, Object>>();
        config = new HashMap<String, Object>();
        register = new HashMap<Integer, Subscription>();
    }
    
    @Override
    public void setAgent(AgentInterface agent) {
        this.agent = agent;
    }

    @Override
    public void adjust(Map<String, Object> config) {
        this.config.putAll(config);
        if (config.containsKey("producer_based_quality_engine_enabled")) {
            producerBasedQualityEngineEnabled = (boolean) config.getOrDefault("producer_based_quality_engine_enabled", false);
        }
    }

    @Override
    public void publish(List<Data> data) {
        for (Data adata: data) {
            publish(adata);
        }
    }

    @Override
    public void publish(Data data) {
        //System.out.println("BaseProducer.publish " + data);
        if (!producerBasedQualityEngineEnabled) {
            agent.getBroker().publish(data);
            ((StatisticInterface)agent).increaseProperty("message_published_count");
        } else {
            for (Subscription subscription: register.values() ) {
                if (subscription.getTopic().trim().equalsIgnoreCase(data.getName().trim())) {
                    if (data.getMetaData("eof")!=null) {
                        agent.getBroker().publish(data);
                        ((StatisticInterface)agent).increaseProperty("message_published_count");
                    }
                    Data adata = null;
                    try {
                        adata = (Data) data.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    adata.setName(subscription.getQualityEmbeddedTopic());
                    QualityEngine qualityEngine = QualityEngine.getInstance(subscription);
                    if (qualityEngine!=null) {
                        qualityEngine.setWaker(GSWaker.getInstance());
                        qualityEngine.receive(adata);
                    } else {
                        agent.getBroker().publish(adata);
                        ((StatisticInterface)agent).increaseProperty("message_published_count");
                    }
                }
            }
        }
    }

    @Override
    public void addTopic(String topicName, Map<String, Object> config) {
        topics.put(topicName, config);
    }

    @Override
    public AgentInterface getAgent() {
        return agent;
    }

    @Override
    public int subscribe(Subscription subscription) {

        // put to register
        int id = lastSubscriptionId++;
        register.put(id, subscription);
        
        // create QualityEngine for this subscription
        new QualityEngine(subscription, producerBasedQualityEngineEnabled, (StatisticInterface) agent);
        
        return id;
    }

    @Override
    public void wake(int wakeId) {
        // Do nothing, we implement Wakeable only for the sake of QualityEngine when producerBasedQualityEngineEnabled
    }

    @Override
    public void setWaker(Waker waker) {
        this.waker = waker;
    }
}
