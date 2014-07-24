package scu.adapter.gridsim;

import eduni.simjava.Sim_event;
import gridsim.AllocPolicy;
import gridsim.GridSimTags;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;
import gridsim.parallel.ParallelResource;
import gridsim.parallel.reservation.MessageType;
import gridsim.parallel.reservation.ReservationMessage;
import gridsim.parallel.scheduler.ARConservativeBackfill;

import java.util.Calendar;
import java.util.LinkedList;

import scu.common.model.PropertyTypes;
import scu.common.model.Service;
import scu.util.Util;

public class GSService extends ParallelResource {

    protected Service service;
    protected AllocPolicy scheduler;

    public GSService(Service service) throws Exception {
        // TODO: to have our own scheduler (AllocPolicy), 
        // instead of the simplified ARSimpleSpaceShared
        this(service, GSReservationPolicy.getInstance(service));
        //this(service, new ARConservativeBackfill(service.getTitle(), "Scheduler"));
    }

    public GSService(Service service, AllocPolicy scheduler) throws Exception {
        this(service.getTitle(),   
                Util.Integer(service.getProvider().getProperties().getValue(PropertyTypes.PROP_PERFORMANCE)), 
                Util.Double(service.getProvider().getProperties().getValue(PropertyTypes.PROP_TIMEZONE)), 
                Util.Double(service.getProvider().getProperties().getValue(PropertyTypes.PROP_COST)),
                scheduler);
        this.service = service;
        this.scheduler = scheduler;
        this.service.setId(super.get_id());
    }

    private GSService(String name, int performanceRating, double timeZone, 
            double costPerSec, AllocPolicy scheduler) throws Exception {
        super(name, 100.0, 
                //new GSServiceCharacteristics(performanceRating, timeZone, costPerSec), 
                new GSServiceCharacteristics(100, timeZone, costPerSec),
                //getDefaultIndividualCalender(timeZone), 
                scheduler);
        this.setName(name);
    }

    public GSService(Service service, AllocPolicy scheduler, 
            ResourceCharacteristics characteristics, ResourceCalendar calendar) throws Exception {
        super(service.getTitle(), 100.0, characteristics, 
                //calendar, 
                scheduler);
        this.service = service;
        this.setName(service.getTitle());
    }

    public static ResourceCalendar getDefaultIndividualCalender(double timeZone) {
        //double peakLoad = 1.0;        // the resource load during peak hour
        double peakLoad = 0.0;        // the resource load during peak hour
        //double offPeakLoad = 0.2;     // the resource load during off-peak hr
        double offPeakLoad = 0.0;     // the resource load during off-peak hr
        double holidayLoad = 0.0;     // the resource load during holiday
        //double holidayLoad = 1.0;     // the resource load during holiday
        long seed = 11L*13*17*19*23+1;

        LinkedList<Integer> weekends = new LinkedList<Integer>();
        weekends.add(new Integer(Calendar.SATURDAY));
        weekends.add(new Integer(Calendar.SUNDAY));

        LinkedList<Integer> holidays = new LinkedList<Integer>();
        return new ResourceCalendar(timeZone, peakLoad, offPeakLoad, holidayLoad, weekends, holidays, seed);
    }

    public Service getService() {
        return service;
    }
    
    protected synchronized void handleForecastResponseTime(ReservationMessage message) {
        
        // handle with ReservationPolicy
        GSReservationPolicy policy = (GSReservationPolicy)super.policy_;
        ReservationMessage response = policy.handleForecastResponseTime(message);
        
        // send response
        sendReservationMessage(response);
        
    }

    protected void processOtherEvent(Sim_event ev) {
        boolean handled = false;
        try {
            if (ev.get_tag() == MessageType.FORECAST_RESPONSE_TIME.intValue()) {
                ReservationMessage message = (ReservationMessage)ev.get_data();
                handleForecastResponseTime(message);
                handled = true;
            }
        }
        catch (ClassCastException c) {
            System.out.println(super.get_name() + 
                    ".processOtherEvent(): Exception occurs.");
        }
        if (!handled) super.processOtherEvent(ev);
        
    }

    protected void sendReservationMessage(ReservationMessage message) {
        super.send(message.getDestinationID(), GridSimTags.SCHEDULE_NOW, 
                message.getMessageType().intValue(), message);
    }

    @Override
    public String toString() {
        return service.toString();
    }

}
