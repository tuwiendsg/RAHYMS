package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import gridsim.GridSim;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class DumpMonitoringConsumer extends BaseMonitoringConsumer {

    protected MonitoringAgentInterface agent;
    
    @Override
    public void receive(Data data) {
        System.out.println("[Dumper] " + GridSim.clock() + ": " + data);
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        // TODO Auto-generated method stub
        
    }

}
