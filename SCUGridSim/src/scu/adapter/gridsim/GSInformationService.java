package scu.adapter.gridsim;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import scu.common.interfaces.IServiceManager;
import gridsim.GridInformationService;
import gridsim.GridSim;
import gridsim.GridSimTags;

public class GSInformationService extends GridInformationService {

    IServiceManager manager;

    public GSInformationService(IServiceManager manager) throws Exception {
        super("SCUInformationService", GridSimTags.DEFAULT_BAUD_RATE);
        this.manager = manager;
    }

    @Override
    protected void processEvent(Sim_event ev) {
        super.processEvent(ev);
        // Get ID of an entity that send this event
        int id = ( (Integer) ev.get_data() ).intValue();
        switch (ev.get_tag())  {
            // A resource is requesting to register.
            case GridSimTags.REGISTER_RESOURCE:
                break;

            // A resource that can support Advance Reservation
            case GridSimTags.REGISTER_RESOURCE_AR:
                GSService service = (GSService)Sim_system.get_entity(id);
                manager.saveService(service.getService());
                break;

            // filter based on SLA
            case 1001:
                break;
        }
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
}
