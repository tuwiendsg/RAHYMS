package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public abstract class BaseMonitoringConsumer implements MonitoringConsumerInterface {

    protected MonitoringAgentInterface agent;
    
    @Override
    public void subscribeTo(MonitoringProducerInterface producer,
            Subscription subscription) {
        subscription.setConsumer(this);
        producer.subscribe(subscription);
    }

    @Override
    public void setMonitoringAgent(MonitoringAgentInterface agent) {
        this.agent = agent;
    }

}
