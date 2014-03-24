package scu.adapter.gridsim;

import eduni.simjava.Sim_event;
import gridsim.ARGridResource;
import gridsim.ARPolicy;
import gridsim.ARSimpleSpaceShared;
import gridsim.GridSim;
import gridsim.ResourceCalendar;

import java.util.Calendar;
import java.util.LinkedList;

import scu.common.model.PropertyTypes;
import scu.common.model.Service;
import scu.util.Util;

public class GSService extends ARGridResource {

    protected Service service;
    protected ARPolicy scheduler;

    public GSService(Service service) throws Exception {
        // TODO: to have our own scheduler (ARPolicy), 
        // instead of the simplified ARSimpleSpaceShared
        this(service, new ARSimpleSpaceShared(service.getTitle(), "Scheduler"));
    }

    public GSService(Service service, ARPolicy scheduler) throws Exception {
        this(service.getTitle(),   
                Util.Integer(service.getProvider().getProperties().getValue(PropertyTypes.PROP_PERFORMANCE)), 
                Util.Double(service.getProvider().getProperties().getValue(PropertyTypes.PROP_TIMEZONE)), 
                Util.Double(service.getProvider().getProperties().getValue(PropertyTypes.PROP_COST)),
                scheduler);
        this.service = service;
        this.scheduler = scheduler;
    }

    private GSService(String name, int performanceRating, double timeZone, 
            double costPerSec, ARPolicy scheduler) throws Exception {
        super(name, 100.0, 
                new GSServiceCharacteristics(performanceRating, timeZone, costPerSec), 
                getDefaultIndividualCalender(timeZone), scheduler);
    }

    public static ResourceCalendar getDefaultIndividualCalender(double timeZone) {
        double peakLoad = 1.0;        // the resource load during peak hour
        double offPeakLoad = 0.2;     // the resource load during off-peak hr
        double holidayLoad = 0.0;     // the resource load during holiday
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

    @Override
    public String toString() {
        return service.toString();
    }

    protected void processOtherEvent(Sim_event ev) {
        try {
            Integer obj = (Integer) ev.get_data();

            // get the sender name
            String name = GridSim.getEntityName(obj.intValue());
            switch (ev.get_tag()) {
                case GSConstants.HELLO_TAG:
                    System.out.println(super.get_name() 
                            + ": received HELLO tag from " + name +
                            " at time " + GridSim.clock());
                    break;

                case GSConstants.TEST_TAG:
                    System.out.println(super.get_name() 
                            + ": received TEST tag from " + name + 
                            " at time " + GridSim.clock());
                    break;

            default:
                break;
            }
        }
        catch (ClassCastException c) {
            System.out.println(super.get_name() + 
                    ".processOtherEvent(): Exception occurs.");
        }

    }


}
