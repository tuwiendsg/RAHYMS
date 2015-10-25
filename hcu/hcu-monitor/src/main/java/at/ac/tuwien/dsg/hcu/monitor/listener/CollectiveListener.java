package at.ac.tuwien.dsg.hcu.monitor.listener;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.util.Util;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CollectiveListener implements ListenerInterface {

    private EPStatement statement;

    @Override
    public void initiate(EPServiceProvider epService, MonitorInterface monitor) {

        String expression;

        expression = "SELECT irstream * FROM CollectiveStreamWindow";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    CollectiveStream stream = (CollectiveStream) event.getUnderlying();
                   // Util.log().warning("Collective [" + (i++) + "]: stream=" +  stream);
                }
                for (EventBean event: oldEvents) {
                    CollectiveStream stream = (CollectiveStream) event.getUnderlying();
                    //Util.log().warning("Collective oldEvents [" + (i++) + "]: stream=" +  stream);
                }
            }
        });
        

        expression = "INSERT INTO CollectiveStreamWindow "
                + "SELECT * FROM CollectiveStream ";
        epService.getEPAdministrator().createEPL(expression);
        
        /*
        expression = "SELECT irstream count(*) FROM CollectiveStreamWindow ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    Util.log().warning("Count Collective [" + (i++) + "]: " + event.getUnderlying());
                }
                for (EventBean event: oldEvents) {
                    Util.log().warning("Count Collective oldEvents [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });

        expression = "SELECT irstream count(*) FROM HCUUtilization as cnt ";
        epService.getEPAdministrator().createEPL(expression)
        .addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                int i = 0;
                for (EventBean event: newEvents) {
                    Util.log().warning("Count HCUUtilization [" + (i++) + "]: " + event.getUnderlying());
                }
                for (EventBean event: oldEvents) {
                    Util.log().warning("Count HCUUtilization oldEvents [" + (i++) + "]: " + event.getUnderlying());
                }
            }
        });
        */
    }

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }



}
