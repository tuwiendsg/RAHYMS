package at.ac.tuwien.dsg.hcu.cloud.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.ac.tuwien.dsg.hcu.common.interfaces.MetricInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.HumanComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.MachineComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.Util;

public class ServiceGenerator {

    private ArrayList<JSONObject> configs = new ArrayList<JSONObject>();
    private int seed;
    private static long lastElementId = 0;
    
    // distribution generators
    private UniformRealDistribution distPropToHave;
    private Object distPropValue;
    private UniformRealDistribution distConnToHave;
    private Object distConnWeight;
    private UniformRealDistribution distSvcToHave;
    private UniformRealDistribution[] distSvcPropToHaves;
    private Object[] distSvcPropValues;

    public ServiceGenerator(ArrayList<ConfigJson> config) {
        for (ConfigJson json: config) {
            configs.add(json.getRoot());
        }
    }

    public ServiceGenerator(ConfigJson config) {
        configs.add(config.getRoot());
    }
    
    public ArrayList<Service> generate() 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        ArrayList<Service> allServices = new ArrayList<Service>();
        for (JSONObject config: configs) {
            ArrayList<Service> services = generateOneConfig(config);
            allServices.addAll(services);
        }
        return allServices;
    }
    
    public ArrayList<Service> generateOneConfig(JSONObject configRoot) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, 
            SecurityException, ClassNotFoundException, JSONException {
        
        // init
        ArrayList<Service> services = new ArrayList<Service>();
        ArrayList<ComputingElement> elements = new ArrayList<ComputingElement>();
        
        // config
        seed = configRoot.getInt("seed");
        int nElements = configRoot.getInt("numberOfElements");
        String namePrefix = configRoot.has("namePrefix") ? configRoot.getString("namePrefix") : null;
        String elementType = configRoot.has("type") ? configRoot.getString("type") : "human";
        JSONObject connCfg = configRoot.getJSONObject("connection");
        JSONArray svcCfg = configRoot.getJSONArray("services");
        JSONArray propCfg = configRoot.getJSONArray("commonProperties"); 
        
        // generate elements
        Util.log().info("Generating " + nElements + " elements");
        for (long i=1; i<=nElements; i++) {
            if (namePrefix==null) {
                if (elementType.equals("machine")) {
                    elements.add(new MachineComputingElement(lastElementId++));
                } else {
                    elements.add(new HumanComputingElement(lastElementId++));
                }
            } else {
                if (elementType.equals("machine")) {
                    elements.add(new MachineComputingElement(lastElementId++, namePrefix + i));
                } else {
                    elements.add(new HumanComputingElement(lastElementId++, namePrefix + i));
                }
            }
        }
        
        // generate common properties
        for (int i=0; i<propCfg.length(); i++) {
            
            // get config
            JSONObject prop = propCfg.getJSONObject(i);
            String type = prop.getString("type");
            String name = prop.getString("name");
            double pToHave = prop.getDouble("probabilityToHave");
            JSONObject mapping = null;
            //logger.info("Generating " + type + " " + name + "...");
            
            // init distributions
            if (distPropToHave==null) 
                distPropToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
            Object propertyValue = null;
            if (type.equals("static") || type.equals("skill")) {
                Object value = prop.get("value");
                if (value instanceof JSONObject) {
                	JSONObject valueCfg = (JSONObject)value;
	                String clazz = GeneratorUtil.getFullClassName(valueCfg.getString("class"));
	                JSONArray params = valueCfg.getJSONArray("params");
	                if (valueCfg.has("mapping")) mapping = valueCfg.getJSONObject("mapping");
	                // TODO: distPropValue should be reusable, because we may generate streams of services
	                distPropValue = GeneratorUtil.createValueDistribution(clazz, params, seed++);
                } else {
                	propertyValue = value;
                }
            }
            
            for (ComputingElement e : elements) {
                ComputingElement element = (ComputingElement)e;
                if (GeneratorUtil.shouldHave(distPropToHave, pToHave)) {
                    if (type.equals("static") || type.equals("skill")) {
                    	if (propertyValue!=null) {
                    		element.getProperties().setValue(name, propertyValue);
                    	} else {
                    		GeneratorUtil.generateProperty(element, name, type, distPropValue, mapping);
                    	}
                    } else if (type.equals("metric")) {
                        String ifaceClazz = prop.getString("interfaceClass");
                        MetricInterface metric = GeneratorUtil.createMetricObject(ifaceClazz);
                        element.getMetrics().setInterface(name, metric);
                    }
                }
            }
            
        }
        
        // generate connections
        double pToConnect = connCfg.getDouble("probabilityToConnect");
        JSONObject weightCfg = connCfg.getJSONObject("weight");
        String weightClazz = GeneratorUtil.getFullClassName(weightCfg.getString("class"));
        JSONArray weightParams = weightCfg.getJSONArray("params");        
        if (distConnToHave==null) 
            distConnToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
        if (distConnWeight==null) 
            distConnWeight = GeneratorUtil.createValueDistribution(weightClazz, weightParams, seed++);
        //logger.info("Generating connections...");
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
        
        if (configRoot.has("singleElementSingleServices") && configRoot.getBoolean("singleElementSingleServices")) {
            services = generateSingleServices(svcCfg, elements);
        } else {
            services = generateMultipleServices(svcCfg, elements);
        }
        
        /*
        System.out.println("=== Generated services ===");
        printServiceList(services);
        System.out.println("==========================");
        */
        System.out.println("=== Generated elements ===");
        printElementList(elements);
        System.out.println("==========================");
        
        return services;
    }
    
    private ArrayList<Service> generateMultipleServices(JSONArray svcCfg, ArrayList<ComputingElement> elements) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, SecurityException, 
            ClassNotFoundException, JSONException {
        
        ArrayList<Service> services = new ArrayList<Service>();
        
        // generate services
        for (int i=0; i<svcCfg.length(); i++) {
            // get config
            JSONObject svc = svcCfg.getJSONObject(i);
            String func = svc.getString("functionality");
            double pSvcToHave = svc.getDouble("probabilityToHave");
            JSONArray prop = svc.getJSONArray("properties");
            //logger.info("Generating service " + func + "...");

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
                clazzes[j] = GeneratorUtil.getFullClassName(valueCfgs[j].getString("class"));
                mappings[j] = null;
                if (valueCfgs[j].has("mapping")) mappings[j] = valueCfgs[j].getJSONObject("mapping");
                distSvcPropValues[j] = GeneratorUtil.createValueDistribution(clazzes[j], params[j], seed++);
                distSvcPropToHaves[j] = new UniformRealDistribution
                        (new MersenneTwister(seed++), 0, 1);
            }
            
            for (ComputingElement e : elements) {
                if (GeneratorUtil.shouldHave(distSvcToHave, pSvcToHave)) {
                    //HumanComputingElement element = (HumanComputingElement)e;
                    // add service
                    Service service = new Service(new Functionality(func), e);
                    e.addService(service);
                    services.add(service);
                    for (int j=0; j<prop.length(); j++) {
                        if (GeneratorUtil.shouldHave(distSvcPropToHaves[j], pToHaves[j])) {
                            GeneratorUtil.generateProperty(e, names[j], types[j], 
                                    distSvcPropValues[j], mappings[j]);
                        }
                    }
                }
            }
            
            
        }
        
        return services;
    }
    
    private ArrayList<Service> generateSingleServices(JSONArray svcCfg, ArrayList<ComputingElement> elements) 
            throws InstantiationException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, SecurityException, 
            ClassNotFoundException, JSONException {
        
        ArrayList<Service> services = new ArrayList<Service>();
        
        // get probability to have for all services
        double[] probabilityToHave = new double[svcCfg.length()];
        for (int i=0; i<svcCfg.length(); i++) {
            JSONObject svc = svcCfg.getJSONObject(i);
            probabilityToHave[i] = svc.getDouble("probabilityToHave");
        }
        
        // generate services
        distSvcToHave = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);
        for (ComputingElement e : elements) {
            
            int serviceId = GeneratorUtil.selectOne(distSvcToHave, probabilityToHave);
            
            // get config for the selected service
            JSONObject svc = svcCfg.getJSONObject(serviceId);
            String func = svc.getString("functionality");
            JSONArray prop = svc.getJSONArray("properties");

            // init config and distributions
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
                clazzes[j] = GeneratorUtil.getFullClassName(valueCfgs[j].getString("class"));
                mappings[j] = null;
                if (valueCfgs[j].has("mapping")) mappings[j] = valueCfgs[j].getJSONObject("mapping");
                distSvcPropValues[j] = GeneratorUtil.createValueDistribution(clazzes[j], params[j], seed++);
                distSvcPropToHaves[j] = new UniformRealDistribution
                        (new MersenneTwister(seed++), 0, 1);
            }
            
            // add service
            HumanComputingElement element = (HumanComputingElement)e;
            Service service = new Service(new Functionality(func), element);
            element.addService(service);
            services.add(service);
            // add related properties to the element
            for (int j=0; j<prop.length(); j++) {
                if (GeneratorUtil.shouldHave(distSvcPropToHaves[j], pToHaves[j])) {
                    GeneratorUtil.generateProperty(element, names[j], types[j], 
                            distSvcPropValues[j], mappings[j]);
                }
            }
        }

        return services;
    }
    
    
    public void printServiceList(List<Service> services) {
        for (Service s: services) {
            System.out.println(s);
        }
        
    }
    
    public void printElementList(List<ComputingElement> elements) {
        for (ComputingElement e: elements) {
            System.out.println(e.detail());
        }
        
    }
}
