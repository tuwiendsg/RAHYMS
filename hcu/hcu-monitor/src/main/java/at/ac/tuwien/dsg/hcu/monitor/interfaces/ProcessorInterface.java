package at.ac.tuwien.dsg.hcu.monitor.interfaces;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

public interface ProcessorInterface {
    public void terminate();
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic,
            Map<String, Object> args);
}
