package at.ac.tuwien.dsg.hcu.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class IllegalSimulationArgumentException extends WebApplicationException{

    //todo brk check that extends it lazim mi?

    public IllegalSimulationArgumentException() {
        super(
                Response
                        .status( Response.Status.BAD_REQUEST )
                        .build()
        );
    }
}
