package at.ac.tuwien.dsg.hcu.monitor.gridsim;

import java.util.List;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimCore;
import gridsim.GridSimTags;
import gridsim.IO_data;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class GSMonitoringAgent extends GridSimCore {

    private MonitoringAgentInterface agent;
    
    public GSMonitoringAgent(MonitoringAgentInterface agent) throws Exception {
        super(agent.getName(), 100.0);
        this.agent = agent;
    }
    
    public boolean isFinished() {
        return !agent.isRunning();
    }

    public void body() {
        
        // register to GIS
        int gisID = GridSim.getGridInfoServiceEntityId();
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GridSimTags.REGISTER_RESOURCE,
                new IO_data(new Integer(super.get_id()), 12, gisID) );
        
        Sim_event ev = new Sim_event();
        while (Sim_system.running() && !isFinished()) {
            // wait for WAKE_UP event
            super.sim_get_next(ev);
            switch (ev.get_tag()) {
                case GSMonitoringSimulation.WAKE_UP:
                    List<Data> data = (List<Data>)ev.get_data();
                    // TODO: send GS event to the consumers so that we can simulate monitoring communication
                    agent.getProducer().publish(data);
                    advance();
                    break;
            }
        }
        
    }
    
    public void startSimulation() {
        agent.start();
        advance();
    }

    private void advance() {
        if (agent.getAdapter()==null) {
            // if the agent does not have an adapter, it can't initiate a data flow. just wait till it receives data via customer
            // TODO: fully simulate consumer with GS
            return;
        }
        List<Data> data = agent.getAdapter().getData();
        Data firstData = data.get(0);
        if (agent.isRunning()) {
            // still have data, schedule to publish the data when the time comes
            double time = (double)firstData.getMetaData("time");
            double wakeTime = time - GridSim.clock();
            if (wakeTime<0) {
                //System.err.println("Invalid wakeTime " + wakeTime);
            }
            super.send(super.output, wakeTime, GSMonitoringSimulation.WAKE_UP,
                new IO_data(data, (firstData.getSize()*data.size()), get_id()) );
        } else {
            if (firstData.getMetaData("eof")!=null) {
                // publish this eof data
                agent.getProducer().publish(data);
            }
        }
    }
}
