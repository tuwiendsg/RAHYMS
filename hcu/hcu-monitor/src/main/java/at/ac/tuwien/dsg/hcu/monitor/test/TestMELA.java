package at.ac.tuwien.dsg.hcu.monitor.test;

import at.ac.tuwien.dsg.hcu.monitor.impl.adapter.MELAMonitoringAdapter;
import at.ac.tuwien.dsg.hcu.monitor.impl.producer.BaseMonitoringProducer;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;

public class TestMELA {

    public static void main(String[] args) {
        MonitoringAdapterInterface monitor = new MELAMonitoringAdapter();
        MonitoringProducerInterface producer = new BaseMonitoringProducer();
        
        monitor.setMonitoringProducer(producer);
        monitor.start();
    }

}
