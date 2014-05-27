package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;

import scu.cloud.generator.ServiceGenerator;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.cloud.monitor.AvailabilityMonitor;
import scu.common.model.ComputingElement;
import scu.common.model.Service;
import scu.util.ConfigJson;


public class TestServiceGenerator {

    public static void main(String[] args) {

        try {

            ConfigJson svcConfig = new ConfigJson("service-generator.json");
            ConfigJson metricConfig = new ConfigJson("metric-generator.json");

            // generate services
            ServiceGenerator svcGen = new ServiceGenerator(svcConfig);
            ArrayList<Service> services;
            services = svcGen.generate();
            
            // save
            ServiceManagerOnMemory manager = new ServiceManagerOnMemory();
            for (Service service : services) {
                manager.registerService(service);
                System.out.println(service);
            }
            
            // test loading
            Collection<ComputingElement> loaded = manager.retrieveElements();
            for (ComputingElement element : loaded) {
                System.out.println(element);
            }
            
            // test metric
            AvailabilityMonitor.initGenerator(metricConfig);
            for (ComputingElement element : loaded) {
                long responseTime = (long) element.getMetrics().getValue("response_time", 
                        new Object[]{10, 5});
                System.out.println(responseTime);
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
