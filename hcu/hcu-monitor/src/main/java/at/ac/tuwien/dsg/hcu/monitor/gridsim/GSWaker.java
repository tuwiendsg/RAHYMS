package at.ac.tuwien.dsg.hcu.monitor.gridsim;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimCore;
import gridsim.GridSimTags;
import gridsim.IO_data;

public class GSWaker extends GridSimCore implements Waker {

    protected boolean finished = false;
    protected static GSWaker instance;
    protected int lastId = 0;
    protected Map<Integer, Wakeable> sleepers;
    
    public static GSWaker getInstance() {
        if (instance==null) {
            try {
                instance = new GSWaker();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
    
    protected GSWaker() throws Exception {
        super("GSWaker", 100.0);
        sleepers = new HashMap<Integer, Wakeable>();
    }

    @Override
    public int wakeMeAfter(Wakeable object, Double wakeTime) {
        int id = ++lastId;
        sleepers.put(id, object);
        super.send(super.output, wakeTime, GSMonitoringSimulation.WAKE_UP,
                new IO_data(id, 4, get_id()) );
        return id;
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
            // wait for WAKE_UP event
            super.sim_get_next(ev);
            switch (ev.get_tag()) {
                case GSMonitoringSimulation.WAKE_UP:
                    Integer wakeId = (Integer)ev.get_data();
                    Wakeable sleeper = sleepers.get(wakeId);
                    if (sleeper!=null) {
                        sleeper.wake(wakeId);
                    }
                    break;
            }
        }
        
    }

    @Override
    public int wakeMeAt(Wakeable object, Double time) {
        Double t = time - GridSim.clock();
        if (t>0.0) {
            return wakeMeAfter(object, t);
        } else {
            return 0;
        }
    }
}
