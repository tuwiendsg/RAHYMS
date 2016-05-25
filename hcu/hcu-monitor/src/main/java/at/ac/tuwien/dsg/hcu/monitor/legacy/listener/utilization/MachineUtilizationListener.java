package at.ac.tuwien.dsg.hcu.monitor.legacy.listener.utilization;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.legacy.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class MachineUtilizationListener implements ListenerInterface {

    private EPStatement statement;


    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        String expression;

        // measure utilization
        expression = 
                "INSERT INTO Utilization "
                        //+ "SELECT unit, task, cast(unit.getMetric('utilization', 0.0), double) as value "
                        + "SELECT type, unit, cast(unit.getMetric('utilization', 0.0), double) as value "
                        + "FROM AssignmentStreamWindow("
                        + " type=EventType.ASSIGNED OR "
                        + " type=EventType.FINISHED OR"
                        + " type=EventType.FAILED,"
                        + " unit.type=2"
                        + ") ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("MachineUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
