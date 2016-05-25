package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import gridsim.GridSim;

public class DumpConsumer extends BaseConsumer {

    @Override
    public void receive(Data data) {
        System.out.println("[Dumper][" + agent.getName() + "]" + GridSim.clock() + ": " + data);
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
