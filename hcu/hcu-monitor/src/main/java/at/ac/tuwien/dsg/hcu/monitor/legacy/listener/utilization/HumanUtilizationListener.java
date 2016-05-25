package at.ac.tuwien.dsg.hcu.monitor.legacy.listener.utilization;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.legacy.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class HumanUtilizationListener implements ListenerInterface {

    private static final double MAX_HUMAN_LOAD = 20.0;
    
    private EPStatement statement;

    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        String expression;

        // measure started assignment
        expression = 
                "INSERT INTO AssignmentCount "
                        + "SELECT unit, count(*) as value "
                        + "FROM AssignmentStream("
                        + "  type=EventType.NOTIFIED"
                        + "  ,unit.type=1"
                        + ") "
                        + "GROUP BY unit.id";
        //epService.getEPAdministrator().createEPL(expression);

        // measure finished/failed assignment
        expression = 
                "INSERT INTO FinishedCount "
                        + "SELECT unit, count(*) as value "
                        + "FROM AssignmentStream("
                        + "  type=EventType.FINISHED OR type=EventType.FAILED"
                        + "  ,unit.type=1"
                        + ") "
                        + "GROUP BY unit.id";
        //epService.getEPAdministrator().createEPL(expression);

        // measure utilization
        expression = 
                "INSERT INTO Utilization "
                        + "SELECT last(AssignmentCount.unit) as unit, (last(AssignmentCount.value) - coalesce(last(FinishedCount.value), 0.0)) / " + MAX_HUMAN_LOAD + " as value "
                        + "FROM AssignmentCount left outer join FinishedCount "
                        + "ON AssignmentCount.unit=FinishedCount.unit "
                        + "GROUP BY AssignmentCount.unit.id";

        expression = "INSERT INTO Utilization "
                + "SELECT type, unit, unit.getActiveAssignmentCount() / " + MAX_HUMAN_LOAD + " as value "
                + "FROM AssignmentStreamWindow("
                + " type=EventType.ASSIGNED OR "
                + " type=EventType.FINISHED OR"
                + " type=EventType.FAILED,"
                + " unit.type=1) ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("HumanUtilization [" + (i++) + "]: " + event.getUnderlying());
                    if ((double)event.get("value")<0) {
                        //System.out.println("********* MINUS ************");
                    }
                }
            }
        });

    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
