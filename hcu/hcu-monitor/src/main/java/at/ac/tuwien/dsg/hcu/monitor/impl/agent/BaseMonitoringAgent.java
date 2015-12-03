package at.ac.tuwien.dsg.hcu.monitor.impl.agent;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;

public class BaseMonitoringAgent implements MonitoringAgentInterface {

    protected MonitoringAdapterInterface adapter;
    protected MonitoringProducerInterface producer;
    protected MonitoringConsumerInterface consumer;
    protected HashMap<String, Object> config;
    protected String name;
    protected boolean isRunning = false;
    
    public BaseMonitoringAgent() {
    }

    public BaseMonitoringAgent(String name, HashMap<String, Object> config) {
        setName(name);
        setConfig(config);
    }
    
    public BaseMonitoringAgent(String name, HashMap<String, Object> config, MonitoringAdapterInterface adapter, MonitoringProducerInterface producer) {
        setName(name);
        setConfig(config);
        setAdapter(adapter);
        setProducer(producer);
    }

    public BaseMonitoringAgent(String name, HashMap<String, Object> config, MonitoringConsumerInterface consumer, MonitoringProducerInterface producer) {
        setName(name);
        setConfig(config);
        setConsumer(consumer);
        setProducer(producer);
    }

    @Override
    public void start() {
        if (adapter!=null) {
            adapter.start();
        }
        if (consumer!=null) {
            consumer.start();
        }
        isRunning = true;
    }

    public boolean isConsumer() {
        return consumer!=null;
    }

    @Override
    public void stop() {
        if (adapter!=null) {
            adapter.stop();
        }
        if (consumer!=null) {
            consumer.stop();
        }
        isRunning = false;
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
        setConfig(config);
        if (adapter!=null) adapter.adjust(config);
        if (consumer!=null) consumer.adjust(config);
        if (producer!=null) producer.adjust(config);
    }

    @Override
    public void adjust(Integer subscriptionId, HashMap<String, Object> config) {
        if (producer!=null) producer.adjust(subscriptionId, config);
    }

    @Override
    public MonitoringProducerInterface getProducer() {
        return producer;
    }
    
    @Override
    public MonitoringConsumerInterface getConsumer() {
        return consumer;
    }

    @Override
    public MonitoringAdapterInterface getAdapter() {
        return adapter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getConfig() {
        return config;
    }

    public void setConfig(HashMap<String, Object> config) {
        if (this.config!=null) {
            this.config.putAll(config);
        } else {
            this.config = config;
        }
    }

    @Override
    public void setProducer(MonitoringProducerInterface producer) {
        this.producer = producer;
        this.producer.setAgent(this);
        if (config!=null) {
            this.producer.adjust(config);
        }
    }

    @Override
    public void setConsumer(MonitoringConsumerInterface consumer) {
        this.consumer = consumer;
        this.consumer.setAgent(this);
        if (config!=null) {
            this.consumer.adjust(config);
        }
    }

    @Override
    public void setAdapter(MonitoringAdapterInterface adapter) {
        this.adapter = adapter;
        this.adapter.setMonitoringAgent(this);
        if (config!=null) {
            this.adapter.adjust(config);
        }
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        if (producer!=null) {
            producer.addTopic(topicName, config);
        }
        if (adapter!=null) {
            adapter.addTopic(topicName, config);
        }
        if (consumer!=null) {
            consumer.addTopic(topicName, config);
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    
}
