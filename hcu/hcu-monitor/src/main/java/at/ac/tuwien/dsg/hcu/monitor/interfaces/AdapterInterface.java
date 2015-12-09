package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public interface AdapterInterface {

    public void start();
    public void stop();
    public void adjust(Map<String, Object> config);
    
    public void setMonitoringAgent(AgentInterface agent);
    public List<Data> getData();
    public void addTopic(String topicName, Map<String, Object> config);
}
