package at.ac.tuwien.dsg.hcu.monitor.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJackson {

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> userData = mapper.readValue(new File("scenarios/test.json"), Map.class);
            List<Object> agents = (List<Object>) userData.get("monitoring_agents");
            System.out.println(userData);
            System.out.println(agents);
            System.out.println(agents.get(0));
            System.out.println(((ArrayList)userData.get("monitoring_agents")).get(1));
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
