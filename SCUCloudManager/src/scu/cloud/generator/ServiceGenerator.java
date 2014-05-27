package scu.cloud.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scu.common.interfaces.MetricMonitorInterface;
import scu.common.model.ComputingElement;
import scu.common.model.Functionality;
import scu.common.model.HumanComputingElement;
import scu.common.model.Service;
import scu.util.ConfigJson;

public class ServiceGenerator {

    private Logger logger = Logger.getLogger("Generator");
    private JSONObject configRoot = null;
    
    // distribution generators
    private UniformRealDistribution distPropToHave;
    private Object distPropValue;
    private UniformRealDistribution distConnToHave;
    private Object distConnWeight;
    private UniformRealDistribution distSvcToHave;
    private UniformRealDistribution[] distSvcPropToHaves;
    private Object[] distSvcPropValues;

    public ServiceGenerator(ConfigJson config) {
        this.configRoot = config.getRoot();
    }
    
    public ArrayList<Service> generate() 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        
        // init
        ArrayList<Service> services = new ArrayList<Service>();
        ArrayList<ComputingElement> elements = new ArrayList<ComputingElement>();
        
        // config
        int seed = configRoot.getInt("seed");
        int nElements = configRoot.getInt("numberOfElements");
        JSONObject connCfg = configRoot.getJSONObject("connection");
        JSONArray svcCfg = configRoot.getJSONArray("services");
        JSONArray propCfg = configRoot.getJSONArray("commonProperties"); 
        
        // generate elements
        logger.info("Generating " + nElements + " elements...");
        for (long i=1; i<=nElements; i++) {
            elements.add(new HumanComputingElement(i));
        }
        
        // generate common properties
        for (int i=0; i<propCfg.length(); i++) {
            
            // get config
            JSONObject prop = propCfg.getJSONObject(i);
            String type = prop.getString("type");
            String name = prop.getString("name");
            double pToHave = prop.getDouble("probabilityToHave");
            JSONObject mapping = null;
            logger.info("Generating " + type + " " + name + "...");
            
            // init distributions
            if (distPropToHave==null) 
                distPropToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
            if (type.equals("static") || type.equals("skill")) {
                JSONObject valueCfg = prop.getJSONObject("value");
                String clazz = GeneratorUtil.DISTRIBUTION_PACKAGE + valueCfg.getString("class");
                JSONArray params = valueCfg.getJSONArray("params");
                if (valueCfg.has("mapping")) mapping = valueCfg.getJSONObject("mapping");
                // TODO: distPropValue should be reusable, because we may generate streams of services
                distPropValue = GeneratorUtil.createValueDistribution(clazz, params, seed++);
            }
            
            for (ComputingElement e : elements) {
                HumanComputingElement element = (HumanComputingElement)e;
                if (GeneratorUtil.shouldHave(distPropToHave, pToHave)) {
                    if (type.equals("static") || type.equals("skill")) {
                        GeneratorUtil.generateProperty(element, name, type, distPropValue, mapping);
                    } else if (type.equals("metric")) {
                        String ifaceClazz = prop.getString("interfaceClass");
                        MetricMonitorInterface metric = GeneratorUtil.createMetricObject(ifaceClazz);
                        element.getMetrics().setInterface(name, metric);
                    }
                }
            }
            
        }
        
        // generate connections
        double pToConnect = connCfg.getDouble("probabilityToConnect");
        JSONObject weightCfg = connCfg.getJSONObject("weight");
        String weightClazz = GeneratorUtil.DISTRIBUTION_PACKAGE + weightCfg.getString("class");
        JSONArray weightParams = weightCfg.getJSONArray("params");        
        if (distConnToHave==null) 
            distConnToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
        if (distConnWeight==null) 
            distConnWeight = GeneratorUtil.createValueDistribution(weightClazz, weightParams, seed++);
        logger.info("Generating connections...");
        for (int i=0; i<elements.size(); i++) {
            ComputingElement e = elements.get(i);
            for (int j=i+1; j<elements.size(); j++) {
                if (i!=j) {
                    if (GeneratorUtil.shouldHave(distConnToHave, pToConnect)) {
                        double weight = (double)GeneratorUtil.sample(distConnWeight);
                        e.setConnection(elements.get(j), weight);
                        elements.get(j).setConnection(e, weight);
                    }                    
                }
            }
        }
        
        // generate services
        for (int i=0; i<svcCfg.length(); i++) {
            // get config
            JSONObject svc = svcCfg.getJSONObject(i);
            String func = svc.getString("functionality");
            double pSvcToHave = svc.getDouble("probabilityToHave");
            JSONArray prop = svc.getJSONArray("properties");
            logger.info("Generating service " + func + "...");

            // init config and distributions
            distSvcToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
            int propLength = prop.length();
            String[] types = new String[propLength];
            String[] names = new String[propLength];
            String[] clazzes = new String[propLength];
            distSvcPropValues = new Object[propLength];
            double[] pToHaves = new double[propLength];
            JSONArray[] params = new JSONArray[propLength];
            JSONObject[] mappings = new JSONObject[propLength];
            JSONObject[] valueCfgs = new JSONObject[propLength];
            distSvcPropToHaves = new UniformRealDistribution[propLength];
            for (int j=0; j<prop.length(); j++) {
                JSONObject curProp = prop.getJSONObject(j);
                types[j] = curProp.getString("type");
                names[j] = curProp.getString("name");
                pToHaves[j] = curProp.getDouble("probabilityToHave");
                valueCfgs[j] = curProp.getJSONObject("value");
                params[j] = valueCfgs[j].getJSONArray("params");
                clazzes[j] = GeneratorUtil.DISTRIBUTION_PACKAGE + valueCfgs[j].getString("class");
                mappings[j] = null;
                if (valueCfgs[j].has("mapping")) mappings[j] = valueCfgs[j].getJSONObject("mapping");
                distSvcPropValues[j] = GeneratorUtil.createValueDistribution(clazzes[j], params[j], seed++);
                distSvcPropToHaves[j] = new UniformRealDistribution
                        (new MersenneTwister(seed++), 0, 1);
            }
            
            for (ComputingElement e : elements) {
                if (GeneratorUtil.shouldHave(distSvcToHave, pSvcToHave)) {
                    HumanComputingElement element = (HumanComputingElement)e;
                    // add service
                    Service service = new Service(new Functionality(func), element);
                    element.addService(service);
                    services.add(service);
                    for (int j=0; j<prop.length(); j++) {
                        if (GeneratorUtil.shouldHave(distSvcPropToHaves[j], pToHaves[j])) {
                            GeneratorUtil.generateProperty(element, names[j], types[j], 
                                    distSvcPropValues[j], mappings[j]);
                        }
                    }
                }
            }
            
            
        }
        
        return services;
    }
    
}
