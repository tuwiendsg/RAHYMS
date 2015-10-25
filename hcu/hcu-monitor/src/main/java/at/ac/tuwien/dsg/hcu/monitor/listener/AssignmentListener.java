package at.ac.tuwien.dsg.hcu.monitor.listener;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class AssignmentListener implements ListenerInterface {

    private EPStatement statement;

    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        String expression;

        expression = "INSERT INTO AssignmentStreamWindow "
                + "SELECT * FROM AssignmentStream ";
        epService.getEPAdministrator().createEPL(expression);

        expression = "SELECT irstream * FROM AssignmentStreamWindow() ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    //Util.log().warning("AssignmentStream [" + (i++) + "]: " +  event.getUnderlying());
                }
                for (EventBean event: oldEvents) {
                    //Util.log().warning("AssignmentStream OLD [" + (i++) + "]: " +  event.getUnderlying());
                }
            }
        });
        
}

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
