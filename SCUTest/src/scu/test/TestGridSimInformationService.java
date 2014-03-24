package scu.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.IO_data;
import gridsim.ResourceCharacteristics;
import scu.adapter.gridsim.GSConstants;
import scu.adapter.gridsim.GSService;
import scu.adapter.gridsim.GSServiceCharacteristics;
import scu.adapter.gridsim.GSInformationService;
import scu.cloud.generator.ComputingElementGenerator;
import scu.cloud.generator.ServiceGenerator;
import scu.cloud.manager.ServiceManagerOnMemory;
import scu.common.model.ComputingElement;
import scu.common.model.Service;
import scu.util.Config;

public class TestGridSimInformationService extends GridSim {

    private int id;     // entity ID of this object
    private String name;    // entity name of this object
    private int totalResources; // total number of resources in the simulation (i.e., services)

    TestGridSimInformationService(String name, int totalResources)
            throws Exception {
        super(name, 560);
        this.name = name;
        this.totalResources = totalResources;

        // Gets an ID for this entity
        this.id = new Integer( getEntityId(name) );
        System.out.println("Creating a grid user entity with name = " +
                name + ", and id = " + this.id);
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body() {
        
        // list of resources
        int resourceId[] = new int[this.totalResources];
        String resourceName[] = new String[this.totalResources];

        LinkedList resList;
        ResourceCharacteristics resChar;

        // waiting to get list of resources. Since GridSim package uses
        // multi-threaded environment, your request might arrive earlier
        // before one or more grid resource entities manage to register
        // themselves to GridInformationService (GIS) entity.
        // Therefore, it's better to wait in the first place
        while (true) {
            // need to pause for a while to wait GridResources finish
            // registering to GIS
            super.gridSimHold(1.0);    // hold by 1 second

            resList = getGridResourceList();
            if (resList.size() == this.totalResources)
                break;
            else
            {
                System.out.println(this.name +
                        ":Waiting to get list of resources ...");
            }
        }

        int SIZE = 12;   // size of Integer object is roughly 12 bytes
        int i = 0;
        
        // a loop to get all the resources available.
        // Once the resources are known, then send HELLO and TEST tag to each
        // of them.
        for (i = 0; i < this.totalResources; i++) {
            // Resource list contains list of resource IDs not grid resource
            // objects.
            resourceId[i] = ( (Integer) resList.get(i) ).intValue();

            // Requests to resource entity to send its characteristics
            // NOTE: sending directly without using I/O port
            super.send(resourceId[i], GridSimTags.SCHEDULE_NOW,
                       GridSimTags.RESOURCE_CHARACTERISTICS, this.id);

            // waiting to get a resource characteristics
            resChar = (GSServiceCharacteristics) receiveEventObject();
            resourceName[i] = resChar.getResourceName();

            // print that this entity receives a particular resource 
            // characteristics
            System.out.println(this.name +
                    ":Received ResourceCharacteristics from " +
                    resourceName[i] + ", with id = " + resourceId[i] + ", element: " +
                    ((GSService)Sim_system.get_entity(resourceId[i])).getService().toString());


            // send TEST tag to a resource using I/O port.
            // It will consider transfer time over a network.
            System.out.println(this.name + ": Sending " + GSConstants.HELLO_TAG + " tag to " +
                    resourceName[i] + " at time " + GridSim.clock());
            super.send( super.output, GridSimTags.SCHEDULE_NOW, GSConstants.HELLO_TAG,
                    new IO_data(this.id, SIZE, resourceId[i]) );
                    
            // send HELLO tag to a resource using I/O port
            System.out.println(this.name + ": Sending " + GSConstants.TEST_TAG + " tag to " +
                    resourceName[i] + " at time " + GridSim.clock());
            super.send( super.output, GridSimTags.SCHEDULE_NOW, GSConstants.TEST_TAG,
                    new IO_data(this.id, SIZE, resourceId[i]) ); 
        }

        // need to wait for 10 seconds to allow a resource to process
        // receiving events.
        super.sim_pause(10);
        
        // shut down all the entities, including GridStatistics entity since
        // we used it to record certain events.
        shutdownGridStatisticsEntity();
        shutdownUserEntity();
        terminateIOEntities();
        System.out.println(this.name + ":%%%% Exiting body()");
    }

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args)
    {
        System.out.println("Starting TestGridSimInformationService");

        try {
            // First step: Initialize the GridSim package. It should be called
            // before creating any entities. We can't run this example without
            // initializing GridSim first. We will get run-time exception
            // error.
            int numUser = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = true ;  // true means trace GridSim events

            // Initialize the GridSim package
            // Starting from GridSim 3.0, you can specify different type of
            // initialisation.
            System.out.println("Initializing GridSim package");

            // In this example, initialise GridSim without creating
            // a default GridInformationService (GIS) entity.
            GridSim.init(numUser, calendar, traceFlag, false);

            // Create a new GIS entity    
            ServiceManagerOnMemory manager = new ServiceManagerOnMemory();
            GSInformationService infoService = new GSInformationService(manager);

            // We need to call this method before the start of simulation
            GridSim.setGIS(infoService);

            // Second step: Creates one or more grid resource entities (i.e., HCU services)
            int numServices = generateServices();

            // Third step: Creates one or more grid user entities
            TestGridSimInformationService user = new TestGridSimInformationService("User_0", numServices);
            // we dont need to do anything to this user object, 
            // once created, it will be regiestered to the infoService, 
            // and body() will be executed when the simulation starts 

            // Fourth step: Starts the simulation
            GridSim.startGridSimulation();
            System.out.println("Finish TestGridSimInformationService");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }
    
    private static int generateServices() {
        int numServices = 0;
        try {
            Config config = new Config("cloud-generator.properties");
            ComputingElementGenerator generator = new ComputingElementGenerator(config);
            ArrayList<ComputingElement> hceList = generator.generateHumanComputingElement();
            ServiceGenerator svcGen = new ServiceGenerator(config);
            ArrayList<Service> services = svcGen.generateServicesForComputingElement(hceList);
            for (Service service : services) {
                GSService gsservice = new GSService(service);
                // once created, it is automatically registered by GridSim
                // and eventually registered to manager by GSInformationService
                numServices++;
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numServices;
    }


} // end class

