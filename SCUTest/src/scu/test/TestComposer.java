package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONException;

import scu.cloud.discoverer.Discoverer;
import scu.cloud.generator.ServiceGenerator;
import scu.cloud.generator.TaskWithOptimizationGenerator;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.cloud.monitor.AvailabilityMonitor;
import scu.common.model.Role;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.common.model.optimization.OptimizationObjective;
import scu.common.model.optimization.TaskWithOptimization;
import scu.composer.Composer;
import scu.composer.model.Solution;
import scu.util.ConfigJson;


public class TestComposer {

    public static void main(String[] args) {

        try {

            ConfigJson config = new ConfigJson("task-generator-no-subs.json");

            ConfigJson svcConfig = new ConfigJson("service-generator.json");
            ConfigJson metricConfig = new ConfigJson("metric-generator.json");

            // generate services
            ServiceGenerator svcGen = new ServiceGenerator(svcConfig);
            ArrayList<Service> services;
            services = svcGen.generate();

            // register services
            ServiceManagerOnMemory manager = new ServiceManagerOnMemory();
            for (Service service : services) {
                manager.registerService(service);
                //System.out.println(service);
            }

            // init availability metric
            AvailabilityMonitor.initGenerator(metricConfig);

            // generate task with optimization objective
            OptimizationObjective objective = new OptimizationObjective();
            objective.setWeight("skill", 1.0)
                     .setWeight("connectedness", 1.0)
                     .setWeight("cost", 1.0)
                     .setWeight("time", 1.0);
            TaskWithOptimizationGenerator taskGen = new TaskWithOptimizationGenerator(config);
            ArrayList<TaskWithOptimization> rootTasks = new ArrayList<TaskWithOptimization>(); 
            ArrayList<TaskWithOptimization> tasks = taskGen.generate(objective, rootTasks);
            
            // list task
            for (TaskWithOptimization t : tasks) {
                System.out.println(t);
            }

            // init composer
            Discoverer discoverer = new Discoverer(manager);
            Composer composer = new Composer("composer.properties", manager, discoverer);

            for (TaskWithOptimization t : tasks) {
                Solution solution = composer.compose(t);
                System.out.println(t);
                System.out.println(solution);
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
