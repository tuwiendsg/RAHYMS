package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;
import at.ac.tuwien.dsg.hcu.util.Util;

public class BaseEPLProcessor extends BaseProcessor {

    @Override
    public void initiate(EPServiceProvider epService, MonitoringConsumerInterface consumer, String topic, HashMap<String, Object> args) {

        super.initiate(epService, consumer, topic, args);
        String expression = (String) args.get("epl_expression");
        this.addListener(expression, new BaseListener());
        
    }

}
