package at.ac.tuwien.dsg.hcu.monitor.gridsim;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimCore;
import gridsim.GridSimTags;
import gridsim.IO_data;

public class GSMonitoringBroker extends GridSimCore {

    protected BrokerInterface broker;
    protected boolean finished = false;
    protected static int lastBrokerId = 0;
    
    public GSMonitoringBroker(BrokerInterface broker) throws Exception {
        this(broker, "GSMonitoringBroker" + (++lastBrokerId));
    }

    public GSMonitoringBroker(BrokerInterface broker, String name) throws Exception {
        super(name, 100.0);
        this.broker = broker;
    }

    public boolean isFinished() {
        return finished;
    }
    
    public void finish() {
        finished = true;
    }

    public void body() {
        
        // register to GIS
        int gisID = GridSim.getGridInfoServiceEntityId();
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GridSimTags.REGISTER_RESOURCE,
                new IO_data(new Integer(super.get_id()), 12, gisID) );
        
        Sim_event ev = new Sim_event();
        while (Sim_system.running() && !isFinished()) {
            // TODO: do all messaging thru GridSim
            super.sim_get_next(ev);
        }
        
    }

}
