package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public interface MonitoringAdapterInterface {

    public void start();
    public void stop();
    public void adjust(HashMap<String, Object> config);
    
    public void setMonitoringAgent(MonitoringAgentInterface agent);
    public List<Data> getData();
    public void addTopic(String topicName, HashMap<String, Object> config);
}