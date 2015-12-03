package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class TracerConsumer extends BaseMonitoringConsumer {

    protected MonitoringAgentInterface agent;
    protected Tracer tracer;
    protected String tracerName;
    protected List<String> sources = new LinkedList<String>();
    protected Map<String,String> currentRow;
    protected Double prevTime = -1.0;
    protected String filePrefix;
    
    @Override
    public void receive(Data data) {
        if (data==null || data.getMetaData("eof")!=null || data.getMetaData("time")==null) {
            tracer.close();
        }
        if (sources.contains(data.getName())) {
            if (prevTime!=data.getMetaData().getTime()) {
                writeRow();
                currentRow = new HashMap<String, String>();
                currentRow.put("time", String.format("%f",data.getMetaData().getTime()));
                prevTime = data.getMetaData().getTime();
            }
            currentRow.put(data.getName(), data.getValue().toString());
        }
    }

    private void writeRow() {
        if (currentRow!=null && tracer!=null) {
            String row = currentRow.get("time");
            for (String source: sources) {
                row += "," + currentRow.getOrDefault(source, "");
            }
            tracer.traceln(row);
        }
    }

    @Override
    public void subscribeTo(MonitoringProducerInterface producer,
            Subscription subscription) {
        super.subscribeTo(producer, subscription);
        sources.add(subscription.getTopic());
    }
    
    @Override
    public void start() {
        if (filePrefix!=null && sources!=null) {
            String header = "time";
            for (String source: sources) {
                header += "," + source;
            }
            tracerName = new BigInteger(130, new SecureRandom()).toString(32);
            tracer = Tracer.createTracer(tracerName, filePrefix, null, header, true);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
        filePrefix = (String) config.get("file_prefix");
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
    }

}
