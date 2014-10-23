package at.ac.tuwien.dsg.salam.simulation.adapter.gridsim;

import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.parallel.SSGridlet;
import gridsim.parallel.gui.ActionType;
import gridsim.parallel.gui.Visualizer;
import gridsim.parallel.profile.PERangeList;
import gridsim.parallel.profile.ProfileEntry;
import gridsim.parallel.profile.TimeSlot;
import gridsim.parallel.reservation.ErrorType;
import gridsim.parallel.reservation.Reservation;
import gridsim.parallel.reservation.ReservationMessage;
import gridsim.parallel.reservation.ReservationStatus;
import gridsim.parallel.reservation.ServerReservation;
import gridsim.parallel.scheduler.ARConservativeBackfill;
import gridsim.parallel.scheduler.ConservativeBackfill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.PropertyTypes;
import at.ac.tuwien.dsg.salam.common.model.Service;
import at.ac.tuwien.dsg.salam.util.Util;

public class GSReservationPolicy extends ARConservativeBackfill {

    protected static Hashtable<Long, GSReservationPolicy> instances
        = new Hashtable<Long, GSReservationPolicy>();
    
    protected ComputingElement owner;
    
    public GSReservationPolicy(ComputingElement owner)
            throws Exception {
        super(owner.getName(), "scheduler");
        this.owner = owner;
    }
    
/*
    public void gridletSubmit(Gridlet gl, boolean ack) {
        super.gridletSubmit(gl, ack);
        GSAssignment assignment = (GSAssignment)gl;
        Util.log().info("Gridlet recieved " + assignment.getAssignment());
        sendFinishGridlet(assignment);
    }
*/    
    public double forecastExecutionTime(double load) {
        double rating = Util.Double(owner.getProperties().getValue(PropertyTypes.PROP_PERFORMANCE, 1.0));
        if (rating==0.0) rating = 1.0;
        return load / rating;
    }
    
    public synchronized double forecastResponseTime(double startTime, 
            double load, double duration) {
        // if duration is specified, we dont need to forecast again the duration of execution time
        double execTime = (duration > 0) ? duration : forecastExecutionTime(load);
        double earliestTime = forecastEarliestAvailability(startTime, execTime);
        return earliestTime + execTime;
    }
    
    public synchronized double forecastEarliestAvailability(double startTime, 
            double duration) {
        Collection<TimeSlot> slots = 
                super.profile.getTimeSlots(startTime, Integer.MAX_VALUE);
        double earliestTime = 0;
        for (TimeSlot slot: slots) {
            if (slot.getDuration() > duration + 1) {
                earliestTime = slot.getStartTime();
                break;
            }
        }
        earliestTime = earliestTime + 1;
        //System.out.println(this.get_name() + ", start = " + startTime + ", duration = " + duration + ", earliest = " + earliestTime);
        return earliestTime;
    }

    public ReservationMessage handleForecastResponseTime(ReservationMessage message) {
        
        double startTime = message.getReservation().getStartTime();
        double duration = message.getReservation().getDurationTime();
        Double forecastedResponseTime = forecastResponseTime(startTime, 0, duration);

        // send response
        ReservationMessage response = message.createResponse();
        ArrayList<TimeSlot> options = new ArrayList<TimeSlot>();
        options.add(new TimeSlot(startTime, forecastedResponseTime, new PERangeList(0, 0)));
        response.getReservation().setReservationOptions(options);
        return response;
        
    }

    //public void processOtherEvent(Sim_event ev) {
        //super.processOtherEvent(ev);
    //}
    
    protected double forecastFinishTime(double availableRating, double length) {
        double finishTime = (length / availableRating);
        if (finishTime < 1.0) {
            finishTime = 1.0;
        }
        return finishTime;
    }
    
    public static GSReservationPolicy getInstance(Service service) throws Exception {
        return getInstance(service.getProvider());
    }

