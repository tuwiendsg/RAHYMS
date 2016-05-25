package at.ac.tuwien.dsg.hcu.monitor.gridsim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.util.Util;
import eduni.simjava.Sim_system;
import gridsim.GridSim;

public class GSMonitoringSimulation extends GridSim {

    public static final int WAKE_UP = 3000;

    private static String NAME = "GSMonitoringSimulation";
    
    private static int numAgents = 0;
    private static List<GSMonitoringBroker> brokers;
    private static List<GSMonitoringAgent> gsAgents;
    private static List<GSMonitoringAgent> stopped = new ArrayList<GSMonitoringAgent>();

    public GSMonitoringSimulation() throws Exception {
        super(NAME, 560);
    }
    
    public static void startSimulation(Map<String, AgentInterface> agents, List<BrokerInterface> brokers, boolean debug) {
        
        try {
            // INITIALIZATION
            Util.log().info("Initializing GridSim package");
            // number of grid users, i.e., myself. This is necessary for GridSim for waiting until all users finish
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = true ;  // true means trace GridSim events
            GridSim.init(numUser, calendar, traceFlag, true);
            
            // create myself, auto register to GridSim
            new GSMonitoringSimulation();
            
            // create GSMonitoringAgent for all producing gsAgents, auto register to GridSim
            gsAgents = new ArrayList<GSMonitoringAgent>();
            numAgents = 0;
            for (AgentInterface agent: agents.values()) {
                if (agent.getProducer()!=null) {
                    // only producing agents needs to be created as GS entity
                    GSMonitoringAgent gsAgent = new GSMonitoringAgent(agent);
                    gsAgents.add(gsAgent);
                    numAgents++;
                    if (agent instanceof Wakeable) {
                        ((Wakeable)agent).setWaker(gsAgent);
                    }
                }
            }
            
            // create waker
            GSWaker waker = GSWaker.getInstance();
            numAgents++;
    
            // create broker
            GSMonitoringSimulation.brokers = new ArrayList<GSMonitoringBroker>();
            for (BrokerInterface broker: brokers) {
                if (broker instanceof Wakeable) {
                    ((Wakeable) broker).setWaker(waker);
                }
                GSMonitoringSimulation.brokers.add(new GSMonitoringBroker(broker));
                numAgents++;
            }

            // start simulation
            // start all agents
            for (AgentInterface agent: agents.values()) {
                agent.start();
            }

            Util.log().info("Starting simulation");
            GridSim.startGridSimulation(debug);
            
            // simulation finished
            Util.log().info(NAME + " finishes.");
        }
        catch (Exception e) {
            e.printStackTrace();
            Util.log().severe("Unwanted errors happen");
        }
        
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body() {

        // wait until all services have been registered
        Util.log().info("Waiting until all gsAgents registered");
        LinkedList<Integer> agentList = null;
        while (true) {
            super.gridSimHold(1.0);    // hold by 1 second
            agentList = getGridResourceList();
            if (agentList.size() == numAgents) {
                break;
            }
        }
        Util.log().info(agentList.size() + " gsAgents have been registered");

        try {
            
            // start all GSMonitoringAgent
            for (int agentId: agentList) {
                try {
                    GSMonitoringAgent agent = (GSMonitoringAgent)Sim_system.get_entity(agentId);
                    agent.startSimulation();
                } catch (ClassCastException e) {
                }
            }
            
            // wait till all gsAgents finishes
            while(true) {
                super.gridSimHold(10.0);    // hold by 10 seconds
                boolean allFinished = true;
                for (int agentId: agentList) {
                    GSMonitoringAgent agent = null;
                    try {
                        agent = (GSMonitoringAgent)Sim_system.get_entity(agentId);
                    } catch (ClassCastException e) {
                        // only wait for GSMonitoringAgent
                    }
                    if (agent !=null && !agent.isFinished()) {
                        allFinished = false;
                        break;
                    } else if (agent!=null && !stopped.contains(agent)) {
                        Util.log().info("Agent " + agent.get_name() + " is stopped. Waiting for gsAgents: " + getRunningAgentName());
                        stopped.add(agent);
                    }
                }
                if (allFinished) {
                    break;
                }
            }
            
            // shutting entities
            GSWaker.getInstance().finish();
            Util.log().info("Stopping waker " + GSWaker.getInstance().get_name());
            for (GSMonitoringBroker broker: brokers) {
                broker.finish();
                Util.log().info("Stopping broker " + broker.get_name());
            }
            shutdownGridStatisticsEntity();
            shutdownUserEntity();
            terminateIOEntities();
            
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private List<String> getRunningAgentName() {
        List<String> result = new ArrayList<String>();
        for (GSMonitoringAgent agent: gsAgents) {
            if (!agent.isFinished()) {
                result.add(agent.get_name());
            }
        }
        return result;
    }
    
}
