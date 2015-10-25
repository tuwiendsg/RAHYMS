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

    public static final int WAKE_UP = 3000;

    private MonitoringAgentInterface agent;
    private boolean isFinished = false;;
    
    public GSMonitoringAgent(MonitoringAgentInterface agent) throws Exception {
        super(agent.getName(), 100.0);
        this.agent = agent;
    }
    
    public boolean isFinished() {
        return isFinished;
    }

    public void body() {
        
        // register to GIS
        int gisID = GridSim.getGridInfoServiceEntityId();
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GridSimTags.REGISTER_RESOURCE,
                new IO_data(new Integer(super.get_id()), 12, gisID) );
        
        Sim_event ev = new Sim_event();
        while (Sim_system.running() && !isFinished) {
            // wait for WAKE_UP event
            super.sim_get_next(ev);
            switch (ev.get_tag()) {
                case GSMonitoringAgent.WAKE_UP:
                    List<Data> data = (List<Data>)ev.get_data();
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
        List<Data> data = agent.getAdapter().getData();
        Data firstData = data.get(0);
        if (firstData==null || firstData.getMetaData("eof")!=null || firstData.getMetaData("time")==null) {
            isFinished = true;
            agent.stop();
        } else {
            double time = (double)firstData.getMetaData("time");
            double wakeTime = time - GridSim.clock();
            super.send(super.output, wakeTime, GSMonitoringAgent.WAKE_UP,
                    new IO_data(data, (firstData.getSize()*data.size()), get_id()) );
        }        
    }
}