    public static GSReservationPolicy getInstance(ComputingElement provider) throws Exception {
        GSReservationPolicy policy = null;
        long providerId = provider.getId();
        if (instances.containsKey(providerId)) {
            policy = instances.get(providerId);
        } else {
            policy = new GSReservationPolicy(provider);
            instances.put(providerId, policy);
        }
        return policy;
    }

    /**
     * Handles an advance reservation request.
     * If we can't reserve exactly on the requested time, find next earliest slot
     * @param message the advance reservation message received
     * @return <code>true</code> if the reservation was accepted; 
     * <code>false</code> otherwise.
     */
    public synchronized ReservationMessage createReservation(ReservationMessage message) {
        
        // copy some ARConservativeBackfile private members here
        Visualizer visualizer = GridSim.getVisualizer();
        int commitPeriod = 30*60;
        int EXPIRY_TIME = 12;
        
        Reservation reserv = message.getReservation();
        ServerReservation sRes = new ServerReservation(reserv); 
        
        //-------------- FOR DEBUGGING PURPOSES ONLY ---------------
        visualizer.notifyListeners(this.get_id(), ActionType.ITEM_ARRIVED, true, sRes);
        //----------------------------------------------------------

        ReservationMessage response = message.createResponse(); // creates a response

        if (sRes.getNumPE() > super.totalPE_) {
            Util.log().warning("Not enough PEs available ");
            reserv.setStatus(ReservationStatus.FAILED);
            response.setErrorCode(ErrorType.OPERATION_FAILURE);
            return response;
        }

        double currentTime = GridSim.clock();
        double startTime = Math.max(reserv.getStartTime(), currentTime);
        double originalStartTime = startTime;
        //TODO: To check this exptime (Marcos)
        double expTime = sRes.getActualFinishTime();
        int duration = reserv.getDurationTime();
        sRes.setStartTime(startTime);
        
        // check the availability in the profile
        ProfileEntry entry = profile.checkAvailability(reserv.getNumPE(), startTime, duration);

        // if entry is null, it means that there are not enough PEs 
        if(entry == null) {
            // find next possible availability
            double earliestStartTime = forecastEarliestAvailability(startTime, duration);
            if(earliestStartTime > 0) {
                entry = profile.checkAvailability(reserv.getNumPE(), earliestStartTime, duration);
                if (entry!=null) {
                	startTime = earliestStartTime;
                	sRes.setStartTime(earliestStartTime);
                    Util.log().info("Reservation #" + reserv.getID() + " from " + 
                            GridSim.getEntityName(message.getSourceID()) + 
                            ", startTime moved from " + originalStartTime + " to " + earliestStartTime);
                }
            }
        }
            
        // still can't find a slot, return error
        if(entry == null) {
            Util.log().warning("Reservation #" + reserv.getID() + " from " + 
                    GridSim.getEntityName(message.getSourceID()) + 
                    " user requires " + reserv.getNumPE() + 
                    " PEs from " + originalStartTime + " to " + (originalStartTime + duration) + 
                    " could not be accepted");

            response.setErrorCode(ErrorType.OPERATION_FAILURE);
        } else { 
            PERangeList selected = entry.getAvailRanges().selectPEs(reserv.getNumPE());
            profile.allocatePERanges(selected, startTime, sRes.getExpectedFinishTime());
            sRes.setPERangeList(selected);
            
            if(Double.compare(currentTime, startTime)  == 0) {
                // if start time = current time, reservation is immediate. A event
                // is scheduled to start the reservation and it commits the reservation
                sRes.setStatus(ReservationStatus.COMMITTED);
                super.sendInternalEvent(GridSimTags.SCHEDULE_NOW, ConservativeBackfill.UPT_SCHEDULE);
            } else {
                // expiration time is used only for non-immediate reservations
                expTime = Math.min(startTime, currentTime + commitPeriod);
                expTime = expTime + 1; // shift a bit to make a chance for late assignments
                
                sRes.setStatus(ReservationStatus.NOT_COMMITTED);
                // to check for expired reservations
                super.sendInternalEvent(expTime - currentTime, EXPIRY_TIME);
            }
            
            sRes.setExpiryTime(expTime);
            reservTable.put(new Integer(sRes.getID()), sRes);

            //-------------- FOR DEBUGGING PURPOSES ONLY  --------------
            visualizer.notifyListeners(this.get_id(), ActionType.ITEM_SCHEDULED, true, sRes);
            //----------------------------------------------------------
        }
        
        return response;
    }

