package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;
import at.ac.tuwien.dsg.hcu.util.Tracer;

public class TracerConsumer extends BaseConsumer {

    protected AgentInterface agent;
    protected Tracer tracer;
    protected String tracerName;
    protected List<String> columns = new LinkedList<String>();
    protected Map<String,String> currentRow = new HashMap<String, String>();
    protected Double prevTime = -1.0;
    protected String filePrefix;
    protected String metadataAttribute = null; // if null, we create columns by subscription
    
    @Override
    public void receive(Data data) {
        if (data==null || data.getMetaData("eof")!=null || data.getMetaData("time")==null) {
            tracer.close();
        }
        String col = (String) ((metadataAttribute!=null) ? data.getMetaData(metadataAttribute) : data.getName());
        if (columns.contains(col)) {
            if (prevTime<data.getMetaData().getTime()) {
                writeRow();
                currentRow = new HashMap<String, String>();
                currentRow.put("time", String.format("%f",data.getMetaData().getTime()));
                prevTime = data.getMetaData().getTime();
            }
            currentRow.put(col, (data.getValue()==null)?"":data.getValue().toString());
        }
    }

    private void writeRow() {
        if (currentRow!=null && tracer!=null) {
            String row = currentRow.get("time");
            for (String source: columns) {
                row += "," + currentRow.getOrDefault(source, "");
            }
            tracer.traceln(row);
        }
    }

    @Override
    public void subscribeTo(ProducerInterface producer,
            Subscription subscription) {
        super.subscribeTo(producer, subscription);
        if (metadataAttribute==null) {
            columns.add(subscription.getTopic());
        }
    }
    
    @Override
    public void start() {
        if (filePrefix!=null && columns!=null) {
            String header = "time";
            for (String source: columns) {
                header += "," + source;
            }
            tracerName = new BigInteger(130, new SecureRandom()).toString(32);
            tracer = Tracer.createTracer(tracerName, filePrefix, null, header, true);
        }
    }

    @Override
    public void stop() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void adjust(Map<String, Object> config) {
        super.adjust(config);
        if (config.containsKey("file_prefix")) {
            filePrefix = (String) config.get("file_prefix");
        }
        if (config.containsKey("by_metadata")) {
            Map<String, Object> byMetadata = (Map<String, Object>) config.get("by_metadata");
            if (byMetadata!=null) {
                metadataAttribute = (String) byMetadata.get("attribute");
                columns = (List<String>) byMetadata.get("values");
            }
        }
    }

    @Override
    public void addTopic(String topicName, Map<String, Object> config) {
    }

}
