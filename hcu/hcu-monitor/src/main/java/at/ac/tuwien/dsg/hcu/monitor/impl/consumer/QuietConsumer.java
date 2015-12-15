package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class QuietConsumer extends BaseConsumer {

    protected AgentInterface agent;
    
    @Override
    public void receive(Data data) {
        // sssshhh
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
