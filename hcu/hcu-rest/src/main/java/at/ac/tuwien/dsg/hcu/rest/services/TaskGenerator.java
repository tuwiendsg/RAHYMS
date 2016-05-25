package at.ac.tuwien.dsg.hcu.rest.services;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.Role;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;
import at.ac.tuwien.dsg.hcu.rest.resource.Task;
import at.ac.tuwien.dsg.hcu.rest.resource.TaskRule.Consequence;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;

public class TaskGenerator {

    private TaskRuleService ruleService = null;
    
    public TaskGenerator(ConfigJson config) {
        ruleService = new TaskRuleService();
        ruleService.populate(config);
    }

    public at.ac.tuwien.dsg.hcu.common.model.Task createHCUTask(Task t) {
        return createHCUTask(t, null);
    }

    public at.ac.tuwien.dsg.hcu.common.model.Task createHCUTask(Task t, List<String> includedServices) {

        // find task generating rule
        Consequence consequence = ruleService.findRule(t.getTag(), t.getSeverity());

        // can't find matching rule
        if (consequence==null) return null;

        // define task load
        double loadFactor = consequence.getLoadFactor();
        double load = 1.0 * loadFactor;

        // create task
        at.ac.tuwien.dsg.hcu.common.model.Task hcuTask;
        hcuTask = new at.ac.tuwien.dsg.hcu.common.model.Task(t.getName(), t.getContent(), load);
        hcuTask.setSpecification(new Specification());

        // define task role
        List<String> servicesRequired = consequence.getRequiredServices();
        for (String f: servicesRequired) {
            if (includedServices==null || includedServices.contains(f)) {
                Role r = new Role(new Functionality(f), new Specification());
                hcuTask.addUpdateRole(r);
            }
        }

        return hcuTask;
    }
}
