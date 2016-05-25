package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;

public class UtilizationProcessor extends BaseProcessor {

    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic, Map<String, Object> args) {
        super.initiate(epService, consumer, topic, args);
        String aggregate = (String) args.get("aggregate");
        String expression = "SELECT " + aggregate + "(doubleValue) as value, name, metaData FROM Data";
        this.addListener(expression, new BaseListener());
    }
    

}
