package at.ac.tuwien.dsg.hcu.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import at.ac.tuwien.dsg.hcu.monitor.gridsim.GSMonitoringSimulation;
import at.ac.tuwien.dsg.hcu.monitor.impl.Broker;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.BrokerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.ProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Quality;
import at.ac.tuwien.dsg.hcu.monitor.model.Subscription;

public class Simulation {

    String title = "HCS Monitoring Simulation";
    HashMap<String, AgentInterface> agents;
    List<AgentInterface> producerAgents;
    List<BrokerInterface> brokers;
    
    RandomGenerator gen = new MersenneTwister(1000);

    @SuppressWarnings("unchecked")
    public boolean init(Map<String, Object> config) {
        
        if (config==null) {
            System.err.println("Null scenario configuration\n");
            return false;
        }
        
        title = (String) config.getOrDefault("title", title);
        boolean producerBasedQualityEngineEnabled = (boolean) config.getOrDefault("producer_based_quality_engine_enabled", false);
        Map<String, Object> globalConfig = new HashMap<String, Object>();
        globalConfig.put("producer_based_quality_engine_enabled", producerBasedQualityEngineEnabled);
        
        // create agents
        agents = new HashMap<String, AgentInterface>();
        producerAgents = new ArrayList<AgentInterface>();
        brokers = new ArrayList<BrokerInterface>();
        
        // TODO: make brokers configurable
        BrokerInterface broker = new Broker();
        broker.adjust(globalConfig);
        brokers.add(broker);
        
        List<HashMap<String, Object>> agentConfig = (List<HashMap<String, Object>>) config.get("monitoring_agents");
        for (HashMap<String, Object> cfg: agentConfig) {
            
            if (cfg.containsKey("enabled") && (Boolean)cfg.get("enabled")==false) {
                continue;
            }
            
            // get agent config
            String agentClassName = (String) cfg.get("class");
            String agentName = (String) cfg.getOrDefault("name", "AGENT_" + (agents.size()+1));
            Map<String, Object> adapterCfg = (HashMap<String, Object>) cfg.get("adapter");
            Map<String, Object> consumerCfg = (HashMap<String, Object>) cfg.get("consumer");
            List<Map<String, Object>> subscriptions = null;
            if (consumerCfg!=null) {
                subscriptions = (List<Map<String, Object>>) consumerCfg.get("subscriptions");
            }
            Map<String, Object> producerCfg = (HashMap<String, Object>) cfg.get("producer");
            List<Map<String, Object>> topics = (List<Map<String, Object>>) cfg.get("topics");
            if (agentClassName==null || (adapterCfg==null && consumerCfg==null)) {
                System.err.println("Unspecified agentClassName or (adapterCfg and consumerCfg)\n");
                return false;
            }
            
            // create agent
            Integer duplicate = (Integer) cfg.getOrDefault("duplicate", 1);
            for (int i=1; i<=duplicate; i++) {
                // prepare duplication
                String name = agentName;
                List<Map<String, Object>> subs = subscriptions;
                if (duplicate>1) {
                    name += "-" + i;
                    subs = duplicateSubscriptions(subscriptions, i);
                }
                // create
                AgentInterface agent = createAgent(agentClassName, name, broker, adapterCfg, consumerCfg, subs, producerCfg, topics, globalConfig);
                // add to agent list
                agents.put(agent.getName(), agent);
            }
            
        }
    
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> duplicateSubscriptions(List<Map<String, Object>> subscriptions, int i) {
        List<Map<String, Object>> subs = null;
        if (subscriptions!=null) {
            subs = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> subscription: subscriptions) {
                Map<String, Object> sub = new HashMap<String, Object>(subscription);
                Map<String, Object> quality = new HashMap<String, Object>((Map<String, Object>) sub.get("quality"));
                Double rate = (Double) quality.get("rate");
                // TODO: should not be hardcoded!
                if (rate!=null) {
                    // used in varying clients experiments
                    Double newRate = generateNormalRandomNumber(rate, rate/10);
                    //quality.put("rate", newRate);
                    // used in varying rates experiments
                    quality.put("rate", rate * i);
                    sub.put("quality", quality);
                }
                subs.add(sub);
            }
        }
        return subs;
    }
    
