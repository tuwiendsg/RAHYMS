package at.ac.tuwien.dsg.hcu.monitor.impl.agent;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;

public class BaseAgent implements AgentInterface {

    protected BrokerInterface broker;
    protected AdapterInterface adapter;
    protected ProducerInterface producer;
    protected ConsumerInterface consumer;
    protected HashMap<String, Object> config;
    protected String name;
    protected boolean isRunning = false;
    
    public BaseAgent() {
    }

    public BaseAgent(String name, HashMap<String, Object> config) {
        setName(name);
        setConfig(config);
    }
    
    public BaseAgent(String name, HashMap<String, Object> config, AdapterInterface adapter, ProducerInterface producer) {
        setName(name);
        setConfig(config);
        setAdapter(adapter);
        setProducer(producer);
    }

    public BaseAgent(String name, HashMap<String, Object> config, ConsumerInterface consumer, ProducerInterface producer) {
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
    public ProducerInterface getProducer() {
        return producer;
    }
    
    @Override
    public ConsumerInterface getConsumer() {
        return consumer;
    }

    @Override
    public AdapterInterface getAdapter() {
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
    public void setProducer(ProducerInterface producer) {
        this.producer = producer;
        this.producer.setAgent(this);
        if (config!=null) {
            this.producer.adjust(config);
        }
    }

    @Override
    public void setConsumer(ConsumerInterface consumer) {
        this.consumer = consumer;
        this.consumer.setAgent(this);
        if (config!=null) {
            this.consumer.adjust(config);
        }
    }

    @Override
    public void setAdapter(AdapterInterface adapter) {
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

    @Override
    public BrokerInterface getBroker() {
        return broker;
    }

    @Override
    public void setBroker(BrokerInterface broker) {
        this.broker = broker;
    }

    
}
