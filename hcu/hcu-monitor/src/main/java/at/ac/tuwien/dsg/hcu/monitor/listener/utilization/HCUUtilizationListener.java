package at.ac.tuwien.dsg.hcu.monitor.listener.utilization;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
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
                        + "SELECT CollectiveStream.*, count(*) cnt, avg(value) as value FROM "
                        + "CollectiveStream(type!=EventType.FINISHED, type!=EventType.FAILED).std:unique(collective.id) as CollectiveStream "
                        + "JOIN AssignmentStream(type=EventType.ASSIGNED).win:keepall() as AssignmentStream "
                        + "ON CollectiveStream.task=AssignmentStream.task "
                        + "JOIN Utilization.std:unique(unit.id) "
                        + "ON AssignmentStream.unit=Utilization.unit "
                        + "GROUP BY CollectiveStream.task";
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
        expression = "SELECT * FROM HCUUtilization ";
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
                }
            }
        });
        
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
