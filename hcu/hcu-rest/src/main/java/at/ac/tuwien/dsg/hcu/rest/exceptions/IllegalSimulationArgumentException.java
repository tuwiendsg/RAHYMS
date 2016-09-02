package at.ac.tuwien.dsg.hcu.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class IllegalSimulationArgumentException extends WebApplicationException{

    public IllegalSimulationArgumentException() {
        super(
                Response
                        .status( Response.Status.BAD_REQUEST )
                        .build()
        );
    }
}
