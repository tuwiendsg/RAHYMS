package at.ac.tuwien.dsg.salam.simulation.adapter.gridsim;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_exception;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.parallel.profile.TimeSlot;
import gridsim.parallel.reservation.ErrorType;
import gridsim.parallel.reservation.MessageType;
import gridsim.parallel.reservation.Reservation;
import gridsim.parallel.reservation.ReservationMessage;
import gridsim.parallel.reservation.ReservationRequester;

import java.util.Collection;
import java.util.Hashtable;

import at.ac.tuwien.dsg.salam.common.model.Assignment;
import at.ac.tuwien.dsg.salam.common.model.Assignment.Status;
import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.Service;
import at.ac.tuwien.dsg.salam.util.Util;

public class GSUser extends ReservationRequester {

    private GSMiddleware middleware;
    
    private static int uniqueId = 0;
    
    // keep reservation list by the assignment id
    private Hashtable<Integer, Reservation> reservationList;
    private Hashtable<Integer, GSAssignment> gridletList;
    
    public GSUser(GSMiddleware middleware) throws Exception, Sim_exception {
        super("GSUser_" + (++uniqueId), 560);
        this.middleware = middleware;
        this.reservationList = new Hashtable<Integer, Reservation>();
        this.gridletList = new Hashtable<Integer, GSAssignment>();
    }
    
    public void reserveAssignment(Assignment assignment) {

        GSAssignment gsAssignment = new GSAssignment(assignment);
        Service destination = assignment.getAssignee();
        double duration = assignment.getForecastedDuration();
        int destId = destination.getId();
        double startTime = assignment.getStartTime();
        
        // make the reservation
        int numPE = 1;
        Util.log().info("Sending reservation to " + destId + ", " + assignment);
        // give some time, since simulation time may have been shifted a bit
        duration = duration + 3;
        startTime = startTime + 2;
        Reservation reservation = super.createReservation(startTime, (int)duration, numPE, destId);
        Util.log().info("Reservation result = " + reservation +
                ", for " + assignment);
        
        // commit reservation
        if (reservation==null) {
            Util.log().severe("Reservation Error");;
        } else {
            reservationList.put(assignment.getId(), reservation);
            gridletList.put(assignment.getId(), gsAssignment);
        }

        // add assignment count
        ComputingElement provider = assignment.getAssignee().getProvider();
        provider.addAssignmentCount();
    }
    
    public void commitAssignment(Assignment assignment) {
        Reservation reservation = reservationList.get(assignment.getId());
        GSAssignment gsAssignment = gridletList.get(assignment.getId());
        if (reservation==null) {
            Util.log().severe("Reservation not found for assignment " + assignment);
        } else {
            boolean commitResult = super.commitReservation(reservation.getID());
            if (commitResult) {
                gsAssignment.setReservationID(reservation.getID());
                gsAssignment.setUserID(get_id());
                super.gridletSubmit(gsAssignment, assignment.getAssignee().getId());
                ComputingElement provider = assignment.getAssignee().getProvider();
                //System.out.println("Realibility of " + provider.getName() + "(k=" + (provider.getAssignmentCount()+1) + ") = " + provider.getMetrics().getValue("reliability", new Object[]{GridSim.clock(), true}) );
                //provider.addAssignmentCount();
            } else {
                Util.log(this.getClass().getName()).severe("Error committing assignment " + assignment + " on reservation " + reservation);
                assignment.setStatus(Status.FAILED);
                returnAssignment(assignment);
            }
        }
    }
    
