package scu.cloud.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scu.common.model.ComputingElement;
import scu.common.model.Functionality;
import scu.common.model.HumanComputingElement;
import scu.common.model.Role;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.common.sla.Objective;
import scu.common.sla.Specification;
import scu.util.ConfigJson;

public class TaskGeneratorJson {

    private Logger logger = Logger.getLogger("Generator");
    private JSONObject configRoot = null;
    
    // distribution generators
    private Hashtable<String, Object> distOccurances;
    private Hashtable<String, Object> distLoads;
    private Hashtable<String, Object> distValues;
    private Hashtable<String, UniformRealDistribution> distToHaves;
    
    private int seed;
    private long lastTaskId;
    Hashtable<String, JSONObject> taskTypes;

    public TaskGeneratorJson(ConfigJson config) {
        this.configRoot = config.getRoot();
        distOccurances = new Hashtable<String, Object>();
        distLoads = new Hashtable<String, Object>();
        distValues = new Hashtable<String, Object>();
        distToHaves = new Hashtable<String, UniformRealDistribution>();
        taskTypes = new Hashtable<String, JSONObject>();
        lastTaskId = 0;
    }
    
    public ArrayList<Task> generate(ArrayList<Task> rootTasks) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        
        // init
        ArrayList<Task> tasks = new ArrayList<Task>();

        // get config
        seed = configRoot.getInt("seed");
        JSONArray taskTypeCfgs = configRoot.getJSONArray("taskTypes");
        for (int i=0; i<taskTypeCfgs.length(); i++) {
            taskTypes.put(taskTypeCfgs.getJSONObject(i).getString("name"), 
                    taskTypeCfgs.getJSONObject(i));
        }
        
        // generate tasks
        for (JSONObject curTaskTypeCfg: taskTypes.values()) {
            if (curTaskTypeCfg.getBoolean("isRootTask")) {
                tasks = createTask(curTaskTypeCfg, null, "", 1.0, rootTasks);
            }
        }
        