    private Double generateNormalRandomNumber(Double mean, Double stddev) {
        NormalDistribution dist = new NormalDistribution(gen, mean, stddev, 1.0E-9);
        return dist.sample();
    }
    
    @SuppressWarnings("unchecked")
    private AgentInterface createAgent(
            String agentClassName,
            String agentName,
            BrokerInterface broker,
            Map<String, Object> adapterCfg,
            Map<String, Object> consumerCfg,
            List<Map<String, Object>> subscriptions,
            Map<String, Object> producerCfg,
            List<Map<String, Object>> topics,
            Map<String, Object> globalConfig) {
        
        AgentInterface agent = null;
        try {
            
            // instantiate agent
            Class<?> agentClazz = Class.forName(agentClassName);
            agent = (AgentInterface) agentClazz.newInstance();
            agent.setName(agentName);
            agent.setBroker(broker);
            
            // instantiate adapter
            if (adapterCfg!=null) {
                String adapterClassName = (String) adapterCfg.get("class");
                Map<String, Object> _adapterCfg = (Map<String, Object>) adapterCfg.get("config");
                if (adapterClassName==null) {
                    System.err.println("Unspecified adapterClassName\n");
                    return null;
                }
                Class<?> adapterClazz = Class.forName(adapterClassName);
                AdapterInterface adapter = (AdapterInterface) adapterClazz.newInstance();
                adapter.adjust(_adapterCfg);
                agent.setAdapter(adapter);
            } 
            // instantiate consumer
            else if (consumerCfg!=null) {
                String consumerClassName = (String) consumerCfg.get("class");
                Map<String, Object> _consumerCfg = (Map<String, Object>) consumerCfg.get("config");
                if (consumerClassName==null) {
                    System.err.println("Unspecified consumerClassName\n");
                    return null;
                }
                Class<?> consumerClazz = Class.forName(consumerClassName);
                ConsumerInterface consumer = (ConsumerInterface) consumerClazz.newInstance();
                consumer.adjust(_consumerCfg);
                agent.setConsumer(consumer);
                // manage subscription
                // NOTE: config sequence matters, the producing agent must already be created before
                // TODO: support circular subscription
                if (subscriptions!=null) {
                    for (Map<String, Object> subscriptionCfg: subscriptions) {
                        String to = (String) subscriptionCfg.get("to");
                        AgentInterface destAgent = agents.get(to);
                        String topic = (String) subscriptionCfg.get("topic");
                        Map<String, Object> _subscriptionCfg = (Map<String, Object>) subscriptionCfg.get("config");
                        if (to==null || topic==null || destAgent==null) {
                            System.err.println(String.format("Invalid subscription, to:%s, topic:%s, skipping...\n", to, topic));
                            continue;
                        }
                        Subscription subscription = new Subscription();
                        subscription.setTopic(topic);
                        subscription.setConfig(_subscriptionCfg);
                        Map<String, Object> subscriptionQuality = (Map<String, Object>) subscriptionCfg.get("quality");
                        if (subscriptionQuality!=null) {
                            subscription.setQuality(new Quality(subscriptionQuality));
                        }
                        consumer.subscribeTo(destAgent.getProducer(), subscription);
                    }
                }
            }
            // instantiate producer
            if (producerCfg!=null) {
                String producerClassName = (String) producerCfg.get("class");
                Map<String, Object> _producerCfg = (Map<String, Object>) producerCfg.get("config");
                if (producerClassName==null) {
                    System.err.println("Unspecified producerClassName\n");
                    return null;
                }
                Class<?> producerClazz = Class.forName(producerClassName);
                ProducerInterface producer = (ProducerInterface) producerClazz.newInstance();
                producer.adjust(_producerCfg);
                agent.setProducer(producer);
                producerAgents.add(agent);
            }
            // add topics
            if (topics!=null) {
                for (Map<String, Object> topic: topics) {
                    String topicName = (String) topic.get("name");
                    Map<String, Object> topicConfig = (Map<String, Object>) topic.get("config");
                    if (topicName!=null) {
                        agent.addTopic(topicName, topicConfig);
                    }
                }
            }
            agent.adjust(globalConfig);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return agent;
    }
    
    public void start() {
        
        System.out.println("Running " + title + "...");
        // start simulation
        GSMonitoringSimulation.startSimulation(agents, brokers, false);
    }
}