    /*
     * Schedules a new job received by the Grid resource entity and 
     * for which an advance reservation has been made.
     * @param gridlet a job object to be executed
     */
    public synchronized boolean handleReservationJob(Gridlet gridlet) {
        
        // copy some ARConservativeBackfile private members here
        Visualizer visualizer = GridSim.getVisualizer();
        int commitPeriod = 30*60;
        int EXPIRY_TIME = 12;

        int reqPE = gridlet.getNumPE();
        SSGridlet sgl = new SSGridlet(gridlet);
        long runTime = super.forecastExecutionTime(ratingPE, sgl.getLength());
        ServerReservation sRes = reservTable.get(gridlet.getReservationID());
        
        //-------------------- FOR DEBUGGING PURPOSES ONLY ---------------------
        visualizer.notifyListeners(this.get_id(), ActionType.ITEM_ARRIVED, true, sgl);
        //----------------------------------------------------------------------
        
        if(sRes == null) {
            String userName = GridSim.getEntityName( gridlet.getUserID() );
            Util.log().info("Gridlet #" + gridlet.getGridletID() + " from " +
                    userName + " cannot be accepted because the reservation #" +
                    gridlet.getReservationID() + " has not been found.");
            return false;
        }
        // job requires more PEs than what the reservation currently has
        else if( sRes.getNumRemainingPE() < reqPE ) {
            String userName = GridSim.getEntityName( gridlet.getUserID() );
            Util.log().info("Gridlet #" + gridlet.getGridletID() + " from " +
                    userName + " cannot be accepted because the reservation #" +
                    sRes.getID() + " has only " + sRes.getNumRemainingPE() + " PEs.");
            return false;
        }
        
        // job is expected to run for longer than the time previously reserved
        else if (runTime > sRes.getRemainingTime()) {
            // TODO: reschedule 
            String userName = GridSim.getEntityName( gridlet.getUserID() );
            Util.log().info("Gridlet #" + gridlet.getGridletID() + " from " +
                    userName + " cannot be accepted because the reservation #" +
                    sRes.getID() + " has a remaining time of " + 
                    sRes.getRemainingTime() + " seconds," +
                    " whereas the gridlet is expected to run for " +
                    runTime + " seconds.");
            return false;
        }

        double startTime = Math.max(sRes.getStartTime(), GridSim.clock());
        PERangeList selected = sRes.selectPERangeList(reqPE);
        sgl.setPERangeList(selected);
        sgl.setStartTime(startTime);
        sgl.setActualFinishTime(startTime + runTime);
        sgl.setStatus(Gridlet.QUEUED);
        waitingJobs.add(sgl);
        
        // if reservation has not been committed, then commit the reservation
        if (sRes.getReservationStatus() == ReservationStatus.NOT_COMMITTED) {
            sRes.setStatus(ReservationStatus.COMMITTED);
            super.sendInternalEvent(sRes.getStartTime() - GridSim.clock(), 
                    ConservativeBackfill.UPT_SCHEDULE);
        }
        
        super.sendInternalEvent(startTime - GridSim.clock(), 
                ConservativeBackfill.UPT_SCHEDULE);
        
        //------------------ FOR DEBUGGING PURPOSES ONLY ----------------
        visualizer.notifyListeners(this.get_id(), ActionType.ITEM_SCHEDULED, true, sgl);
        //---------------------------------------------------------------
        return true;
    }
}
