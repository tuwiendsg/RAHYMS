package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;

public class BaseEPLProcessor extends BaseProcessor {

    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic, Map<String, Object> args) {

        super.initiate(epService, consumer, topic, args);
        String expression = (String) args.get("epl_expression");
        this.addListener(expression, new BaseListener());
        
    }

}
