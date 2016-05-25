package at.ac.tuwien.dsg.hcu.cloud.service;

public class TestMachineTaskClient {

    public static void main(String[] args) {
        
        MachineTaskClient.sendTask("128.130.172.216:8080", "10.99.0.17:8080", 20);

    }

}
