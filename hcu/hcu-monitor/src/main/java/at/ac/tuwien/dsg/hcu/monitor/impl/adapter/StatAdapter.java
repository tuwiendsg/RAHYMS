package at.ac.tuwien.dsg.hcu.monitor.impl.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.gridsim.GSWaker;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.StatisticInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import gridsim.GridSim;

public class StatAdapter extends BaseAdapter implements Wakeable {

    //protected Map<Integer, String> wakeIds = new HashMap<Integer, String>(); // [wakeId: topicName]
    protected Map<String, Map<String, Object>> lastValues; // [[topic: [agent: value, ...]], ...]
    
    @Override
    public void start() {
        lastValues = new HashMap<String, Map<String, Object>>();
        for (String topicName: topics.keySet()) {
            lastValues.put(topicName, new HashMap<String, Object>());
        }
        Double samplingRate = (Double) config.get("sampling_rate");
        GSWaker.getInstance().wakeMeAfter(this, samplingRate);
        /*
        for (String topicName: topics.keySet()) {
            Map<String, Object> topicCfg = topics.get(topicName);
            Double samplingRate = (Double) topicCfg.get("sampling_rate");
            int wakeId = GSWaker.getInstance().wakeMeAfter(this, samplingRate);
            wakeIds.put(wakeId, topicName);
        }*/
    }

    @Override
    public void stop() {
    }

    @Override
    public List<Data> getData() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void wake(int wakeId) {
        boolean allStopped = true;
        List<Data> data = new ArrayList<Data>();
        for (String topicName: topics.keySet()) {
            //String topicName = wakeIds.get(wakeId);
            //wakeIds.remove(wakeId);
            Map<String, Object> topicLastValues = lastValues.get(topicName);
            Map<String, Object> topicCfg = topics.get(topicName);
            List<String> agents = (List<String>) topicCfg.get("agents");
            String propertyName = (String) topicCfg.get("property");
            Boolean resetOnSampling = (Boolean) topicCfg.getOrDefault("reset_on_sampling", false);
            Object defaultValue = topicCfg.get("default");
            for (String agentName: agents) {
                // get the requested statistic entities
                StatisticInterface statEntity = null;
                if (agentName.equals("broker")) {
                    statEntity = (StatisticInterface) this.agent.getBroker();
                    if (!this.agent.getBroker().isStopping()) {
                        allStopped = false;
                    }
                } else {
                    AgentInterface agent = this.agent.getBroker().getAgent(agentName);
                    if (!agent.isRunning()) {
                        continue;
                    } else {
                        allStopped = false;
                        statEntity = (StatisticInterface)agent;
                    }
                }
                Object lastValue = topicLastValues.get(agentName);
                Object propertyValue = statEntity.getProperty(propertyName);
                topicLastValues.put(agentName, propertyValue);
                if (resetOnSampling) {
                    //statEntity.resetProperty(propertyName);
                    if (lastValue!=null) {
                        if (propertyValue instanceof Double) {
                            propertyValue = (Double)propertyValue - (Double)lastValue;
                        } else if (propertyValue instanceof Integer) {
                            propertyValue = (Integer)propertyValue - (Integer)lastValue;
                        }
                    }
                }
                // create data
                Data adata = new Data();
                if (propertyValue==null && defaultValue!=null) {
                    propertyValue = defaultValue;
                }
                adata.setName(topicName);
                adata.setValue(propertyValue);
                adata.setMetaData("agent_name", agentName);
                adata.setMetaData("time", GridSim.clock()); // TODO: should get the clock from somewhere
                data.add(adata);
            }
            lastValues.put(topicName, topicLastValues);
        }
        agent.publish(data);
        // wake next time
        if (!allStopped) {
            Double samplingRate = (Double) config.get("sampling_rate");
            GSWaker.getInstance().wakeMeAfter(this, samplingRate);
            /*
            Double samplingRate = (Double) topicCfg.get("sampling_rate");
            int newWakeId = GSWaker.getInstance().wakeMeAfter(this, samplingRate);
            wakeIds.put(newWakeId, topicName); */
        } else {
            this.agent.stop();
        }
    }
}
