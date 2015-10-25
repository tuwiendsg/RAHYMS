package at.ac.tuwien.dsg.hcu.cloud.scheduler;

import gridsim.GridSim;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

import at.ac.tuwien.dsg.hcu.common.interfaces.CloudUserInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;
import at.ac.tuwien.dsg.hcu.common.interfaces.SchedulerInterface;
import at.ac.tuwien.dsg.hcu.common.model.Assignment;
import at.ac.tuwien.dsg.hcu.common.model.Assignment.Status;
import at.ac.tuwien.dsg.hcu.common.model.Batch;
import at.ac.tuwien.dsg.hcu.common.model.Role;
import at.ac.tuwien.dsg.hcu.common.model.SCU;
import at.ac.tuwien.dsg.hcu.common.model.Task;
import at.ac.tuwien.dsg.hcu.monitor.stream.AssignmentStream;
import at.ac.tuwien.dsg.hcu.monitor.stream.CollectiveStream;
import at.ac.tuwien.dsg.hcu.monitor.stream.EventType;
import at.ac.tuwien.dsg.hcu.util.ShowGraphApplet;
import at.ac.tuwien.dsg.hcu.util.Util;

public class Scheduler implements SchedulerInterface {

    protected final static int MAX_RETRY = 3;
    
    protected Queue<Task> queueList;
    protected List<SCU> runningList;
    protected List<SCU> successList;
    protected List<SCU> failList;
    protected Hashtable<Integer,Integer> retryCounter;
    
    // to keep assignments ready to commit, i.e.:
    // - kicking of root assignments
    // - assignments whose dependencies already finished
    protected Queue<Assignment> readyAssignments;
    
    protected ComposerInterface composer;
    protected DependencyProcessorInterface dp;
    protected CloudUserInterface cloudUserInterface;
    protected MonitorInterface monitor;
    
    public Scheduler(ComposerInterface composer, DependencyProcessorInterface dp) {
        queueList = new LinkedList<Task>();
        runningList = new LinkedList<SCU>();
        successList = new LinkedList<SCU>();
        failList = new LinkedList<SCU>();
        readyAssignments = new LinkedList<Assignment>();
        retryCounter = new Hashtable<Integer,Integer>();
        this.composer = composer;
        this.dp = dp;
    }
    
    public void queue(Task task) {
        // verify dependencies
        List<Assignment> assignments = task.createEmptyAssignments();
        if (!dp.verify(assignments)) {
            Util.log().severe("INVALID dependency detected! Roles dependencies must be acyclic and does not have multi-edges.");

            // debug, show the graph
            ListenableDirectedWeightedGraph<Assignment, DefaultWeightedEdge> graph = dp.generateGraph(assignments);
            if (graph!=null) {
                ShowGraphApplet<Assignment, DefaultWeightedEdge> applet = new ShowGraphApplet<Assignment, DefaultWeightedEdge>();
                applet.showGraph(graph);
            } else {
                System.out.println("Invalid dependencies");
            }
            
        } else {
            Util.log().info("Adding " + task + " to scheduler queue");
            queueList.add(task);
            task.setStatus(Task.Status.QUEUED);
        }
    }

    public void processQueue() {
        
        synchronized (queueList) {
            
            Util.log().info("Processing scheduling queue, size = " + queueList.size());
            
            Task task = queueList.poll();
            while (task!=null) {
            
                // try to compose
                List<Assignment> assignments = composer.compose(task, GridSim.clock());
                
                if (assignments==null || assignments.size()==0) {
                    // unable to compose an SCU, put back at the end of the queue for retries
                    Integer retry = retryCounter.get(task.getId());
                    if (retry==null) retry = 0;
                    if (retry < MAX_RETRY) {
                        queue(task);
                        retryCounter.put(task.getId(), retry + 1);
                    } else {
                        task.setStatus(Task.Status.FAILED);
                        cloudUserInterface.notifyTaskResult(task);
                    }
                } else {
                    // fill up start time
                    dp.forecastStartTimeForAllAssignment(assignments);
                    for (Assignment a: assignments) { 
                        Util.log().info(a.toString());
                    }
                    // create SCU
                    Batch batch = new Batch(assignments);
                    batch.setTask(task);
                    SCU scu = new SCU(batch);
                    batch.setRunningSCU(scu);
                    // deploy
                    deploySCU(scu);
                    runningList.add(scu);
                    task.setStatus(Task.Status.RUNNING);
                    // Collective CREATED event
                    monitor.sendEvent(new CollectiveStream(EventType.CREATED, GridSim.clock(), scu));
                }

                task = queueList.poll();
            }
        }
        
    }
    
    public void processWaitingAssignments() {
        synchronized (readyAssignments) {
            Assignment assignment = readyAssignments.poll();
            while (assignment!=null) { 
                // TODO: reschedule if reservation has been canceled
                cloudUserInterface.commitAssignment(assignment);
                assignment = readyAssignments.poll();
            }
        }
    }
    