    public void queryResponseTimeForecast(GSAssignment gsAssignment, int requesterId) {
        Collection<TimeSlot> resOptions = null;
        try {
            
            // create a reservation message
            Assignment assignment = gsAssignment.getAssignment();
            Service destination = assignment.getAssignee();
            Reservation reservation = new Reservation(super.get_id());
            reservation.setStartTime(assignment.getStartTime());
            reservation.setDurationTime((int) Math.ceil(assignment.getForecastedDuration()));
            reservation.setResourceID(destination.getId());
            ReservationMessage message = new ReservationMessage(super.get_id(), destination.getId(), reservation);
            message.setMessageType(MessageType.FORECAST_RESPONSE_TIME);
            
            // gets the reply from the grid resource
            ReservationMessage reply = sendReservationMessage(message);
            
            // If error code is NO_ERROR, the message has been successful.
            ErrorType error = (reply == null) ? 
                    ErrorType.OPERATION_FAILURE : reply.getErrorCode();
            
            if(error == ErrorType.NO_ERROR && reply != null) {
                resOptions = reply.getReservation().getReservationOptions();
            }
            else {
                System.out.println("Service " + destination + " could not inform the " +
                        "response time at time " + GridSim.clock());
                resOptions = null;
            }
        }
        
        catch (Exception ex) {
            Util.log().severe("Service " + gsAssignment.getAssignment().getAssignee() +
                    " could not inform the response time " + ex);
            resOptions = null;
        }
        
        Double responseTime = null;
        if (resOptions.size()>0) {
            responseTime = ((TimeSlot)(resOptions.toArray()[0])).getFinishTime();
        }
        
        // response back to requester
        super.send(requesterId, GridSimTags.SCHEDULE_NOW, 
                GSConstants.FORECAST_RESPONSE_TIME_RESULT, responseTime);
    }
    
    public void returnAssignment(Assignment assignment) {
        if (middleware!=null) {
            Util.log().info("Returning assignment to middleware");
            super.send(middleware.get_id(), GridSimTags.SCHEDULE_NOW, GSConstants.RETURN_ASSIGNMENT, assignment);
        }
    }

    public void body() {
        
        // loop to get events, especially returned gridlet
        Sim_event ev = new Sim_event();
        boolean stop = false;
        while (Sim_system.running() && !stop) {
            super.sim_get_next(ev);
            switch (ev.get_tag()) {
                case GridSimTags.END_OF_SIMULATION:
                    Util.log().info("END OF SIMULATION");
                    Util.log().info(new Boolean(Sim_system.running()).toString());
                    stop = true;
                    break;
                case GridSimTags.GRIDLET_RETURN:
                    GSAssignment returnedAssignment = (GSAssignment)ev.get_data();
                    Util.log().info("GRIDLET_RETURN, " + returnedAssignment.getGridletStatusString() + ", " + returnedAssignment);
                    Assignment assignment = returnedAssignment.getAssignment();
                    if (returnedAssignment.getGridletStatus()==Gridlet.SUCCESS) {
                        assignment.setStatus(Status.SUCCESSFUL);
                    } else {
                        assignment.setStatus(Status.FAILED);
                    }
                    returnAssignment(assignment);
                    break;
                case GSConstants.RESERVE_ASSIGNMENT:
                    Assignment assignment1 = (Assignment)ev.get_data();
                    Util.log().info("RESERVE_ASSIGNMENT, " + assignment1);
                    reserveAssignment(assignment1);
                    assignment1.setStatus(Status.RESERVED);
                    break;
                case GSConstants.COMMIT_ASSIGNMENT:
                    Assignment assignment2 = (Assignment)ev.get_data();
                    Util.log().info("COMMIT_ASSIGNMENT, " + assignment2);
                    commitAssignment(assignment2);
                    assignment2.setStatus(Status.READY);
                    break;
                case GSConstants.FORECAST_RESPONSE_TIME:
                    GSAssignment assignment3 = (GSAssignment)ev.get_data();
                    Util.log().info("FORECAST_RESPONSE_TIME recieved");
                    queryResponseTimeForecast(assignment3, ev.get_src());
                    break;
                case GSConstants.HELLO_TAG:
                    Object msg = ev.get_data();
                    Util.log().info("HELLO recieved = " + msg + 
                            ", from = " + ev.get_src());
                    break;
                default:
                    Object msg1 = ev.get_data();
                    Util.log().info(ev.get_tag() + ": " + msg1);
                    break;
            }
        }

        terminateIOEntities();
        Util.log().info("Exiting body of " + get_name() + " ThreadId=" + this.getId() + ", entityId=" + this.get_id());            

    }

}
