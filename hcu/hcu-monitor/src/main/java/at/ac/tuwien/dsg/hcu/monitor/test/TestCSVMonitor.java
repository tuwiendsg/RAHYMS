package at.ac.tuwien.dsg.hcu.monitor.test;

import java.util.HashMap;

import at.ac.tuwien.dsg.hcu.monitor.impl.adapter.CSVMonitoringAdapter;
import at.ac.tuwien.dsg.hcu.monitor.impl.agent.BaseMonitoringAgent;
import at.ac.tuwien.dsg.hcu.monitor.impl.consumer.BaseMonitoringConsumer;
import at.ac.tuwien.dsg.hcu.monitor.impl.producer.CSVMonitoringProducer;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class TestCSVMonitor extends BaseMonitoringConsumer {
    
    boolean eof = false;

    public static void main(String[] args) {
        
        // create agent
        HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(CSVMonitoringAdapter.CFG_SET_CSV_FILE, "data/4.2.2015/10.99.0.18.csv");
        config.put(CSVMonitoringAdapter.CFG_SET_CSV_TIME_COL, "Time ");
        MonitoringAdapterInterface adapter = new CSVMonitoringAdapter();
        MonitoringProducerInterface producer = new CSVMonitoringProducer();
        MonitoringAgentInterface agent = new BaseMonitoringAgent("TEST_AGENT", config, adapter, producer);
        agent.start();
        
        // create consumer
        TestCSVMonitor consumer = new TestCSVMonitor(agent);
        
        // test
        consumer.test();
        
        agent.stop();

    }
    
    MonitoringAgentInterface agent;
    int subscriptionId;
    
    public TestCSVMonitor(MonitoringAgentInterface agent) {
        this.agent = agent;
        Subscription subscription = new Subscription();
        subscription.setTopic("cpu_idle ");
        subscribeTo(agent.getProducer(), subscription);
    }
    
    public void test() {
        HashMap<String, Object> tick = new HashMap<String, Object>();
        tick.put(CSVMonitoringAdapter.CFG_TICK, "");
        
        while (!eof) {
            agent.adjust(tick);
        }
    }

    @Override
    public void receive(Data data) {
        System.out.println(data);
        if (data.getMetaData().get("eof")!=null) {
            eof = true;
        }
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
        // TODO Auto-generated method stub
        
    }

}
