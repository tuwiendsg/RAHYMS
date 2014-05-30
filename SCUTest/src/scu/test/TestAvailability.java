package scu.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;

import scu.cloud.monitor.AvailabilityMonitor;
import scu.common.model.HumanComputingElement;
import scu.common.model.Metrics;
import scu.util.ConfigJson;

public class TestAvailability {

    public static void main(String[] args) {

        try {

            ConfigJson metricConfig = new ConfigJson("metric-generator.json");
            AvailabilityMonitor.initGenerator(metricConfig);
            
            HumanComputingElement element = new HumanComputingElement(1);
            element.getMetrics().setInterface("availability_status", AvailabilityMonitor.getInstance());
            element.getMetrics().setInterface("availability_sequence", AvailabilityMonitor.getInstance());
            element.getMetrics().setInterface("response_time", AvailabilityMonitor.getInstance());
            
            System.out.println(element.getMetrics().getValue(
                    "availability_sequence"));
            System.out.println(element.getMetrics().getValue(
                    "availability_status", new Object[]{9}));
            System.out.println(element.getMetrics().getValue(
                    "availability_sequence"));
            
            element.getMetrics().setValue(
                    "availability_status", new Object[]{20, 20, AvailabilityMonitor.BUSY});
            System.out.println(element.getMetrics().getValue(
                    "availability_status", new Object[]{20}));
            System.out.println(element.getMetrics().getValue(
                    "availability_sequence"));
            
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

    }

}