        return tasks;
    }
    
    private ArrayList<Task> createTask(JSONObject taskTypeCfg, Task parent,
            String parentPath, double probabilityToHave,
            ArrayList<Task> rootTasks) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {

        // init
        ArrayList<Task> tasks = new ArrayList<Task>();
        
        // get config
        String name = taskTypeCfg.getString("name");
        String description = taskTypeCfg.getString("description");
        String currentPath = parentPath + "/" + name;

        // calc occurance
        long occurance = 0;
        if (taskTypeCfg.getBoolean("isRootTask")) {
            logger.info("Generating " + name + " tasks...");
            // init occurance distribution
            if (distOccurances.get(name)==null) {
                JSONObject occuranceCfg = taskTypeCfg.getJSONObject("tasksOccurance");
                String clazz = GeneratorUtil.DISTRIBUTION_PACKAGE + occuranceCfg.getString("class");
                JSONArray params = occuranceCfg.getJSONArray("params");
                Object dist = GeneratorUtil.createValueDistribution(clazz, params, seed++);
                distOccurances.put(name, dist);
            }
            Object curDistOccurance = distOccurances.get(name);
            occurance = Math.round((double)GeneratorUtil.sample(curDistOccurance, null));
        } else {
            if (distToHaves.get(currentPath)==null) {
                UniformRealDistribution dist = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
                distToHaves.put(currentPath, dist);
            }
            UniformRealDistribution distToHave = distToHaves.get(currentPath);
            if (GeneratorUtil.shouldHave(distToHave, probabilityToHave)) {
                occurance = 1;
            } else {
                occurance = 0;
            }
        }
        
        // init load distribution
        if (distLoads.get(name)==null) {
            JSONObject loadCfg = taskTypeCfg.getJSONObject("load");
            String clazz = GeneratorUtil.DISTRIBUTION_PACKAGE + loadCfg.getString("class");
            JSONArray params = loadCfg.getJSONArray("params");
            Object dist = GeneratorUtil.createValueDistribution(clazz, params, seed++);
            distLoads.put(name, dist);
        }

        // create task
        Object curDistLoad = distLoads.get(name);
        for (int i=0; i<occurance; i++) {
            
            double load = (double)GeneratorUtil.sample(curDistLoad, null);
            Task task = new Task(++lastTaskId, load, parent, name, description);
            if (parent!=null) parent.addSubTask(task);
            if (taskTypeCfg.getBoolean("isRootTask")) {
                rootTasks.add(task);
            }
            tasks.add(task);
            
            // create roles
            JSONArray rolesCfg = taskTypeCfg.getJSONArray("roles");
            createRoles(rolesCfg, task, currentPath);
            
            // create sub tasks
            JSONArray subTaskCfgs = taskTypeCfg.getJSONArray("subTaskTypes");
            for (int j=0; j<subTaskCfgs.length(); j++) {
                JSONObject curSubTaskCfg = subTaskCfgs.getJSONObject(j);
                String subTaskName = curSubTaskCfg.getString("name");
                double subTaskPToHave = curSubTaskCfg.getDouble("probabilityToHave");
                logger.info("Generating sub-tasks...");
                ArrayList<Task> subTasks = createTask(taskTypes.get(subTaskName), 
                        task, currentPath, subTaskPToHave, rootTasks);
                tasks.addAll(subTasks);
            }
        }
        
        return tasks;
    }
    
    private void createRoles(JSONArray rolesCfg, Task task, String parentPath) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        
        // iterate roles
        for (int i=0; i<rolesCfg.length(); i++) {

            // get role config
            JSONObject roleCfg = rolesCfg.getJSONObject(i);
            String func = roleCfg.getString("functionality");
            double pRoleToHave = roleCfg.getDouble("probabilityToHave");
            JSONArray specCfg = roleCfg.getJSONArray("specification");
            String currentRolePath = parentPath + "/{r}" + func;
            
            // init dist
            if (distToHaves.get(currentRolePath)==null) {
                UniformRealDistribution dist = new UniformRealDistribution(
                        new MersenneTwister(seed++), 0, 1);
                distToHaves.put(currentRolePath, dist);
            }
            UniformRealDistribution distRoleToHave = distToHaves.get(currentRolePath);
            
            if (GeneratorUtil.shouldHave(distRoleToHave, pRoleToHave)) {
                
                // create role
                Role role = new Role(new Functionality(func));
                task.addUpdateRole(role);
                
                // create spec
                Specification spec = new Specification();
                role.setSpecification(spec);
                
                // iterate spec
                for (int j=0; j<specCfg.length(); j++) {
                    
                    // get spec's objective config
                    JSONObject curSpecCfg = specCfg.getJSONObject(j);
                    String type = curSpecCfg.getString("type");
                    String name = curSpecCfg.getString("name");
                    double pToHave = curSpecCfg.getDouble("probabilityToHave");
                    JSONObject valueCfg = curSpecCfg.getJSONObject("value");
                    String clazz = GeneratorUtil.DISTRIBUTION_PACKAGE + valueCfg.getString("class");
                    JSONArray params = valueCfg.getJSONArray("params");
                    JSONObject mapping = null;
                    if (valueCfg.has("mapping")) mapping = valueCfg.getJSONObject("mapping");
                    String comparator = curSpecCfg.getString("comparator");
                    String currentObjectivePath = currentRolePath + "/{o}" + name;
                    
                    // init dist
                    if (distToHaves.get(currentObjectivePath)==null) {
                        UniformRealDistribution dist = new UniformRealDistribution(
                                new MersenneTwister(seed++), 0, 1);
                        distToHaves.put(currentObjectivePath, dist);
                    }
                    UniformRealDistribution distObjToHave = distToHaves.get(currentObjectivePath);
                    if (distValues.get(currentObjectivePath)==null) {
                        Object dist = GeneratorUtil.createValueDistribution(clazz, params, seed++);
                        distValues.put(currentObjectivePath, dist);
                    }
                    Object distObjValue = distValues.get(currentObjectivePath);
                    
                    if (GeneratorUtil.shouldHave(distObjToHave, pToHave)) {
                        // create objective
                        Object value = GeneratorUtil.sample(distObjValue, mapping);
                        Comparator comparatorObj = (Comparator)Class.forName(comparator).newInstance();
                        Objective objective = new Objective(name, value, comparatorObj);
                        spec.addObjective(objective);
                    }
                }
                
                
            }
            
        }
    }
    
}
