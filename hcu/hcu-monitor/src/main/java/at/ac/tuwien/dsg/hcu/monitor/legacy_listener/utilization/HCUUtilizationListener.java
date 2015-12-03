package at.ac.tuwien.dsg.hcu.monitor.legacy_listener.utilization;

import gridsim.GridSim;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.model.SCU;
import at.ac.tuwien.dsg.hcu.monitor.legacy_listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.util.Tracer;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class HCUUtilizationListener implements ListenerInterface {

    private EPStatement statement;


    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        final MonitorInterface _monitor = monitor;
        String expression;

        // measure HCU utilization
        expression = "INSERT INTO HCUUtilization "
                        + "SELECT asw.type as type, csw.collective as collective, avg(coalesce(value, 0.0)) as value, count(*) as cnt "
                        + "FROM "
                        + "CollectiveStreamWindow(type!=EventType.FINISHED, type!=EventType.FAILED) as csw "
                        + "JOIN AssignmentStreamWindow(type=EventType.ASSIGNED) as asw "
                        + "ON csw.task=asw.task "
                        + "JOIN Utilization "
                        + "ON asw.unit=Utilization.unit "
                        + "GROUP BY csw.collective "
                        + "HAVING count(*)>0";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("HCUUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });

        // listen utilization
        expression = "SELECT irstream * FROM HCUUtilization ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("UtilListener [" + (i++) + "]: " + event.getUnderlying());
                    SCU collective = (SCU) event.get("collective");
                    collective.setProperty("utilization", event.get("value"));
                    // insert fact
                    _monitor.getRuleEngine().insertFact(collective);
                    // trace
                    Tracer.traceln("hcu_util_detail", String.format(
                            "%s,%s,%.3f,%d,%s,%s",
                            collective.getId(),
                            GridSim.clock(),
                            event.get("value"),
                            event.get("cnt"),
                            event.get("type"),
                            //collective.getBatch().getAssignments().size(),
                            collective.debugUtilization()
                    ));
                }
            }
        });
        

        // listen aggregate utilization
        expression = "SELECT irstream min(value) as min_util, avg(value) as avg_util, max(value) as max_util, count(*) cnt, median(value) as median_util, avedev(value) as avedev_util, stddev(value) as stddev_util "
                + "FROM HCUUtilization ";
                //+ "JOIN CollectiveStreamWindow ON CollectiveStreamWindow.collective=HCUUtilization.collective "
                //+ "WHERE collectiveStatus(HCUUtilization.collective)='Running' ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    HashMap hcuUtil = (HashMap) event.getUnderlying();
                    //Util.log().warning("AggregateUtilListener [" + (i++) + "]: util=" + hcuUtil);
                    // trace
                    Tracer.traceln("hcu_util_aggregate", String.format(
                            "%s,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f",
                            GridSim.clock(),
                            hcuUtil.get("cnt"),
                            hcuUtil.get("avg_util")==null ? 0.0 : hcuUtil.get("avg_util"),
                            hcuUtil.get("min_util")==null ? 0.0 : hcuUtil.get("min_util"),
                            hcuUtil.get("max_util")==null ? 0.0 : hcuUtil.get("max_util"),
                            hcuUtil.get("median_util")==null ? 0.0 : hcuUtil.get("median_util"),
                            hcuUtil.get("avedev_util")==null ? 0.0 : hcuUtil.get("avedev_util"),
                            hcuUtil.get("stddev_util")==null ? 0.0 : hcuUtil.get("stddev_util")
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
