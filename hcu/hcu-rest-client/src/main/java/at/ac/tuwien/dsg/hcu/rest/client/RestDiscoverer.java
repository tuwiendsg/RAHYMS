package at.ac.tuwien.dsg.hcu.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.ac.tuwien.dsg.hcu.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Connection;
import at.ac.tuwien.dsg.hcu.common.model.Functionality;
import at.ac.tuwien.dsg.hcu.common.model.HumanComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.MachineComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Properties;
import at.ac.tuwien.dsg.hcu.common.model.Service;
import at.ac.tuwien.dsg.hcu.common.model.Skills;
import at.ac.tuwien.dsg.hcu.common.sla.Objective;
import at.ac.tuwien.dsg.hcu.common.sla.Specification;
import at.ac.tuwien.dsg.hcu.util.Util;

public class RestDiscoverer implements DiscovererInterface {

    private Map<String, Object> config;
    
    public RestDiscoverer(ServiceManagerInterface manager) {
        // we dont need an internal service manager here
    }

    @Override
    public List<Service> discoverServices(Functionality functionality, Specification specification) {
        return discoverServices(functionality, specification, 0, 0, 0);
    }

    @Override
    public List<Service> discoverServices(Functionality functionality, Specification specification, double timeStart,
            double load, double deadline) {
        List<Service> results = new ArrayList<Service>();
        try {
            
            // construct post data
            String jsonData = String.format(
                    "{\"functionality\":\"%s\","
                  + " \"constraints\":%s,"
                  + " \"timeStart\":%f,"
                  + " \"load\":%f,"
                  + " \"deadline\":%f}",
                  functionality!=null ? functionality.getName() : "",
                  specification!=null ? specToJson(specification) : "",
                  timeStart, load, deadline);
            
            // make api call
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost((String)config.get("discoverServiceEndpoint"));
            StringEntity entity = new StringEntity(jsonData);
            entity.setContentType("application/json;charset=UTF-8");
            post.setEntity(entity);
            //System.out.println("Calling " + DISCOVER_SERVICE_ENDPOINT);
            HttpResponse response = client.execute(post);
            
            // read response
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String jsonResult = "";
            String line = "";
            while ((line = rd.readLine()) != null) {
                jsonResult += "\n" + line;
            }
            
            //parse response, first to a list of hashmap
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> tmpResults = new ArrayList<HashMap>();
            tmpResults = mapper.readValue(jsonResult, List.class);
            // then convert to list of services
            for (HashMap svcMap: tmpResults) {
                Service service = mapToService(svcMap);
                //System.out.println(service);
                results.add(service);
            }
            //System.out.println(results.size() + " service(s) discovered.");
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        return results;
    }

    @Override
    public List<Connection> discoverConnections(List<Service> services) {
        return new ArrayList<Connection>();
    }
    
    private String specToJson(Specification spec) {
        String result = "";
        
        for (Objective obj: spec.getObjectives()) {
            // skip connectedness (handled separately)
            // skip deadline (specified directly as deadline argument)
            if (obj.getName().equals("connectedness") || obj.getName().equals("deadline")) {
                continue;
            }
            if (!result.equals("")) {
                result += ",";
            }
            result += Util.dumpObjectToJson(obj);
        }
        
        result = "[" + result + "]";
        return result;
    }
    
    private Service mapToService(Map data) {

        String funcName = "";
        if (data.get("functionality")!=null) {
            funcName = (String)((Map)data.get("functionality")).get("name");
        } else {
            Util.log().severe("Service has no functionality");
        }
        Functionality functionality = new Functionality(funcName);
        
        ComputingElement element = mapToComputingElement((Map) data.get("provider"));
        
        Service service = new Service(functionality, element);
        service.setId((int) data.get("id"));
        service.setDescription((String) data.get("description"));
        
        return service;
    }

    private ComputingElement mapToComputingElement(Map data) {
        
        if (data==null) {
            return null;
        }
        
        int type = (int) data.getOrDefault("type", 0);
        long id = ((int) data.get("id")) * 1L;
        ComputingElement element;
        
        if (type==ComputingElement.HUMAN_COMPUTING_ELEMENT_TYPE) {
            element = new HumanComputingElement(id, (String) data.get("name"));
        }
        else if (type==ComputingElement.MACHINE_COMPUTING_ELEMENT_TYPE) {
            element = new MachineComputingElement(id, (String) data.get("name"));
        }
        else {
            element = new ComputingElement(id, (String) data.get("name"));
        }
        element.setDescription((String)data.get("description"));
        
        Properties properties = new Properties();
        properties.setValueSet((Map<String, Object>) ((Map)data.get("properties")).get("valueSet"));
        element.setProperties(properties);
        
        if (data.get("skills")!=null && type==ComputingElement.HUMAN_COMPUTING_ELEMENT_TYPE) {
            Skills skills = new Skills();
            skills.setValueSet((Map<String, Object>) ((Map)data.get("skills")).get("valueSet"));
            ((HumanComputingElement)element).setSkills(skills);
        }
        
        return element;
    }

    @Override
    public void setConfiguration(Map<String, Object> config) {
        this.config = config;
    }
}