    public void deploySCU(SCU scu) {
        // reserve
        for (Assignment assignment: scu.getBatch().getAssignments()) {
            cloudUserInterface.reserveAssignment(assignment);
        }
        // getting root assignments
        List<Assignment> root = dp.findNonDependantAssignments(scu.getBatch().getAssignments());
        readyAssignments.addAll(root);
        // kicking off the root assignment
        processWaitingAssignments();
    }
    
    public void handleSuccessfulAssignment(Assignment assignment) {
        int index = findSCUFromList(assignment.getBatchId(), runningList);
        if (index==-1) {
            Util.log().info("BatchId " + assignment.getBatchId() + " can't be found on the runningList");
        } else {
            // get further assignments to be executed
            SCU scu = runningList.get(index);
            Batch batch = scu.getBatch();
            List<Assignment> assignments = batch.getAssignments();
            // record statistics
            batch.getTask().getStat().recordAssignment(assignment, assignments, assignment.getFinishTime());
            // start next assignment
            List<Assignment> next = dp.startAfter(assignments, assignment);
            if (next!=null && next.size()>0) {
                readyAssignments.addAll(next);
                // Collective TRANSITIONED event
                monitor.sendEvent(new CollectiveStream(EventType.TRANSITIONED, GridSim.clock(), scu));                
            } else {
                // no further assignment depends on this assignment
                // check if all other assignments finished
                Status batchStatus = checkStatus(batch);
                if (batchStatus!=Status.RUNNING) {
                    EventType eventType = null;
                    if (batchStatus==Status.SUCCESSFUL) {
                        batch.getTask().setStatus(Task.Status.SUCCESSFUL);
                        eventType = EventType.FINISHED;
                    }
                    if (batchStatus==Status.FAILED) {
                        batch.getTask().setStatus(Task.Status.FAILED);
                        eventType = EventType.FAILED;
                    }
                    cloudUserInterface.notifyTaskResult(batch.getTask());
                    // record statistics
                    batch.getTask().getStat().recordFinishTime(assignment.getFinishTime());
                    // Collective FINISHED/FAILED event
                    monitor.sendEvent(new CollectiveStream(eventType, GridSim.clock(), scu));                
                }
            }
        }
        processWaitingAssignments();
    }
    
    private int findSCUFromList(int batchId, List<SCU> list) {
        // create dummy SCU object, scu uses batchId for comparison
        SCU scu = new SCU(new Batch(batchId));
        return list.indexOf(scu);
    }
    
    private Status checkStatus(Batch batch) {
        Status status = Status.SUCCESSFUL;
        for (Assignment a: batch.getAssignments()) {
            if (a.getStatus()==Status.FAILED) {
                status = Status.FAILED; // if any assignment is failed, the batch failed
            } else if (a.getStatus()!=Status.SUCCESSFUL) {
                status = Status.RUNNING; // actually, some can be also reserved or ready
                break;
            }
        }
        return status;
    }
    
    public void rescheduleAssignment(Assignment assignment) {
        
        // TODO: count retry
        
        int index = findSCUFromList(assignment.getBatchId(), runningList);
        if (index==-1) {
            Util.log().info("BatchId " + assignment.getBatchId() + " can't be found on the runningList");
        } else {
            
            // get batch
            SCU scu = runningList.get(index);
            Batch batch = scu.getBatch();
        
            // partial compose rescheduled assignment
            Task task = assignment.getTask();
            Role role = assignment.getRole();
            ArrayList<Role> roles = new ArrayList<Role>();
            roles.add(role);
            List<Assignment> assignments = composer.partialCompose(task, roles, GridSim.clock());
            
            if (assignments==null || assignments.size()==0) {
                // TODO: return failure to the consumer
            } else {
                Assignment a = assignments.get(0); // only one assignment is being rescheduled
                Util.log().info("Rescheduling " + a);
                // get start time and reserve 
                double startTime = dp.forecastEarliestStartTime(a);
                a.setStartTime(startTime);
                // swap assignment in batch
                batch.replaceAssignment(assignment, a);
                // Collective MODIFIED event
                monitor.sendEvent(new CollectiveStream(EventType.UPDATED, GridSim.clock(), scu));                
                // reserve
                cloudUserInterface.reserveAssignment(a);
                // kick off
                readyAssignments.add(a);
                processWaitingAssignments();
            }
        }
    }

    @Override
    public void setCloudUserInterface(CloudUserInterface cloudUserInterface) {
        this.cloudUserInterface = cloudUserInterface;
    }
    
    @Override
    public void setMonitorInterface(MonitorInterface monitorInterface) {
    	this.monitor = monitorInterface;
    }

    @Override
    public void submitTask(Task task) {
        queue(task);
        processQueue();
    }

    @Override
    public void notifyExecutionResult(Assignment assignment, Status status) {
        switch (status) {
            case SUCCESSFUL:
                handleSuccessfulAssignment(assignment);
                break;
            default:
                // Disable rescheduling, for reliability analysis
                rescheduleAssignment(assignment);                
                break;
        }
            
    }

    @Override
    public void notifyReservationResult(Assignment assignment, Status status) {
        // TODO: implement!
    }

    @Override
    public void wake() {
        processQueue();
        processWaitingAssignments();
    }
    
    
}