package at.ac.tuwien.dsg.hcu.monitor.listener;

import gridsim.GridSim;
import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.model.SCU;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class FinishListener implements ListenerInterface {

    private EPStatement statement;

    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        final MonitorInterface _monitor = monitor;
        String expression;

        expression = "ON pattern [every stream=CollectiveStreamWindow(type=EventType.FINISHED)] "
                + "DELETE FROM HCUUtilization "
                + "WHERE HCUUtilization.collective.id=stream.collective.id ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    Util.log().warning("********DELETE new HCUUtilization [" + (i++) + "]: " + event.getUnderlying());
                    SCU collective = (SCU) event.get("collective");
                    collective.setProperty("utilization", 0.0);
                    // insert fact
                    _monitor.getRuleEngine().insertFact(collective);
                    // trace
                    Tracer.traceln("hcu_utilization", String.format(
                            "%s,%s,%d,%d,%s,%s",
                            collective.getId(),
                            GridSim.clock(),
                            0,
                            0,
                            "DELETED",
                            //collective.getBatch().getAssignments().size(),
                            collective.debugUtilization()
                    ));
                }
                for (EventBean event: oldEvents) {
                    //Util.log().warning("DELETE old HCUUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });

        expression = "ON pattern [every stream=CollectiveStreamWindow(type=EventType.FINISHED)] "
                + "DELETE FROM CollectiveStreamWindow "
                + "WHERE CollectiveStreamWindow.collective.id=stream.collective.id ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("DELETE newEvents [" + (i++) + "]: " + event.getUnderlying());
                }
                for (EventBean event: oldEvents) {
                    //Util.log().warning("DELETE oldEvents [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
        
        expression = "ON pattern [every stream=CollectiveStreamWindow(type=EventType.FINISHED)] "
                + "DELETE FROM AssignmentStreamWindow "
                + "WHERE AssignmentStreamWindow.task=stream.collective.task ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("DELETE AssignmentStreamWindow [" + (i++) + "]: " + event.getUnderlying());
                }
                for (EventBean event: oldEvents) {
                    //Util.log().warning("DELETE oldEvents [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
