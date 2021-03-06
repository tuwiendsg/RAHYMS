package at.ac.tuwien.dsg.hcu.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.StatisticInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.Quality;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;
import at.ac.tuwien.dsg.hcu.util.Util;

public class QualityEngine implements Wakeable {

    protected static int MAX_ESTIMATION = 9999999; // limit the number of estimation in the case of retained data, afterwards we wont schedule any estimation
    protected static int MAX_RETAINED_DATA = 9999;
    
    protected Subscription subscription;
    protected Quality quality;
    
    protected Waker waker;
    protected Queue<Data> retainedData;
    protected Data lastSentData;
    protected int lastWakeId = 0;
    protected double lastWakeTime = 0.0;
    protected boolean dataChanged = false;
    protected boolean isFinished = false;
    protected int estimationCount = 0;
    protected boolean producerBasedQualityEngineEnabled = false;
    protected StatisticInterface statEntity;
    
    private static Map<Integer, QualityEngine> instances = new HashMap<Integer, QualityEngine>();
    
    public QualityEngine(Subscription subscription, boolean producerBasedQualityEngineEnabled, StatisticInterface statEntity) {
        this(subscription);
        this.producerBasedQualityEngineEnabled = producerBasedQualityEngineEnabled;
        this.statEntity = statEntity;
    }
    
    public QualityEngine(Subscription subscription) {
        this.subscription = subscription;
        this.quality = subscription.getQuality();
        if (this.quality==null) {
            this.quality = new Quality();
        }
        instances.put(subscription.getId(), this);
    }
    
    public static QualityEngine getInstance(Subscription subscription) {
        return instances.get(subscription.getId());
    }
    
    public void setWaker(Waker waker) {
        this.waker = waker;
    }
    
    public void finish() {
        isFinished = true;
    }
    
    private synchronized Data estimate() {
        if (retainedData==null) {
            //Util.log().warning("#1 retainedData is null");
        }
        if (retainedData==null || retainedData.size()==0) {
            estimationCount++;
            return lastSentData;
        }
        
        Double sum = 0.0;
        for (Data d: retainedData) {
            Double doubleValue = d.getDoubleValue();
            if (doubleValue!=null) {
                sum += doubleValue;
            }
        }
        Data data = null;
        if (retainedData==null) {
            Util.log().warning("#2 retainedData is null");
        }
        try {
            data = (Data) ((Data) retainedData.element()).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (data==null) {
            Util.log().warning("data is null");
        }
        data.setValue(sum / retainedData.size());
        return data;
    }
    
    public void receive(Data data) {
        
        // send EOF data
        if (data==null || data.getMetaData("eof")!=null || data.getMetaData("time")==null) {
            send(data);
            finish();
            return;
        }
        
        Double accuracy = quality.get(Quality.ACCURACY);
        Double freshness = quality.get(Quality.FRESHNESS);
        Double rate = quality.get(Quality.RATE);
        
        // no Quality specified, just sent
        if ((accuracy==null || accuracy==0.0) && 
            (freshness==null || freshness==0.0) && 
            (rate==null || rate==0.0)) {
            send(data);
            return;
        }

        addRetainedData(data);
        estimationCount = 0;

        // process Accuracy
        if (accuracy!=null) {
            if (lastSentData==null || Math.abs(data.getDoubleValue()-lastSentData.getDoubleValue()) >= accuracy) {
                send(data);
                // cancel waker
                lastWakeId = 0;
                lastWakeTime = 0.0;
                return;
            }
        }
        
        // process Freshness
        if (freshness!=null) {
            if (!dataChanged) {
                if (lastSentData==null || Double.compare(data.getDoubleValue(), lastSentData.getDoubleValue()) != 0) {
                    dataChanged = true;
                    lastWakeTime = (Double)data.getMetaData("time") + freshness;
                    lastWakeId = waker.wakeMeAfter(this, freshness);
                }
            }
        }

        // process Rate
        if (rate!=null && rate>0.0) {
            double nextWakeTimeByRate = (Double)data.getMetaData("time") + rate;
            if (lastWakeId==0) {
                // this is probably the first time we got data, or waker has been canceled, reschedule again
                lastWakeId = waker.wakeMeAfter(this, rate);
                lastWakeTime = nextWakeTimeByRate;
            } else if (lastWakeTime > nextWakeTimeByRate) {
                // we should wake up earlier due to rate requirement
                lastWakeId = waker.wakeMeAfter(this, rate);
                lastWakeTime = nextWakeTimeByRate;
            }
        }

    }
    
    public void wake(int wakeId) {
        
        if (wakeId!=lastWakeId) {
            // cancel
            return;
        }
        
        Data data = estimate();
        send(data);
        
        // process Rate, for next sampling
        Double rate = quality.get(Quality.RATE);
        if (rate!=null && rate>0.0 && !isFinished && estimationCount < MAX_ESTIMATION) {
            double nextWakeTimeByRate = lastWakeTime + rate;
            lastWakeId = waker.wakeMeAfter(this, rate);
            lastWakeTime = nextWakeTimeByRate;
        } else {
            System.out.println("QualityEngine was forcefully terminated!!");
        }

    }
    
    private void send(Data data) {
        if (producerBasedQualityEngineEnabled) {
            subscription.getConsumerAgent().getBroker().publish(data);
        } else {
            // this is run by broker, just give directly to consumer
            subscription.getConsumerAgent().receive(data);
        }
        statEntity.increaseProperty("message_published_count");
        lastSentData = data;
        dataChanged = false;
        retainedData = null;
    }
    
    private void addRetainedData(Data data) {
        if (retainedData == null) {
            retainedData = new LinkedList<Data>();
        }
        while (retainedData.size()>=MAX_RETAINED_DATA) {
            retainedData.poll();
        }
        retainedData.add(data);
    }

}
