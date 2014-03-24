package scu.cloud.generator;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import scu.common.model.ComputingElement;
import scu.common.model.Functionality;
import scu.common.model.Service;
import scu.util.Config;

public class ServiceGenerator {

    private Logger logger = Logger.getLogger("Generator");
    private Config config = null;

    public ServiceGenerator(Config config) {
        this.config = config;
    }
    
    public ArrayList<Service> generateServicesForComputingElement(ArrayList<ComputingElement> elements) {
        
        // get configs
        int seed = Integer.parseInt(config.get("seed"));
        int nSvcFunc = Integer.parseInt(config.get("number_of_service_functionalities"));
        double nSvcAvg = Double.parseDouble(config.get("average_number_of_services_per_elements"));
        double nSvcSd = Double.parseDouble(config.get("sd_number_of_services_per_elements"));

        // initiate the distribution for the types of services assigned to each element
        UniformIntegerDistribution distSvcFunc = new UniformIntegerDistribution(
                new MersenneTwister(seed), 1, (nSvcFunc>1?nSvcFunc:2));      
        // initiate the distribution for the number of services assigned to each element
        NormalDistribution distSvc = new NormalDistribution(new MersenneTwister(seed), 
                nSvcAvg, nSvcSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        // populating elements' services
        logger.info("Generating elements' services (navg="+nSvcAvg+",scoreavg="+nSvcSd+").");

        ArrayList<Service> services = new ArrayList<Service>();
        
        for (ComputingElement element : elements) {
            long nSvc = 1;
            if (nSvcFunc>1) nSvc = Math.round(distSvc.sample());
            ArrayList<Integer> flist = new ArrayList<Integer>();
            for (int j=0; j<nSvc; j++) {
                int f = 0;
                do {
                    f = distSvcFunc.sample();
                } while (flist.contains(f) || f>nSvcFunc);
                String svcFunc = "F" + f;
                flist.add(f);
                Service service = new Service(new Functionality(svcFunc), element);
                services.add(service);
            }
            
        }
        
        return services;
    }
    
}
