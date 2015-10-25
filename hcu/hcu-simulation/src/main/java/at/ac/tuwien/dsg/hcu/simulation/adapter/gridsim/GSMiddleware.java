package at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridInformationService;
import gridsim.GridSim;
import gridsim.GridSimTags;

import java.util.Hashtable;

import at.ac.tuwien.dsg.hcu.common.interfaces.CloudUserInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ServiceManagerInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Assignment.Status;
import at.ac.tuwien.dsg.hcu.common.model.ComputingElement;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.monitor.stream.EventType;
import at.ac.tuwien.dsg.hcu.monitor.stream.UnitStream;
import at.ac.tuwien.dsg.hcu.util.Util;

public class GSMiddleware extends GridInformationService implements CloudUserInterface {

    protected ServiceManagerInterface manager;
    protected SchedulerInterface scheduler;
    protected MonitorInterface monitor;
    
    protected Hashtable<Integer, GSTask> taskList;
    protected Hashtable<Integer, Integer> ownerList;
    protected GSUser user;

    public GSMiddleware(ServiceManagerInterface manager, SchedulerInterface scheduler, MonitorInterface monitor) throws Exception {
        super("GSMiddleware", GridSimTags.DEFAULT_BAUD_RATE);
        this.manager = manager;
        this.scheduler = scheduler;
        this.monitor = monitor;
        scheduler.setCloudUserInterface(this);
        scheduler.setMonitorInterface(monitor);
        this.taskList = new Hashtable<Integer, GSTask>();
        this.ownerList = new Hashtable<Integer, Integer>();
        user = new GSUser(this, monitor);
    }

    @Override
    protected void processEvent(Sim_event ev) {
        super.processEvent(ev);
        switch (ev.get_tag())  {

            // Handle registration
            case GridSimTags.REGISTER_RESOURCE:
            case GridSimTags.REGISTER_RESOURCE_AR:
                int id = ( (Integer) ev.get_data() ).intValue();
                if (Sim_system.get_entity(id) instanceof GSService) {
                    GSService service = (GSService)Sim_system.get_entity(id);
                    handleRegistration(service);       
                }
                break;

            case GSConstants.SUBMIT_TASK:
                GSTask task = (GSTask)ev.get_data();
                Util.log().info("SUBMIT_TASK " + task);
                handleSubmittedTask(task);
                break;
                
            case GSConstants.RETURN_ASSIGNMENT:
                Assignment returnedAssignment = (Assignment)ev.get_data();
                Util.log().info("RETURN_ASSIGNMENT " + returnedAssignment);
                handleFinishedAssignment(returnedAssignment);
                break;

            case GSConstants.FORWARD_ASSIGNMENT:
                GSAssignment forwardedAssignment = (GSAssignment)ev.get_data();
                Util.log().info("FORWARD_ASSIGNMENT " + forwardedAssignment);
                reserveAssignment(forwardedAssignment.getAssignment());
                break;

            case GSConstants.WAKE_SCHEDULER:
                scheduler.wake();
                break;

            case GridSimTags.END_OF_SIMULATION:
                Util.log().info("END OF SIMULATION, dest id = " + ev.get_dest());
                super.send(user.get_id(), GridSimTags.SCHEDULE_NOW, GridSimTags.END_OF_SIMULATION, null);
                break;
        }
    }
    
    private void handleRegistration(GSService service) {
        ComputingElement element = manager.retrieveElement(service.getService().getProvider().getId());
        if (element==null) {
            // new unit, send Unit CREATED event
            monitor.sendEvent(new UnitStream(EventType.CREATED, GridSim.clock(), element));
        }
        manager.registerService(service.getService());
    }
    
    private void handleSubmittedTask(GSTask task) {
        taskList.put(task.getTask().getId(), task);
        ownerList.put(task.getGridletID(), task.getUserID());
        task.setUserID(get_id());
        
        // now, let the scheduler handles it
        //task.getTask().setSubmissionTime(Sim_system.clock() + 5); // give some room for processing
        task.getTask().setSubmissionTime(GridSim.clock());
        scheduler.submitTask(task.getTask());
    }
    
    private void handleFinishedAssignment(Assignment assignment) {
        //assignment.setFinishTime(Sim_system.clock() + 5);
        assignment.setFinishTime(GridSim.clock());
        scheduler.notifyExecutionResult(assignment, assignment.getStatus());
    }

    // Only for testing
    protected void processOtherEvent(Sim_event ev)
    {
        int resID = 0;          // sender ID
        String name = null;     // sender name

        switch ( ev.get_tag() )
        {
            case 99001:
                resID = ( (Integer) ev.get_data() ).intValue();
                name = GridSim.getEntityName(resID);
                System.out.println(super.get_name() + 
                        ": Received 99001 tag from " + name +
                        " at time " + GridSim.clock());
                break;
                
            case 99002:
                resID = ( (Integer) ev.get_data() ).intValue();
                name = GridSim.getEntityName(resID);
                System.out.println(super.get_name() + 
                        ": Received 99002 tag from " + name + 
                        " at time " + GridSim.clock());
                break;

            default:
                break;
        }

    }
    
    
    public GSUser getUser() {
        return user;
    }

    @Override
    public void reserveAssignment(Assignment assignment) {
        super.send(user.get_id(), GridSimTags.SCHEDULE_NOW, GSConstants.RESERVE_ASSIGNMENT, assignment);
    }

    @Override
    public void commitAssignment(Assignment assignment) {
        assignment.setCommitTime(GridSim.clock());
        super.send(user.get_id(), GridSimTags.SCHEDULE_NOW, GSConstants.COMMIT_ASSIGNMENT, assignment);
    }

    @Override
    public void notifyTaskResult(Task task) {
        Util.log().info("Notifying task result, status = " + task.getStatus() + " for " + task);
        Integer ownerId = ownerList.get(task.getId());
        GSTask gsTask = taskList.get(task.getId());
        if (ownerId==null || gsTask==null) {
            Util.log().info("Task with id " + task.getId() + " can't be found on the list");
        } else {
            super.send(ownerId, GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_RETURN, gsTask);
        }
    }

    @Override
    public void pauseAssignment(Assignment assignment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resumeAssignment(Assignment assignment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Status queryAssignment(Assignment assignment) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void wakeSchedulerAt(long time) {
        super.sim_schedule(get_id(), time, GSConstants.WAKE_SCHEDULER);
    }

    @Override
    public void notifyFinalizeAssignment(Assignment assignment) {
        // TODO Auto-generated method stub
        
    }

}
