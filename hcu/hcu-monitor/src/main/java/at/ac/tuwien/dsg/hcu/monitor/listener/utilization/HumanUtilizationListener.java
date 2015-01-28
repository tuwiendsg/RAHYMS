package at.ac.tuwien.dsg.hcu.monitor.listener.utilization;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.listener.ListenerInterface;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class HumanUtilizationListener implements ListenerInterface {

    private EPStatement statement;


    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        String expression;

        // create window
        expression = "CREATE WINDOW AssignmentCount.win:keepall() AS "
                        //+ "SELECT unit, task, 0L as value FROM AssignmentStream";
                        + "SELECT unit, 0L as value FROM AssignmentStream";
        epService.getEPAdministrator().createEPL(expression);
        expression = "CREATE WINDOW FinishedCount.win:keepall() AS "
                        //+ "SELECT unit, task, 0L as value FROM AssignmentStream";
                        + "SELECT unit, 0L as value FROM AssignmentStream";
        epService.getEPAdministrator().createEPL(expression);
        
        // measure started assignment
        expression = 
                "INSERT INTO AssignmentCount "
                        //+ "SELECT unit, task, count(*) as value "
                        + "SELECT unit, count(*) as value "
                        + "FROM AssignmentStream("
                        + "  type=EventType.ASSIGNED"
                        + "  ,unit.type=1"
                        + ") "
                        + "GROUP BY unit.id";
        epService.getEPAdministrator().createEPL(expression);

        // measure finished/failed assignment
        expression = 
                "INSERT INTO FinishedCount "
                        //+ "SELECT unit, task, count(*) as value "
                        + "SELECT unit, count(*) as value "
                        + "FROM AssignmentStream("
                        + "  type=EventType.FINISHED OR type=EventType.FAILED"
                        + "  ,unit.type=1"
                        + ") "
                        + "GROUP BY unit.id";
        epService.getEPAdministrator().createEPL(expression);

        // measure utilization
        expression = 
                "INSERT INTO Utilization "
                        //+ "SELECT last(AssignmentCount.unit) as unit, last(AssignmentCount.task) as task, (last(AssignmentCount.value) - coalesce(last(FinishedCount.value), 0.0)) / 4 as value "
                        + "SELECT last(AssignmentCount.unit) as unit, (last(AssignmentCount.value) - coalesce(last(FinishedCount.value), 0.0)) / 4 as value "
                        + "FROM AssignmentCount full outer join FinishedCount "
                        + "ON AssignmentCount.unit=FinishedCount.unit "
                        + "GROUP BY AssignmentCount.unit.id";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("HumanUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });

        // listener
        expression = "SELECT * FROM AssignmentCount";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                //Util.log().warning("AssignmentCount: " + newEvents[0].getUnderlying());
            }
        });

        // listener
        expression = "SELECT * FROM FinishedCount";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                //Util.log().warning("FinishedCount: " + newEvents[0].getUnderlying());
            }
        });

        // listener
/*
        expression = "SELECT * FROM Utilization";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    Util.log().warning("HumanUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
*/
        expression = "SELECT * FROM AssignmentStream ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("Assignment [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
