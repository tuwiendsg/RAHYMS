package at.ac.tuwien.dsg.hcu.monitor.impl.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.StatisticInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class BaseAgent implements AgentInterface, StatisticInterface, Wakeable {

    protected BrokerInterface broker;
    protected AdapterInterface adapter;
    protected ProducerInterface producer;
    protected ConsumerInterface consumer;
    protected Map<String, Object> config;
    protected String name;
    protected boolean isRunning = false;
    protected Map<String, Object> properties;
    
    public BaseAgent() {
        properties = new HashMap<String, Object>();
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
    public void adjust(Map<String, Object> config) {
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

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config2) {
        if (this.config!=null) {
            this.config.putAll(config2);
        } else {
            this.config = config2;
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
    public void addTopic(String topicName, Map<String, Object> config) {
        if (producer!=null) {
            producer.addTopic(topicName, config);
        }
        if (adapter!=null) {
            adapter.addTopic(topicName, config);
        }
        if (consumer!=null) {
            consumer.addTopic(topicName, config);
        }
        broker.registerTopic(this, topicName);
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
        broker.registerAgent(this);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public void wake(int wakeId) {
        if (getAdapter() instanceof Wakeable) {
            ((Wakeable) getAdapter()).wake(wakeId);
        }
        if (getConsumer() instanceof Wakeable) {
            ((Wakeable) getConsumer()).wake(wakeId);
        }
        if (getProducer() instanceof Wakeable) {
            ((Wakeable) getProducer()).wake(wakeId);
        }
    }

    @Override
    public void setWaker(Waker waker) {
        if (getAdapter() instanceof Wakeable) {
            ((Wakeable) getAdapter()).setWaker(waker);
        }
        if (getConsumer() instanceof Wakeable) {
            ((Wakeable) getConsumer()).setWaker(waker);
        }
        if (getProducer() instanceof Wakeable) {
            ((Wakeable) getProducer()).setWaker(waker);
        }
    }

    @Override
    public void publish(List<Data> data) {
        if (getProducer()!=null) {
            getProducer().publish(data);
        }
    }

    @Override
    public void publish(Data data) {
        if (getProducer()!=null) {
            getProducer().publish(data);
            
        }
    }

    @Override
    public void receive(Data data) {
        if (getConsumer()!=null) {
            getConsumer().receive(data);
            increaseProperty("message_received_count");
        }
    }

    @Override
    public void increaseProperty(String name) {
        Integer count = (Integer) properties.getOrDefault(name, 0);
        properties.put(name, count + 1);
    }

    @Override
    public String toString() {
        return name;
    }
    
    
}
