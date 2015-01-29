package at.ac.tuwien.dsg.hcu.monitor.listener.utilization;

import gridsim.GridSim;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.event.WrapperEventBean;

public class HCUUtilizationListener implements ListenerInterface {

    private EPStatement statement;


    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        final MonitorInterface _monitor = monitor;
        String expression;

        // measure HCU utilization
        expression = "INSERT INTO HCUUtilization "
                        + "SELECT CollectiveStream.*, avg(value) as value FROM "
                        + "CollectiveStream(type!=EventType.FINISHED, type!=EventType.FAILED).std:unique(collective.id) as CollectiveStream "
                        + "JOIN AssignmentStream(type=EventType.ASSIGNED).win:keepall() as AssignmentStream "
                        + "ON CollectiveStream.task=AssignmentStream.task "
                        + "JOIN Utilization.std:unique(unit.id) "
                        + "ON AssignmentStream.unit=Utilization.unit "
                        + "GROUP BY CollectiveStream.collective";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    Util.log().warning("HCUUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });

        // listen utilization
        expression = "SELECT * FROM HCUUtilization.std:groupwin(collective.id).win:length(1) ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    CollectiveStream collectiveStream = (CollectiveStream) ((WrapperEventBean)event).getUnderlyingEvent().getUnderlying();
                    HashMap hcuUtil = (HashMap) ((WrapperEventBean)event).getUnderlyingMap();
                    //Util.log().warning("UtilListener [" + (i++) + "]: util=" + hcuUtil + ", collective=" + collectiveStream);
                    collectiveStream.setMetricName("utilization");
                    collectiveStream.setMetricValue(hcuUtil.get("value"));
                    _monitor.getRuleEngine().insertFact(collectiveStream);
                    // trace
                    Tracer.traceln("hcu_utilization", String.format(
                            "%s,%s,%.3f,%d,%s",
                            collectiveStream.getCollective().getId(),
                            GridSim.clock(),
                            hcuUtil.get("value"),
                            collectiveStream.getCollective().getBatch().getAssignments().size(),
                            collectiveStream.getType()
                    ));
                }
            }
        });
        
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
