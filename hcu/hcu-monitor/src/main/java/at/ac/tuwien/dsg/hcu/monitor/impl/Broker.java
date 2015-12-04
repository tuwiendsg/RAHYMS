package at.ac.tuwien.dsg.hcu.monitor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.QualityEngine;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class Broker implements BrokerInterface {

    protected Map<Integer, Subscription> register;
    protected int lastSubscriptionId = 0;

    public Broker() {
        register = new HashMap<Integer, Subscription>();
    }
    
    @Override
    public void adjust(HashMap<String, Object> config) {
    }

    @Override
    public void adjust(Integer subscriptionId, HashMap<String, Object> config) {
        Subscription subscription = register.get(subscriptionId);
        subscription.setConfig(config);
        register.put(subscriptionId, subscription); // not sure if it's necessary to put back
    }

    @Override
    public void publish(Data data) {
        for (Subscription subscription: register.values() ) {
            if (data.getMetaData("eof")!=null) {
                subscription.getConsumer().receive(data);
            }
            else if (subscription.getTopic().trim().equalsIgnoreCase(data.getName().trim())) {
                QualityEngine qualityEngine = QualityEngine.getInstance(subscription);
                if (qualityEngine!=null) {
                    qualityEngine.receive(data);
                } else {
                    subscription.getConsumer().receive(data);
                }
            }
        }
    }

    @Override
    public void publish(List<Data> data) {
        for (Data adata: data) {
            publish(adata);
        }
    }

    @Override
    public int subscribe(Subscription subscription) {

        // put to register
        int id = lastSubscriptionId++;
        register.put(id, subscription);
        
        // create QualityEngine for this subscription
        new QualityEngine(subscription);
        
        return id;
    }


}
