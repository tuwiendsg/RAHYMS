package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;

public class Subscription {

    private Integer id;
    private MonitoringConsumerInterface consumer;
    private String topic;
    private HashMap<String, Object> config;
    private Quality quality;
    
    private static int lastId = 0;
    
    public Subscription() {
        id = ++lastId;
    }
    
    public MonitoringConsumerInterface getConsumer() {
        return consumer;
    }
    
    public void setConsumer(MonitoringConsumerInterface consumer) {
        this.consumer = consumer;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public HashMap<String, Object> getConfig() {
        return config;
    }
    
    public void setConfig(HashMap<String, Object> config) {
        if (this.config==null) {
            this.config = config;
        } else {
            // merge config
            this.config.putAll(config);
        }
            
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }
    
}
