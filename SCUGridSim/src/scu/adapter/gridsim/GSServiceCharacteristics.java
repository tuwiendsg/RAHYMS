package scu.adapter.gridsim;

import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

public class GSServiceCharacteristics extends ResourceCharacteristics {

    // TODO: add ICU properties
    
    public GSServiceCharacteristics(int performanceRating, double timeZone, double costPerSec) {
        super(
                "ICU-Arch", 
                "ICU-OS", 
                getDefaultMachineList(performanceRating), 
                ResourceCharacteristics.ADVANCE_RESERVATION,
                timeZone, costPerSec);
    }
    
    private static MachineList getDefaultMachineList(int mipsRating) {
        MachineList list = new MachineList();
        //list.add(new Machine(0, 1, mipsRating));
        list.add(new Machine(0, 1, 1));
        return list;
    }

}
