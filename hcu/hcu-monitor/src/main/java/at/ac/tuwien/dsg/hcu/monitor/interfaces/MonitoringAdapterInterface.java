package at.ac.tuwien.dsg.hcu.monitor.interfaces;

public interface MonitoringAdapterInterface {

    public void setMonitoringProducer(MonitoringProducerInterface producer);
    public void start();
    public void stop();
    
}
