package at.ac.tuwien.dsg.hcu.monitor.impl.consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.hcu.monitor.impl.consumer.StateBasedMonitoringConsumer.StateHistory.HistoryEntry;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;
import at.ac.tuwien.dsg.hcu.util.Util;

public class StateBasedMonitoringConsumer extends EventBasedMonitoringConsumer implements Wakeable {
    
    protected static final String DEFAULT_STATE_ENGINE_URI = "dsg.tuwien.ac.at/hcu/monitor/impl/consumer/StateBasedMonitoringConsumer";
    protected static Waker waker;
    
    protected List<Transition> transitions;
    protected Map<String, StateHistory> history; // [id: history, ...]
    protected Map<String, String> currentStates; // [id: state, ...]
    protected Double windowSize = 0.0;
    protected int lastWakeId = 0;
    
    public StateBasedMonitoringConsumer() {
        super();
        history = new HashMap<String, StateHistory>();
        transitions = new ArrayList<Transition>();
        currentStates = new HashMap<String, String>();
    }
    
    public static void setWaker(Waker _waker) {
        waker = _waker;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void adjust(HashMap<String, Object> config) {
        List<Map<String, String>> transitionsCfg = (List<Map<String, String>>) config.get("transitions");
        if (transitionsCfg!=null) {
            for (Map<String, String> transition: transitionsCfg) {
                Transition t = new Transition(
                        transition.get("from"),
                        transition.get("event"),
                        transition.get("to"));
                transitions.add(t);
            }
        }
        windowSize = (Double) config.getOrDefault("window", 0.0);
    }
    
    public void init() {
        super.init();
        initListener();
    }

    protected void initListener() {
        
        String expression = "SELECT value, metaData FROM Data";
        
        epService.getEPAdministrator()
                .createEPL(expression)
                .addListener(new StateListener());
    }
    
    
    protected class StateListener implements UpdateListener {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            int i = 0;
            for (EventBean event: newEvents) {
                String stateEvent = (String) ((Map)event.getUnderlying()).get("value");
                MetaData metaData = (MetaData) ((Map)event.getUnderlying()).get("metaData");
                String id = metaData.getId();
                Double time = metaData.getTime();
                //Util.log().warning(String.format("StateListener [%d]: event=%s, id=%s, time=%f", i++, stateEvent, id, time));
                performTransition(id, stateEvent, time);
            }
        }
        
    }
    
    protected void performTransition(String id, String event, Double time) {

        if (windowSize>0 && waker!=null && lastWakeId==0) {
            lastWakeId = waker.wakeMeAfter(this, windowSize);
            // TODO: start at 0 or t
        }
        
        
        String prevState = currentStates.get(id);
        Transition transition = findTransition(prevState, event);
        
        if (transition!=null) {
            
            currentStates.put(id, transition.to);

            // get history for this unit
            StateHistory unitHistory = history.get(id);
            if (unitHistory==null) {
                unitHistory = new StateHistory();
                history.put(id, unitHistory);
            }

            if (prevState!=null) {
                // update history on the previous state
                HistoryEntry historyEntry = unitHistory.getEntries().getLast();
                historyEntry.setEnd(time);
            }
            
            // update history on the new state
            unitHistory.add(transition.to, time);
            
            // calculate metrics
            calculateStateMetrics(id, transition.to, time);
        }
        
//        System.out.println(currentStates);
//        System.out.println(history);
    }
    
    @SuppressWarnings("unchecked")
    private void calculateStateMetrics(String id, String newState, Double time) {
        for (String topic: topics.keySet()) {
            Map<String, Object> topicCfg = topics.get(topic);
            String function = (String) topicCfg.get("function");
            Method method = Util.getMethod(function);
            Map<String, Object> args = (Map<String, Object>) topicCfg.get("args");
            try {
                List<Data> results = (List<Data>)method.invoke(null, topic, id, newState, time, history, args);
                if (results!=null) {
                    agent.getProducer().publish(results);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean equalsOrNull(Object obj1, Object obj2) {
        if (obj1!=null && obj2!=null) {
            return obj1.equals(obj2);
        } else {
            return obj1==null && obj2==null;
        }
    }
    
    protected Transition findTransition(String currentState, String event) {
        for (Transition t: transitions) {
            if (equalsOrNull(t.from, currentState) && equalsOrNull(t.event, event)) {
                return t;
            }
        }
        return null;
    }

    protected class Transition {
        String from;
        String event;
        String to;
        
        public Transition(String from, String event, String to) {
            this.from = from;
            this.event = event;
            this.to = to;
        }
    }

    @Override
    public void wake(int wakeId) {
        if (wakeId==lastWakeId) {
            
        }
        // else, should not occur, but just ignore the wake event
        
        lastWakeId = waker.wakeMeAfter(this, windowSize);
    }
    
    public class StateHistory {
        
        private LinkedList<HistoryEntry> entries;
        
        public StateHistory() {
            entries = new LinkedList<HistoryEntry>();
        }
        
        public void add(String state, Double start) {
            add(state, start, 0.0);
        }
        
        public void add(String state, Double start, Double end) {
            entries.add(new HistoryEntry(state, start, end));
        }
        
        public LinkedList<HistoryEntry> getEntries() {
            return entries;
        }
        
        @Override
        public String toString() {
            return entries.toString();
        }

        public class HistoryEntry {
            private String state;
            private Double start = 0.0;
            private Double end = 0.0;
            public HistoryEntry(String state, Double start, Double end) {
                this.state = state;
                this.start = start;
                this.end = end;
            }
            public Double getStart() {
                return start;
            }
            public void setStart(Double start) {
                this.start = start;
            }
            public Double getEnd() {
                return end;
            }
            public void setEnd(Double end) {
                this.end = end;
            }
            public String getState() {
                return state;
            }
            public void setState(String state) {
                this.state = state;
            }
            @Override
            public String toString() {
                return state + ":" + start + "-" + end;
            }
            
        }
    }
    
}
