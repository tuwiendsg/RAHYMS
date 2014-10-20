package at.ac.tuwien.dsg.salam.rest.services;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import at.ac.tuwien.dsg.salam.common.model.Functionality;
import at.ac.tuwien.dsg.salam.common.model.Role;
import at.ac.tuwien.dsg.salam.common.sla.Specification;
import at.ac.tuwien.dsg.salam.rest.resource.Task;
import at.ac.tuwien.dsg.salam.rest.resource.TaskRule.Consequence;
import at.ac.tuwien.dsg.salam.util.ConfigJson;

public class TaskGenerator {

    private TaskRuleService ruleService = null;
    
    public TaskGenerator(ConfigJson config) {
        ruleService = new TaskRuleService();
        ruleService.populate(config);
    }

    public at.ac.tuwien.dsg.salam.common.model.Task createSalamTask(Task t) {
        return createSalamTask(t, null);
    }

    public at.ac.tuwien.dsg.salam.common.model.Task createSalamTask(Task t, List<String> includedServices) {

        // find task generating rule
        Consequence consequence = ruleService.findRule(t.getTag(), t.getSeverity());

        // can't find matching rule
        if (consequence==null) return null;

        // define task load
        double loadFactor = consequence.getLoadFactor();
        double load = 1.0 * loadFactor;

        // create task
        at.ac.tuwien.dsg.salam.common.model.Task salamTask;
        salamTask = new at.ac.tuwien.dsg.salam.common.model.Task(t.getName(), t.getContent(), load);
        salamTask.setSpecification(new Specification());

        // define task role
        List<String> servicesRequired = consequence.getRequiredServices();
        for (String f: servicesRequired) {
            if (includedServices==null || includedServices.contains(f)) {
                Role r = new Role(new Functionality(f), new Specification());
                salamTask.addUpdateRole(r);
            }
        }

        return salamTask;
    }
}
