package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONException;

import scu.cloud.discoverer.Discoverer;
import scu.cloud.generator.ServiceGenerator;
import scu.cloud.generator.TaskGenerator;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.cloud.monitor.AvailabilityMonitor;
import scu.common.model.Role;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.util.ConfigJson;


public class TestDiscoverer {

    public static void main(String[] args) {

        try {

            ConfigJson config = new ConfigJson("task-generator.json");

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
                System.out.println(service);
            }

            // init availability metric
            AvailabilityMonitor.initGenerator(metricConfig);

            // generate task
            TaskGenerator taskGen = new TaskGenerator(config);
            ArrayList<Task> rootTasks = new ArrayList<Task>(); 
            ArrayList<Task> tasks = taskGen.generate(rootTasks);
            
            // discover
            Discoverer discoverer = new Discoverer(manager);
            int time = 0;
            for (Task t : rootTasks) {
                int deadline = (int)t.getSpecification()
                        .findObjective("deadline") 
                        .getValue();
                for (Role r: t.getAllRoles()) {
                    ArrayList<Service> discServices = discoverer.discoverServices(
                            r.getFunctionality(), 
                            t.getSpecification().merge(r.getSpecification()),
                            time++, 
                            (int) Math.ceil(t.getLoad()), 
                            deadline);
                    System.out.println(r);
                    System.out.println(discServices.size());
                }
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
