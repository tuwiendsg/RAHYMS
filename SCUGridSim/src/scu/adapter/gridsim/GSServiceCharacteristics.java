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
                new MachineList(), 
                ResourceCharacteristics.ADVANCE_RESERVATION,
                timeZone, costPerSec);
        int mipsRating = performanceRating;
        this.getMachineList().add(new Machine(0, 1, mipsRating));
    }

}
