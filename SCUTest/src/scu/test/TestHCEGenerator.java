package scu.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import scu.cloud.generator.ComputingElementGenerator;
import scu.cloud.generator.ServiceGenerator;
import scu.cloud.manager.ComputingElementManagerOnMemory;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.common.model.ComputingElement;
import scu.common.model.HumanComputingElement;
import scu.common.model.Service;
import scu.util.Config;


public class TestHCEGenerator {

    public static void main(String[] args) {

        try {

            Config config = new Config("cloud-generator.properties");

            // generate elements
            ComputingElementGenerator hceGen = new ComputingElementGenerator(config);
            ArrayList<ComputingElement> hceList = hceGen.generateHumanComputingElement();
            
            // save
            ServiceManagerOnMemory manager = new ServiceManagerOnMemory();
            for (ComputingElement element : hceList) {
                manager.saveElement(element);
            }
            
            // test loading
            Collection<ComputingElement> loaded = manager.retrieveElements();
            for (ComputingElement element : loaded) {
                System.out.println(element);
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
