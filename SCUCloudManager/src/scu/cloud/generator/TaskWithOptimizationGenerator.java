package scu.cloud.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONException;

import scu.common.model.Task;
import scu.common.model.optimization.OptimizationObjective;
import scu.common.model.optimization.TaskWithOptimization;
import scu.util.ConfigJson;

public class TaskWithOptimizationGenerator extends TaskGenerator {

    public TaskWithOptimizationGenerator(ConfigJson config) {
        super(config);
    }
    
    public ArrayList<TaskWithOptimization> generate(OptimizationObjective objective, 
            ArrayList<TaskWithOptimization> rootTasks) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        
        ArrayList<Task> roots = new ArrayList<Task>(); 
        ArrayList<Task> tasks = super.generate(roots);
        
        ArrayList<TaskWithOptimization> optTasks = new ArrayList<TaskWithOptimization>();
        
        // copy and set optimization objective
        for (Task t: tasks) {
            TaskWithOptimization ot = new TaskWithOptimization(t);
            ot.setOptObjective(objective);
            optTasks.add(ot);
            if (roots.contains(t)) rootTasks.add(ot);
        }
        
        return optTasks;
    }
    

}
