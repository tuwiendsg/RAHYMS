package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProcessorInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;

public class EventBasedConsumer extends BaseConsumer {

    protected static final String DEFAULT_ENGINE_URI = "dsg.tuwien.ac.at/hcu/monitor/impl/consumer/EventBasedConsumer"; 

    protected Configuration configuration = null;
    protected EPServiceProvider epService = null;
    //protected List<Class<ProcessorInterface>> eventProcessorClasses;
    protected List<ProcessorInterface> eventProcessors;
    protected boolean stopped = false;
    
    @Override
    public void receive(Data data) {
        if (data==null || data.getMetaData("eof")!=null || data.getMetaData("time")==null) {
            if (agent.isRunning()) {
                // publish EOF for all topics
                List<Data> eofData = new ArrayList<Data>();
                for (String topic: topics.keySet()) {
                    Data eof = null;
                    try {
                        eof = (Data) data.clone();
                        eof.setName(topic);
                        eofData.add(eof);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                agent.publish(eofData);
                agent.stop();
            }
        } else if (!stopped) {
            epService.getEPRuntime().sendEvent(data);
        }
    }

    protected Configuration getDefaultConfiguration() {
        configuration = new Configuration();
        configuration.getEngineDefaults().getExpression().setUdfCache(false);
        configuration.addImport("at.ac.tuwien.dsg.hcu.monitor.model.Data");
        configuration.addImport("at.ac.tuwien.dsg.hcu.monitor.model.MetaData");
        configuration.addEventType(Data.class);
        configuration.addEventType(MetaData.class);
        return configuration;
    }
    
    public void init() {
        String randomId = new BigInteger(130, new SecureRandom()).toString(32);
        init(getDefaultConfiguration(), DEFAULT_ENGINE_URI + "/" + randomId);
    }
    
    @SuppressWarnings("unchecked")
    public void init(Configuration configuration, String uri) {
        try {
            // initialize EP engine
            epService = EPServiceProviderManager.getProvider(uri, configuration);
            epService.initialize();
            // initialize event processors
            eventProcessors = new ArrayList<ProcessorInterface>();
            for (String topicName: topics.keySet()) {
                Map<String, Object> topicCfg = topics.get(topicName);
                String className = (String) topicCfg.get("event_processor");
                if (className!=null) {
                    ProcessorInterface processor = (ProcessorInterface)Class.forName(className)
                            .getConstructor()
                            .newInstance();
                    Map<String, Object> args = (Map<String, Object>) topicCfg.get("args");
                    processor.initiate(epService, this, topicName, args);
                    eventProcessors.add(processor);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void start() {
        stopped = false;
        init();
    }

    @Override
    public void stop() {
        epService.destroy();
        stopped = true;
    }

}
