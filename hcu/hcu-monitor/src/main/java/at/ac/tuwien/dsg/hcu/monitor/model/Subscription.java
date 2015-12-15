package at.ac.tuwien.dsg.hcu.monitor.model;

import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;

public class Subscription {

    private Integer id;
    private AgentInterface consumerAgent;
    private String topic;
    private Map<String, Object> config;
    private Quality quality;
    
    private static int lastId = 0;
    
    public Subscription() {
        id = ++lastId;
    }
    
    public AgentInterface getConsumerAgent() {
        return consumerAgent;
    }
    
    public void setConsumerAgent(AgentInterface consumerAgent) {
        this.consumerAgent = consumerAgent;
    }
    
    public String getQualityEmbeddedTopic() {
        if (quality!=null) {
            return getTopic() + "/" + quality.toString();
        } else {
            return getTopic();
        }
    }

    public String getTopic() {
        return topic.trim();
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public Map<String, Object> getConfig() {
        return config;
    }
    
    public void setConfig(Map<String, Object> config) {
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

    @Override
    public String toString() {
        return "Subscription [consumerAgent=" + consumerAgent + ", topic=" + topic + ", quality=" + quality + "]";
    }
    
    
}
