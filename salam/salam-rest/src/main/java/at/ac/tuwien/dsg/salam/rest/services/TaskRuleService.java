package at.ac.tuwien.dsg.salam.rest.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import at.ac.tuwien.dsg.salam.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.salam.rest.resource.Task;
import at.ac.tuwien.dsg.salam.rest.resource.Task.SeverityLevel;
import at.ac.tuwien.dsg.salam.rest.resource.TaskRule;
import at.ac.tuwien.dsg.salam.rest.resource.TaskRule.Condition;
import at.ac.tuwien.dsg.salam.rest.resource.TaskRule.Consequence;
import at.ac.tuwien.dsg.salam.util.ConfigJson;

public class TaskRuleService {

    private static ConcurrentMap<Integer, TaskRule> rules = new ConcurrentHashMap<Integer, TaskRule>();
    private static Integer _lastId = 0;

    public TaskRuleService() {
    }
    
    public List<TaskRule> getTaskRules(int page, int pageSize) {
        final List<TaskRule> slice = new ArrayList<TaskRule>( pageSize );
        final Iterator<TaskRule> iterator = rules.values().iterator();
        for( int i = 0; slice.size() < pageSize && iterator.hasNext(); ) {
            if( ++i > ( ( page - 1 ) * pageSize ) ) {
                slice.add(iterator.next());
            }
        }
        return slice;
    }
    
    public TaskRule getTaskRuleById(Integer id) {
        TaskRule rule = rules.get(id);
        if(rule == null) {
            throw new NotFoundException();
        }
        return rule;
    }

    public Integer addTaskRule(String tag, SeverityLevel severity, List<String> services, Double load) {
        Integer id = _lastId++;
        TaskRule rule = new TaskRule(id, tag, severity, services, load);
        rules.put(id, rule);
        return id;
    }

    public void removeTaskRule(Integer id) {
        if (rules.remove(id) == null) {
            throw new NotFoundException();
        }
    }
    
    public Consequence findRule(Condition condition) {
        Consequence result = null;
        for (TaskRule rule: rules.values()) {
            if (rule.getCondition().match(condition)) {
                result = rule.getConsequence();
                break;
            }
        }
        return result;
    }
    
    public Consequence findRule(String tag, SeverityLevel severity) {
        Condition condition = new Condition(tag, severity);
        return findRule(condition);
    }

    public void populate(ConfigJson config) {
        JSONArray rules = config.getRoot().getJSONArray("rules");
        for(int i=0; i<rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            JSONObject conditions = rule.getJSONObject("conditions");
            JSONObject consequences = rule.getJSONObject("consequences");
            String tag = conditions.getString("tag");
            String severity = conditions.getString("severity");
            double loadFactor = consequences.getDouble("load_factor");
            double load = 1.0 * loadFactor;
            JSONArray servicesRequired = consequences.getJSONArray("services_required");
            List<String> services = new ArrayList<String>();
            for (int j=0; j<servicesRequired.length(); j++) {
                String f = servicesRequired.getString(j);
                services.add(f);
            }
            addTaskRule(tag, Task.SeverityLevel.valueOf(severity), services, load);
        }
    }
}
