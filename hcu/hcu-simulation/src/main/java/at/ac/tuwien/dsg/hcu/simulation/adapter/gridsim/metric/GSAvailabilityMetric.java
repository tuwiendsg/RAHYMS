package at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.metric;

import gridsim.GridSim;
import at.ac.tuwien.dsg.hcu.common.interfaces.MetricInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.GSReservationPolicy;

public class GSAvailabilityMetric implements MetricInterface {

    private static GSAvailabilityMetric _instance;

    public GSAvailabilityMetric() {
    }
    
    public static GSAvailabilityMetric getInstance() {
        if (_instance==null) _instance = new GSAvailabilityMetric();
        return _instance;
    }

    @Override
    public Object measure(Service service, String name, Object[] params) {
        return getInstance().measureMetric(service.getProvider(), name, params);
    }

    @Override
    public Object measure(ComputingElement element, String name, Object[] params) {
        return getInstance().measureMetric(element, name, params);
    }

    @Override
    public Object update(Service service, String name, Object[] params) {
        return null;
    }

    @Override
    public Object update(ComputingElement element, String name, Object[] params) {
        return null;
    }

    private Object measureMetric(ComputingElement element, String name, Object[] params) {
        Object result = null;
        switch (name) {
            case "response_time":
                if (params.length>=2) {
                    double start = (double) params[0]; 
                    double load = (double) params[1];
                    double duration = 0;
                    if (params.length>=3) {
                        duration = (double) params[2];
                    }
                    double response = measureResposeTime(element, start, load, duration);
                    //result = response;
                    result = response - GridSim.clock();
                }
                break;
            case "execution_time":
                if (params.length>=1) {
                    double load = (double) params[0]; 
                    double response = measureExecutionTime(element, load);
                    result = response;
                }
                break;
            case "earliest_availability":
                if (params.length>=2) {
                    double start = (double) params[0]; 
                    double duration = (double) params[1]; 
                    double response = measureEarliestAvailability(element, start, duration);
                    result = response;
                }
                break;
        }
        return result;
    }

    private double measureResposeTime(ComputingElement element, double startTime, 
            double load, double duration) {
        // if duration is specified, we dont need to forecast again the duration of execution time
        double forecast = 0;
        try {
            GSReservationPolicy policy = GSReservationPolicy.getInstance(element);
            forecast = policy.forecastResponseTime(startTime, load, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }
    
    private double measureExecutionTime(ComputingElement element, double load) {
        double forecast = 0;
        try {
            GSReservationPolicy policy = GSReservationPolicy.getInstance(element);
            forecast = policy.forecastExecutionTime(load);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }

    private double measureEarliestAvailability(ComputingElement element, double startTime, 
            double duration) {
        double forecast = 0;
        try {
            GSReservationPolicy policy = GSReservationPolicy.getInstance(element);
            forecast = policy.forecastEarliestAvailability(startTime, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }
}
