package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public interface BrokerInterface {

    public void adjust(Map<String, Object> config);
    public void adjust(Integer subscriptionId, Map<String, Object> config);

    public void publish(Data data);
    public void publish(List<Data> data);
    public int subscribe(Subscription subscription); // returns the subscription id
    public void registerAgent(AgentInterface agent);
    public AgentInterface getAgent(String agentName);
    
}
