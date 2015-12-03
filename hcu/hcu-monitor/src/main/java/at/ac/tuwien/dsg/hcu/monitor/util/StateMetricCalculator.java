package at.ac.tuwien.dsg.hcu.monitor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.hcu.monitor.impl.consumer.StateBasedMonitoringConsumer.StateHistory;
import at.ac.tuwien.dsg.hcu.monitor.impl.consumer.StateBasedMonitoringConsumer.StateHistory.HistoryEntry;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;

public class StateMetricCalculator {

    public static List<Data> duration(
            String topic,
            String id, 
            String newState, 
            Double time, 
            Map<String, StateHistory> history, 
            Map<String, Object> args) {
        
        List<Data> results = new ArrayList<Data>();
        boolean returnAllUnits = (boolean) args.getOrDefault("return_all_units", false);
        Double window = (Double) args.getOrDefault("window", 0.0);
        String targetedState = (String) args.get("state");
        
        for (String unitId: history.keySet()) {
            
            StateHistory unitHistory = history.get(unitId);
            
            if (!returnAllUnits && !unitId.equals(id)) {
                // we return duration only for unit with id 
                continue;
            }

            Map<String, Double> stateDuration = new HashMap<String, Double>();
            for (HistoryEntry entry: unitHistory.getEntries()) {
                if (targetedState!=null && !entry.getState().equals(targetedState)) {
                    continue;
                }
                Double currentDuration = stateDuration.getOrDefault(entry.getState(), 0.0);
                
                Double d = 0.0;
                Double endTime = entry.getEnd()>0.0 ? entry.getEnd() : time;
                if (window>0.0) {
                    // check windowing
                    if (endTime<(time-window)) {
                        // fully before the window, ignore
                    } else if (entry.getStart()<(time-window)) {
                        // partially before the window
                        d = endTime - (time-window);
                    } else {
                        // fully inside the window
                        d = endTime - entry.getStart();
                    }
                } else {
                    // no windowing
                    d = endTime - entry.getStart();
                }
                
                currentDuration += d;
                if (currentDuration>0.0) {
                    stateDuration.put(entry.getState(), currentDuration);
                }
            }

            // create result data
            for (String state: stateDuration.keySet()) {
                Data data = createStateData(topic, stateDuration.get(state), time, unitId, state);
                results.add(data);
            }
        }
        
        return results;
    }
    
    public static Data createStateData(String topic, Object value, Double time, String id, String state) {
        Data result = new Data();
        result.setName(topic);
        result.setValue(value);
        result.setMetaData("id", id);
        result.setMetaData("time", time);
        result.setMetaData("state", state);
        return result;
    }

    public static List<Data> countUnitsInState (
            String topic,
            String id, 
            String newState, 
            Double time, 
            Map<String, StateHistory> history, 
            Map<String, Object> args) {
        
        List<Data> results = new ArrayList<Data>();
        String targetedState = (String) args.get("state");
        
        Integer count = 0;
        for (String unitId: history.keySet()) {
            
            StateHistory unitHistory = history.get(unitId);
            HistoryEntry entry = unitHistory.getEntries().getLast();
            if (entry.getState().equals(targetedState)) {
                count++;
            }
            
        }
        
        // create result data
        Data data = createStateData(topic, count, time, null, targetedState);
        results.add(data);

        return results;
    }
}
