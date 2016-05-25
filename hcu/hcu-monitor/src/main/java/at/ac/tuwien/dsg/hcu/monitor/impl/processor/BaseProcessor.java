package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProcessorInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;
import at.ac.tuwien.dsg.hcu.util.Util;

public abstract class BaseProcessor implements ProcessorInterface {

    protected EPStatement statement;
    protected EPServiceProvider epService;
    protected ConsumerInterface consumer;
    protected String topicName;

    @Override
    public void terminate() {
        if (statement != null) statement.removeAllListeners();
    }

    protected void addListener(String expression, UpdateListener listener) {
        epService.getEPAdministrator()
            .createEPL(expression)
            .addListener(listener);
    }

    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic,
            Map<String, Object> args) {
        this.epService = epService;
        this.consumer = consumer;
        this.topicName = topic;
    }

    protected class BaseListener implements UpdateListener {
        @SuppressWarnings({ "rawtypes" })
        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            int i = 0;
            for (EventBean event: newEvents) {
                //Util.log().warning("BaseProcessor [" + (i++) + "]: " +  event.getUnderlying());
                Double value = (Double) ((Map)event.getUnderlying()).get("value");
                MetaData metaData = (MetaData) ((Map)event.getUnderlying()).get("metaData");
                if (topicName.equals("correlated_utilization")) {
                    //Util.log().info(event.getUnderlying().toString());
                }
                Data data = new Data();
                data.setName(topicName);
                data.setValue(value);
                data.setMetaData(metaData);
                
                consumer.getAgent().publish(data);
            }
        }
        
    }
}
