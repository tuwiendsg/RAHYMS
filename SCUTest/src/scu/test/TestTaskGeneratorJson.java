package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;

import scu.cloud.generator.ComputingElementGenerator;
import scu.cloud.generator.ServiceGenerator;
import scu.cloud.generator.ServiceGeneratorJson;
import scu.cloud.generator.TaskGeneratorJson;
import scu.cloud.manager.ComputingElementManagerOnMemory;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.common.model.ComputingElement;
import scu.common.model.HumanComputingElement;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.util.Config;
import scu.util.ConfigJson;


public class TestTaskGeneratorJson {

    public static void main(String[] args) {

        try {

            ConfigJson config = new ConfigJson("task-generator.json");

            // generate services
            TaskGeneratorJson taskGen = new TaskGeneratorJson(config);
            ArrayList<Task> rootTasks = new ArrayList<Task>(); 
            ArrayList<Task> tasks = taskGen.generate(rootTasks);
            
            // dump
            System.out.println("====== TASKS =======");
            for (Task t : tasks) {
                System.out.println(t);
            }
            System.out.println("====== ROOT TASKS =======");
            for (Task t : rootTasks) {
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