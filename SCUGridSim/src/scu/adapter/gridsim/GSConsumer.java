package scu.adapter.gridsim;

import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.IO_data;
import gridsim.parallel.reservation.ReservationRequester;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import scu.cloud.generator.ServiceGenerator;
import scu.cloud.generator.TaskGenerator;
import scu.common.interfaces.SchedulerInterface;
import scu.common.interfaces.ServiceManagerInterface;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.util.ConfigJson;
import scu.util.Util;
import scu.util.WekaExporter;

/**
 * @author Muhammad Zuhri
 * GSConsumer controls simulation by
 * - initiating GridSim
 * - generating services (and computing elements)
 * - acting as a consumer who generates task
 * - terminating GridSim
 */
public class GSConsumer extends ReservationRequester {
    
    private static String NAME = "GSConsumer";
    private static GSMiddleware gsMiddleware;
    
    private static String configFile;
    
    // we need to know the number of services generated to wait until all services 
    // have been registered
    private static int numServices;
    
    private TaskGenerator taskGen;
    private int nCycle;
    private int waitBetweenCycles;
        
    private GSConsumer(ConfigJson taskGeneratorConfig) throws Exception {
        super(NAME, 560);
        taskGen = new TaskGenerator(taskGeneratorConfig);
        nCycle = Integer.parseInt(Util.getProperty(configFile, "number_of_cycles"));
        waitBetweenCycles = Integer.parseInt(Util.getProperty(configFile, "wait_between_cycles"));
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body() {

        // wait until all services have been registered
        Util.log().info("Waiting until all services registered");
        LinkedList svcList = null;
        while (true) {
            super.gridSimHold(1.0);    // hold by 1 second
            svcList = getGridResourceList();
            if (svcList.size() == numServices) {
                break;
            }
        }
        Util.log().info(svcList.size() + " services have been registered");

        try {
            
            int nTask = 0;
            for (int i=0; i<nCycle; i++) {

                ArrayList<Task> tasks = taskGen.generate();
                
                Util.log().info("Generating tasks: " + tasks.size() + " tasks");

                // print generated tasks
                System.out.println("=== Generated tasks ===");
                for (Task t : tasks) {
                    System.out.println(t.detail());
                }
                System.out.println("==========================");

                for (Task t : tasks) {
                    nTask++;
                    Util.log().info("Submitting " + t);
                    // update deadline relative to current time
                    double deadline = (double)t.getSpecification()
                            .findObjective("deadline") // deadline spec must exist
                            .getValue();
                    deadline += Sim_system.clock();
                    t.getSpecification().findObjective("deadline").setValue(deadline);
                    submitTask(t); 
                }
                
                super.sim_pause(waitBetweenCycles);
                
            }
            
            // receiving results
            Util.log().info("Awaiting result");
            List<Task> list = new ArrayList<Task>();
            for (int i=0; i<nTask; i++) {
                Gridlet gl = super.gridletReceive();
                Util.log().info("Receiving Gridlet " + gl);
                if (gl!=null) list.add(((GSTask)gl).getTask());
            }
            
            // shutting down gsMiddleware user
            Util.log().info("SHUTTING DOWN");
            super.send(gsMiddleware.getUser().get_id(), GridSimTags.SCHEDULE_NOW, GridSimTags.END_OF_SIMULATION, null);
            super.send(gsMiddleware.get_id(), GridSimTags.SCHEDULE_NOW, GridSimTags.END_OF_SIMULATION, null);
            
            shutdownGridStatisticsEntity();
            shutdownUserEntity();
            terminateIOEntities();

            System.out.println("RESULT: ");
            for (Task task: list) {
                System.out.println(task);
                String stat = task.getStat().dump();
                System.out.println(stat);
            }
            
            // dump weka file
            String exportTo = Util.getProperty(configFile, "export_arff_to");
            if (exportTo!=null){
                Util.log().info("DUMPING WEKA ARFF");
                WekaExporter.export(list, exportTo);
            }
            

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    protected void submitTask(Task task) {
        int SIZE = 12; // int size
        GSTask gsTask = new GSTask(task);
        gsTask.setUserID(get_id());
        IO_data data = new IO_data(gsTask, SIZE, GridSim.getGISId());
        super.send( super.output, GridSimTags.SCHEDULE_NOW, GSConstants.SUBMIT_TASK, data);
    }

    public static void start(String configurationFile,
            SchedulerInterface scheduler,
            ServiceManagerInterface manager,
            ConfigJson taskGeneratorConfig,
            ConfigJson serviceGeneratorConfig) {
        
        Util.log().info("Initializing " + NAME);

        try {
            
            configFile = configurationFile;
            
            // number of grid users, i.e., itself
            // this is necessary for GridSim for waiting until all users finish
            int numUser = 2;   
            
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = true ;  // true means trace GridSim events

            Util.log().info("Initializing GridSim package");

            GridSim.init(numUser, calendar, traceFlag, false);

            // Create a new Middleware as GIS entity    
            gsMiddleware = new GSMiddleware(manager, scheduler);
            GridSim.setGIS(gsMiddleware);

            // Creates grid resource entities (i.e., HCU services)
            numServices = generateServices(serviceGeneratorConfig);

            // Creates this consumer, as a grid user
            GSConsumer user = new GSConsumer(taskGeneratorConfig);
            // once created, it will be registered to the infoService, 
            // and body() will be executed when the simulation starts 

            // start simulation
            Util.log().info("Starting simulation");
            boolean debug = Boolean.parseBoolean(Util.getProperty(configFile, "debug"));
            GridSim.startGridSimulation(debug);
            
            // simulation finished
            Util.log().info(NAME + " finishes.");
        }
        catch (Exception e) {
            e.printStackTrace();
            Util.log().severe("Unwanted errors happen");
        }
    }

    private static int generateServices(ConfigJson serviceGeneratorConfig) {
        int numServices = 0;
        try {
            ServiceGenerator serviceGen = new ServiceGenerator(serviceGeneratorConfig);
            ArrayList<Service> services = serviceGen.generate();
            for (Service service : services) {
                GSService gsservice = new GSService(service);
                // once created, it is automatically registered by GridSim
                // and eventually registered to GSMiddleware
                numServices++;
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numServices;
    }
}
