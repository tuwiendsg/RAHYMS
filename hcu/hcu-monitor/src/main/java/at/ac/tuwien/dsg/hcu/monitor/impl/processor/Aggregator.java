package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;
import at.ac.tuwien.dsg.hcu.util.Util;

public class Aggregator extends BaseProcessor {

    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic, HashMap<String, Object> args) {

        super.initiate(epService, consumer, topic, args);
        String aggregate = (String) args.get("aggregate");
        String dataWindow = (String) args.get("from");

        String aggregateExpression =
                "SELECT " + aggregate + " as value, last(metaData) as metaData, count(*) as count FROM " + dataWindow;
        this.addListener(aggregateExpression, new AggregatorListener());
        
    }

    protected class AggregatorListener implements UpdateListener {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            int i = 0;
            for (EventBean event: newEvents) {
                //Util.log().warning("AggregatorListener[" + (i++) + "]: " +  event.getUnderlying());
                Double value = (Double) ((Map)event.getUnderlying()).get("value");
                Long count = (Long) ((Map)event.getUnderlying()).get("count");
                MetaData lastMetaData = (MetaData) ((Map)event.getUnderlying()).get("metaData");
                Data data = new Data();
                data.setName(topicName);
                data.setValue(value);
                data.setMetaData("time", lastMetaData.getTime());
                data.setMetaData("count", count);
                
                consumer.getAgent().getBroker().publish(data);
            }
        }
        
    }

}
