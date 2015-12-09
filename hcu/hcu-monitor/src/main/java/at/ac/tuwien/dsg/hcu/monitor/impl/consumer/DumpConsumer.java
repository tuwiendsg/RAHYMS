package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import gridsim.GridSim;

public class DumpConsumer extends BaseConsumer {

    protected AgentInterface agent;
    
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

}
