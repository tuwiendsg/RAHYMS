package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;

import scu.cloud.generator.TaskWithOptimizationGenerator;
import scu.cloud.generator.TaskGenerator;
import scu.common.model.Task;
import scu.common.model.optimization.OptimizationObjective;
import scu.common.model.optimization.TaskWithOptimization;
import scu.util.ConfigJson;


public class TestTaskWithOptimizationGenerator {

    public static void main(String[] args) {

        try {

            ConfigJson config = new ConfigJson("task-generator.json");

            OptimizationObjective objective = new OptimizationObjective();
            objective.setWeight("skill", 1.0)
                     .setWeight("connectedness", 1.0)
                     .setWeight("cost", 1.0)
                     .setWeight("time", 1.0);
            
            // generate services
            TaskWithOptimizationGenerator taskGen = new TaskWithOptimizationGenerator(config);
            ArrayList<TaskWithOptimization> rootTasks = new ArrayList<TaskWithOptimization>(); 
            ArrayList<TaskWithOptimization> tasks = taskGen.generate(objective, rootTasks);
            
            // dump
            System.out.println("====== TASKS =======");
            for (TaskWithOptimization t : tasks) {
                System.out.println(t);
            }
            System.out.println("====== ROOT TASKS =======");
            for (TaskWithOptimization t : rootTasks) {
                System.out.println(t);
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
