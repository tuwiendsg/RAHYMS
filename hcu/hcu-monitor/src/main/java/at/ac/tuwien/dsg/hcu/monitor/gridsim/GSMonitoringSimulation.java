package at.ac.tuwien.dsg.hcu.monitor.gridsim;

import eduni.simjava.Sim_system;
import gridsim.GridSim;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.util.Util;

public class GSMonitoringSimulation extends GridSim {

    private static String NAME = "GSMonitoringSimulation";
    
    private static int numAgents = 0;

    public GSMonitoringSimulation() throws Exception {
        super(NAME, 560);
    }
    
    public static void startSimulation(List<MonitoringAgentInterface> producerAgents, boolean debug) {
        
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
            
            // create GSMonitoringAgent for all producing agents, auto register to GridSim
            numAgents = producerAgents.size();
            for (MonitoringAgentInterface agent: producerAgents) {
                new GSMonitoringAgent(agent);
            }
    
            // start simulation
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
        Util.log().info("Waiting until all agents registered");
        LinkedList<Integer> agentList = null;
        while (true) {
            super.gridSimHold(1.0);    // hold by 1 second
            agentList = getGridResourceList();
            if (agentList.size() == numAgents) {
                break;
            }
        }
        Util.log().info(agentList.size() + " agents have been registered");

        try {
            
            // start all agents
            for (int agentId: agentList) {
                GSMonitoringAgent agent = (GSMonitoringAgent)Sim_system.get_entity(agentId);
                agent.startSimulation();
            }
            
            // wait till all agents finises
            while(true) {
                super.gridSimHold(10.0);    // hold by 10 seconds
                boolean allFinished = true;
                for (int agentId: agentList) {
                    GSMonitoringAgent agent = (GSMonitoringAgent)Sim_system.get_entity(agentId);
                    if (!agent.isFinished()) {
                        allFinished = false;
                        break;
                    }
                }
                if (allFinished) {
                    break;
                }
            }
            
            // shutting entities
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
}
