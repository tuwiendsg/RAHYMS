package at.ac.tuwien.dsg.hcu.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by karaoglan on 17/04/16.
 */
public class IllegalSimulationArgumentException extends WebApplicationException{

    //todo brk check that extends it

    public IllegalSimulationArgumentException() {
        super(
                Response
                        .status( Response.Status.BAD_REQUEST )
                        .build()
        );
    }
}
