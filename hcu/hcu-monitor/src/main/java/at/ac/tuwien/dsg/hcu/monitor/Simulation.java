package at.ac.tuwien.dsg.hcu.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.gridsim.GSMonitoringSimulation;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class Simulation {

    String title = "HCS Monitoring Simulation";
    HashMap<String, MonitoringAgentInterface> agents;
    List<MonitoringAgentInterface> producerAgents;
    
    @SuppressWarnings("unchecked")
    public boolean init(Map<String, Object> config) {
        
        if (config==null) {
            System.err.println("Null scenario configuration\n");
            return false;
        }
        
        title = (String) config.getOrDefault("title", title);
        
        // create agents
        try {
            agents = new HashMap<String, MonitoringAgentInterface>();
            producerAgents = new ArrayList<MonitoringAgentInterface>();
            List<HashMap<String, Object>> agentConfig = (List<HashMap<String, Object>>) config.get("monitoring_agents");
            for (HashMap<String, Object> cfg: agentConfig) {
                
                // get agent config
                String agentClassName = (String) cfg.get("class");
                HashMap<String, Object> adapterCfg = (HashMap<String, Object>) cfg.get("adapter");
                HashMap<String, Object> consumerCfg = (HashMap<String, Object>) cfg.get("consumer");
                HashMap<String, Object> producerCfg = (HashMap<String, Object>) cfg.get("producer");
                List<HashMap<String, Object>> topics = (List<HashMap<String, Object>>) cfg.get("topics");
                if (agentClassName==null || (adapterCfg==null && consumerCfg==null)) {
                    System.err.println("Unspecified agentClassName or (adapterCfg and consumerCfg)\n");
                    return false;
                }
                
                // instantiate agent
                Class<?> agentClazz = Class.forName(agentClassName);
                MonitoringAgentInterface agent = (MonitoringAgentInterface) agentClazz.newInstance();
                agent.setName((String) cfg.getOrDefault("name", "AGENT_" + (agents.size()+1)));
                // instantiate adapter
                if (adapterCfg!=null) {
                    String adapterClassName = (String) adapterCfg.get("class");
                    HashMap<String, Object> _adapterCfg = (HashMap<String, Object>) adapterCfg.get("config");
                    if (adapterClassName==null) {
                        System.err.println("Unspecified adapterClassName\n");
                        return false;
                    }
                    Class<?> adapterClazz = Class.forName(adapterClassName);
                    MonitoringAdapterInterface adapter = (MonitoringAdapterInterface) adapterClazz.newInstance();
                    adapter.adjust(_adapterCfg);
                    agent.setAdapter(adapter);
                } 
                // instantiate consumer
                else if (consumerCfg!=null) {
                    String consumerClassName = (String) consumerCfg.get("class");
                    HashMap<String, Object> _consumerCfg = (HashMap<String, Object>) consumerCfg.get("config");
                    if (consumerClassName==null) {
                        System.err.println("Unspecified consumerClassName\n");
                        return false;
                    }
                    Class<?> consumerClazz = Class.forName(consumerClassName);
                    MonitoringConsumerInterface consumer = (MonitoringConsumerInterface) consumerClazz.newInstance();
                    consumer.adjust(_consumerCfg);
                    agent.setConsumer(consumer);
                    // manage subscription
                    // NOTE: config sequence matters, the producing agent must already be created before
                    ArrayList<HashMap<String, Object>> subscriptions = (ArrayList<HashMap<String, Object>>) consumerCfg.get("subscriptions");
                    if (subscriptions!=null) {
                        for (HashMap<String, Object> subscriptionCfg: subscriptions) {
                            String to = (String) subscriptionCfg.get("to");
                            MonitoringAgentInterface destAgent = agents.get(to);
                            String topic = (String) subscriptionCfg.get("topic");
                            HashMap<String, Object> _subscriptionCfg = (HashMap<String, Object>) subscriptionCfg.get("config");
                            if (to==null || topic==null || destAgent==null) {
                                System.err.println("Invalid subscription\n");
                                return false;
                            }
                            Subscription subscription = new Subscription();
                            subscription.setTopic(topic);
                            subscription.setConfig(_subscriptionCfg);
                            consumer.subscribeTo(destAgent.getProducer(), subscription);
                        }
                    }
                }
                // instantiate producer
                if (producerCfg!=null) {
                    String producerClassName = (String) producerCfg.get("class");
                    HashMap<String, Object> _producerCfg = (HashMap<String, Object>) producerCfg.get("config");
                    if (producerClassName==null) {
                        System.err.println("Unspecified producerClassName\n");
                        return false;
                    }
                    Class<?> producerClazz = Class.forName(producerClassName);
                    MonitoringProducerInterface producer = (MonitoringProducerInterface) producerClazz.newInstance();
                    producer.adjust(_producerCfg);
                    agent.setProducer(producer);
                    producerAgents.add(agent);
                }
                // add topics
                if (topics!=null) {
                    for (HashMap<String, Object> topic: topics) {
                        String topicName = (String) topic.get("name");
                        HashMap<String, Object> topicConfig = (HashMap<String, Object>) topic.get("config");
                        if (topicName!=null) {
                            agent.addTopic(topicName, topicConfig);
                        }
                    }
                }
                // add to agent list
                agents.put(agent.getName(), agent);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    
    public void start() {
        
        System.out.println("Running " + title + "...");
        // start all agents
        for (MonitoringAgentInterface agent: agents.values()) {
            agent.start();
        }
        // start simulation
        GSMonitoringSimulation.startSimulation(producerAgents, false);
    }
}